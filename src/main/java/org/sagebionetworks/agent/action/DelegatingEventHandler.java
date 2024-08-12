package org.sagebionetworks.agent.action;

import org.sagebionetworks.agent.action.handler.EventHandler;
import org.sagebionetworks.agent.action.handler.HandlerProvider;
import org.sagebionetworks.agent.action.model.AgentInputEvent;
import org.sagebionetworks.agent.action.model.AgentResponse;
import org.sagebionetworks.agent.action.model.FunctionResponse;
import org.sagebionetworks.agent.action.model.Response;
import org.sagebionetworks.agent.action.model.ResponseState;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

/**
 * Delegates each event to the specific function handler.
 */
public class DelegatingEventHandler implements RequestHandler<String, String> {

	private final Gson gson;
	private final HandlerProvider handlerProvider;

	@Inject
	public DelegatingEventHandler(Gson gson, HandlerProvider handlerProvdier) {
		super();
		this.gson = gson;
		this.handlerProvider = handlerProvdier;
	}

	@Override
	public String handleRequest(String input, Context context) {
		AgentInputEvent event = this.gson.fromJson(input, AgentInputEvent.class);
		try {
			context.getLogger().log(
					String.format("Handling action group: '%s' function: '%s'", event.actionGroup(), event.function()));
			EventHandler handler = handlerProvider.getHandlerForFunction(event.actionGroup(), event.function())
					.orElseThrow(() -> new IllegalArgumentException(
							String.format("No handler found for action group: '%s' function: '%s'", event.actionGroup(),
									event.function())));
			String body = handler.handleEvent(event, context);
			// There is not state for success.
			ResponseState state = null;
			return createResponse(event, state, body);
		} catch (Exception e) {
			context.getLogger().log(String.format("Failed to handle action group: '%s' function: '%s' message: '%s'",
					event.actionGroup(), event.function(), e.getMessage()));
			JsonObject body = new JsonObject();
			body.addProperty("errorMessage", e.getMessage());
			return createResponse(event, ResponseState.FAILURE, body.toString());
		}
	}

	String createResponse(AgentInputEvent event, ResponseState responseState, String body) {
		JsonObject bodyParent = new JsonObject();
		bodyParent.addProperty("body", body);
		JsonObject responseBody = new JsonObject();
		responseBody.add("TEXT", bodyParent);
		return this.gson.toJson(new AgentResponse(event.messageVersion(),
				new Response(event.actionGroup(), event.function(), new FunctionResponse(responseState, responseBody)),
				event.sessionAttributes(), event.promptSessionAttributes()));
	}
}
