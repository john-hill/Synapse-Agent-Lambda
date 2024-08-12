package org.sagebionetworks.agent.action.model;

import com.google.gson.JsonObject;

public record FunctionResponse(ResponseState responseState, JsonObject responseBody) {
}