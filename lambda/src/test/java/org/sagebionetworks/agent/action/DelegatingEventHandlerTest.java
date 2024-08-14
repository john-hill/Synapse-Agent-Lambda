package org.sagebionetworks.agent.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.ApiException;
import org.sagebionetworks.agent.action.handler.EventHandler;
import org.sagebionetworks.agent.action.handler.HandlerProvider;
import org.sagebionetworks.agent.action.model.Agent;
import org.sagebionetworks.agent.action.model.AgentInputEvent;
import org.sagebionetworks.agent.action.model.AgentResponse;
import org.sagebionetworks.agent.action.model.FunctionResponse;
import org.sagebionetworks.agent.action.model.Parameter;
import org.sagebionetworks.agent.action.model.Response;
import org.sagebionetworks.agent.action.model.ResponseState;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@ExtendWith(MockitoExtension.class)
public class DelegatingEventHandlerTest {

	@Mock
	private HandlerProvider mockHandlerProvider;

	@Mock
	private EventHandler mockEventHandler;

	@Mock
	private LambdaLogger mockLogger;

	@Mock
	private Context mockContext;

	private Gson gson = new Gson();
	private AgentInputEvent event;

	private DelegatingEventHandler handler;

	@BeforeEach
	public void before() {
		this.handler = new DelegatingEventHandler(gson, mockHandlerProvider);
		List<Parameter> params = List.of(new Parameter("item", "string", "some-value"));
		event = new AgentInputEvent("1.0", new Agent("foo", "123", "bar", "99"), "inputText", "sessionId", "action-0",
				"function-1", params, Map.of("key1", "value-1"), Map.of("key3", "value-3"));
	}

	@Test
	public void testHandleRequest() throws ApiException {
		JsonObject result = new JsonObject();
		result.addProperty("result", "success");
		when(mockContext.getLogger()).thenReturn(mockLogger);
		when(mockEventHandler.handleEvent(any(), any())).thenReturn(result.toString());
		when(mockHandlerProvider.getHandlerForFunction(any(), any())).thenReturn(Optional.of(mockEventHandler));
		String eventJson = gson.toJson(event);
		// call under test
		String response = handler.handleRequest(eventJson, mockContext);

		AgentResponse agentReponse = gson.fromJson(response, AgentResponse.class);
		// setup expected.
		JsonObject bodyParent = new JsonObject();
		bodyParent.addProperty("body", result.toString());
		JsonObject responseBody = new JsonObject();
		responseBody.add("TEXT", bodyParent);
		AgentResponse expected = new AgentResponse(event.messageVersion(),
				new Response(event.actionGroup(), event.function(), new FunctionResponse(null, responseBody)),
				event.sessionAttributes(), event.promptSessionAttributes());
		assertEquals(expected, agentReponse);

		verify(mockLogger).log("Handling action group: 'action-0' function: 'function-1'");
		verify(mockEventHandler).handleEvent(event, mockContext);
		verify(mockHandlerProvider).getHandlerForFunction(event.actionGroup(), event.function());
		verifyNoMoreInteractions(mockLogger, mockEventHandler, mockHandlerProvider);
	}

	@Test
	public void testHandleRequestWithNoHandler() throws ApiException {
		JsonObject result = new JsonObject();
		result.addProperty("result", "success");
		when(mockContext.getLogger()).thenReturn(mockLogger);
		// no handler found
		when(mockHandlerProvider.getHandlerForFunction(any(), any())).thenReturn(Optional.empty());
		String eventJson = gson.toJson(event);
		// call under test
		String response = handler.handleRequest(eventJson, mockContext);

		AgentResponse agentReponse = gson.fromJson(response, AgentResponse.class);
		// setup expected
		JsonObject errorObject = new JsonObject();
		errorObject.addProperty("errorMessage", "No handler found for action group: 'action-0' function: 'function-1'");
		JsonObject bodyParent = new JsonObject();
		bodyParent.addProperty("body", errorObject.toString());
		JsonObject responseBody = new JsonObject();
		responseBody.add("TEXT", bodyParent);
		AgentResponse expected = new AgentResponse(event.messageVersion(),
				new Response(event.actionGroup(), event.function(),
						new FunctionResponse(ResponseState.FAILURE, responseBody)),
				event.sessionAttributes(), event.promptSessionAttributes());
		assertEquals(expected, agentReponse);

		verify(mockLogger).log("Handling action group: 'action-0' function: 'function-1'");
		verify(mockLogger).log(
				"Failed to handle action group: 'action-0' function: 'function-1' message: 'No handler found for action group: 'action-0' function: 'function-1''");
		verify(mockHandlerProvider).getHandlerForFunction(event.actionGroup(), event.function());
		verifyNoMoreInteractions(mockLogger, mockEventHandler, mockHandlerProvider);
	}
}
