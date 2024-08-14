package org.sagebionetworks.agent.action.model;

import java.util.List;
import java.util.Map;
/**
 * Contains all of the model objects defined in:
 * https://docs.aws.amazon.com/bedrock/latest/userguide/agents-lambda.html
 */
public record AgentInputEvent(String messageVersion, Agent agent, String inputText, String sessionId, String actionGroup,
		String function, List<Parameter> parameters, Map<String, String> sessionAttributes,
		Map<String, String> promptSessionAttributes) {
}