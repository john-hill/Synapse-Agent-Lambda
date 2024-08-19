package org.sagebionetworks.app.handler;

import java.util.List;

import org.sagebionetworks.agent.action.model.Parameter;

public record ReturnControlEvent(String actionGroup, String function, List<Parameter> params) {
}
