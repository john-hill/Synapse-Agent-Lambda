package org.sagebionetworks.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.openapitools.client.ApiException;
import org.sagebionetworks.agent.action.model.Parameter;
import org.sagebionetworks.app.handler.ReturnControlEvent;
import org.sagebionetworks.app.handler.ReturnControlHandlerProvider;

import com.google.inject.Inject;

import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.ContentBody;
import software.amazon.awssdk.services.bedrockagentruntime.model.FunctionResult;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvocationResultMember;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeAgentRequest;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeAgentResponseHandler;
import software.amazon.awssdk.services.bedrockagentruntime.model.InvokeAgentResponseHandler.Visitor;
import software.amazon.awssdk.services.bedrockagentruntime.model.ReturnControlPayload;
import software.amazon.awssdk.services.bedrockagentruntime.model.SessionState;

/**
 * This worker's job is to call invoke_agent providing the user's input string.
 * It will handle all returnControl responses by executing any requested
 * action+function and provide the results in subsequent invoke_agent calls.
 * Once the agent returns a response for the user, the worker will return that
 * response.
 */
public class InvokeWorker {

	public static final String TSTALIASID = "TSTALIASID";
	private final ExecutorService executor;
	private final ReturnControlHandlerProvider handlerProvider;
	private final BedrockAgentRuntimeAsyncClient bedrockAgentRuntimeClient;
	private final String agentId;

	@Inject
	public InvokeWorker(ExecutorService executor, ReturnControlHandlerProvider handlerProvider,
			BedrockAgentRuntimeAsyncClient bedrockAgentRuntimeClient, Configuration config) {
		super();
		this.executor = executor;
		this.handlerProvider = handlerProvider;
		this.bedrockAgentRuntimeClient = bedrockAgentRuntimeClient;
		this.agentId = config.getAgentId();
	}

	public Future<String> invokeAgent(String sessionId, String inputText) {
		return executor.submit(() -> {
			return invokeAgentWithText(sessionId, inputText);
		});
	}

	/**
	 * Send the user's text directly to the agent via an invoke_agent call.
	 * 
	 * @param sessionId
	 * @param inputText
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @return
	 */
	String invokeAgentWithText(String sessionId, String inputText) throws InterruptedException, ExecutionException {
		// This buffer is used to capture each chunk of data sent from the agent.
		var chunkedBuffer = new StringBuilder();
		var responseStreamHandler = InvokeAgentResponseHandler.builder()
				.subscriber(Visitor.builder().onReturnControl(payload -> {
					// The agent has requested more information, so another invoke_agent call is
					// needed.
					chunkedBuffer.append(invokeAgentWithReturnControlResults(sessionId, payload));
				}).onChunk(chunk -> {
					// Append the text to the response text buffer.
					chunkedBuffer.append(chunk.bytes().asUtf8String());
				}).build()).onResponse(resp -> {
				}).onError(t -> {
					t.printStackTrace();
				}).build();

		CompletableFuture<Void> future = bedrockAgentRuntimeClient
				.invokeAgent(InvokeAgentRequest.builder().agentId(this.agentId).agentAliasId(TSTALIASID)
						.sessionId(sessionId).enableTrace(false).inputText(inputText).build(), responseStreamHandler);
		future.get();
		return chunkedBuffer.toString();
	}

	/**
	 * This invoke_agent call is used to reply to a "return control" response from a
	 * previous invoke_agent call. This call will execute the requested events and
	 * then provide them to the agent with another invoke_agent call. Note: This
	 * method is recursive as the agent might respond with another "return control"
	 * response.
	 * 
	 * @param sessionId
	 * @param payloadIn
	 * @return
	 */
	String invokeAgentWithReturnControlResults(String sessionId, ReturnControlPayload payloadIn) {
		try {
			List<ReturnControlEvent> events = extractEvents(payloadIn);
			List<InvocationResultMember> eventResults = executeEvents(events);
			// This buffer is used to capture each chunk of data sent from the agent.
			var chunkedBuffer = new StringBuilder();
			var responseStreamHandler = InvokeAgentResponseHandler.builder()
					.subscriber(Visitor.builder().onReturnControl(payload -> {
						// The agent has requested more information, so another invoke_agent call is
						// needed.
						chunkedBuffer.append(invokeAgentWithReturnControlResults(sessionId, payload));
					}).onChunk(chunk -> {
						// Append the text to the response text buffer.
						chunkedBuffer.append(chunk.bytes().asUtf8String());
					}).build()).onResponse(resp -> {
					}).onError(t -> {
						t.printStackTrace();
					}).build();

			CompletableFuture<Void> future = bedrockAgentRuntimeClient.invokeAgent(
					InvokeAgentRequest.builder().agentId(this.agentId).agentAliasId(TSTALIASID).sessionId(sessionId)
							.sessionState(SessionState.builder().invocationId(payloadIn.invocationId())
									.returnControlInvocationResults(eventResults).build())
							.enableTrace(false).build(),
					responseStreamHandler);
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
			return chunkedBuffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	/**
	 * Execute each of the passed events and return each result in a list.
	 * 
	 * @param events
	 * @return
	 * @throws ApiException 
	 * @throws UnsupportedOperationException 
	 */
	List<InvocationResultMember> executeEvents(List<ReturnControlEvent> events) throws UnsupportedOperationException, ApiException {
		List<InvocationResultMember> results = new ArrayList<>();
		for (ReturnControlEvent e : events) {
			String responseBody = handlerProvider.getHandler(e.actionGroup(), e.function())
					.orElseThrow(() -> new UnsupportedOperationException(String.format(
							"No handler for actionGroup: '%s' and function: '%s'", e.actionGroup(), e.function())))
					.handleEvent(e);
			results.add(InvocationResultMember.builder()
					.functionResult(FunctionResult.builder().actionGroup(e.actionGroup()).function(e.function())
							.responseBody(Map.of("TEXT", ContentBody.builder().body(responseBody).build())).build())
					.build());
		}
		return results;
	}

	/**
	 * Helper to extract the events from the payload.
	 * 
	 * @param payload
	 * @return
	 */
	List<ReturnControlEvent> extractEvents(ReturnControlPayload payload) {
		List<ReturnControlEvent> events = new ArrayList<>();
		payload.invocationInputs().forEach(iim -> {
			var input = iim.functionInvocationInput();
			if (input == null) {
				throw new IllegalArgumentException("expected FunctionInvocationInput but was null");
			}
			List<Parameter> params = new ArrayList<>();
			input.parameters().forEach(p -> {

				params.add(new Parameter(p.name(), p.type(), p.value()));
			});
			events.add(new ReturnControlEvent(input.actionGroup(), input.function(), params));
		});
		return events;
	}
}
