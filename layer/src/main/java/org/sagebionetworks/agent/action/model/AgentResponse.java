package org.sagebionetworks.agent.action.model;

import java.util.Map;

public record AgentResponse(String messageVersion, Response response, Map<String, String> sessionAttributes,
		Map<String, String> promptSessionAttributes) {
}