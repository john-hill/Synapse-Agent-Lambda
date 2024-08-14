package org.sagebionetworks.agent.action.handler;

import org.openapitools.client.ApiException;
import org.sagebionetworks.agent.action.model.AgentInputEvent;

import com.amazonaws.services.lambda.runtime.Context;

public interface EventHandler {

	/**
	 * Handle the provided event and generate the expected JSON response body
	 * string.
	 * 
	 * @param event
	 * @param context The lambda context of the event.
	 * @return The response should a JSON string.
	 * @throws ApiException
	 */
	String handleEvent(AgentInputEvent event, Context context) throws ApiException;

	/**
	 * The name of the action group that this function belongs to.
	 * 
	 * @return
	 */
	String getActionGroup();
	
	/**
	 * The name of the function to be handled. 
	 * @return
	 */
	String getFunction();

}
