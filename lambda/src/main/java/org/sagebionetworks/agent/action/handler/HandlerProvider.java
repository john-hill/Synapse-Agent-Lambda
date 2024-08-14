package org.sagebionetworks.agent.action.handler;

import java.util.Optional;

public interface HandlerProvider {

	/**
	 * Lookup the handler for the given function.
	 * 
	 * @param function
	 * @return {@link Optional#empty()} when no handler is found.
	 */
	Optional<EventHandler> getHandlerForFunction(String actionGroup, String function);
}
