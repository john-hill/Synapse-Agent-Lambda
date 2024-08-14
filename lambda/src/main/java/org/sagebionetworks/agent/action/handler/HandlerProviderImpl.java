package org.sagebionetworks.agent.action.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class HandlerProviderImpl implements HandlerProvider {

	private final Map<String, EventHandler> handlerMap;

	public HandlerProviderImpl(List<EventHandler> handlers) {
		handlerMap = new HashMap<>();
		handlers.stream().forEach((h) -> {
			handlerMap.put(createKey(h.getActionGroup(), h.getFunction()), h);
		});
	}

	@Override
	public Optional<EventHandler> getHandlerForFunction(String actionGroup, String function) {
		return Optional.ofNullable(handlerMap.get(createKey(actionGroup, function)));
	}

	static String createKey(String actionGroup, String function) {
		Objects.requireNonNull(actionGroup, "actionGroup");
		Objects.requireNonNull(function, "function");
		return String.format("%s-%s", actionGroup, function);
	}

}
