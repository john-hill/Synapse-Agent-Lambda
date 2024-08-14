package org.sagebionetworks.agent.action;

import java.util.List;

import org.openapitools.client.api.SearchServicesApi;
import org.sagebionetworks.agent.action.handler.HandlerProvider;
import org.sagebionetworks.agent.action.handler.HandlerProviderImpl;
import org.sagebionetworks.agent.action.handler.SearchHandler;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class AgentModule extends AbstractModule {

	@Provides
	public Gson createGson() {
		return new Gson();
	}

	@Provides
	public SearchServicesApi createSearchService() {
		return new SearchServicesApi();
	}

	@Provides
	public HandlerProvider createHandlerProvider(SearchHandler searchHandler) {
		return new HandlerProviderImpl(List.of(searchHandler));
	}
}
