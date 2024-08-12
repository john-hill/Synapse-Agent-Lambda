package org.sagebionetworks.agent.action.handler;

import org.openapitools.client.ApiException;
import org.openapitools.client.api.SearchServicesApi;
import org.openapitools.client.model.OrgSagebionetworksRepoModelSearchQuerySearchQuery;
import org.openapitools.client.model.OrgSagebionetworksRepoModelSearchSearchResults;
import org.sagebionetworks.agent.action.model.AgentInputEvent;
import org.sagebionetworks.agent.action.parameter.ParameterUtils;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;
import com.google.inject.Inject;

public class SearchHandler implements EventHandler {

	private final SearchServicesApi searchService;
	private final Gson gson;

	@Inject
	public SearchHandler(SearchServicesApi searchService, Gson gson) {
		this.searchService = searchService;
		this.gson = gson;
	}

	@Override
	public String handleEvent(AgentInputEvent event, Context context) throws ApiException {
		String term = ParameterUtils.extractParameter(String.class, "term", event.parameters())
				.orElseThrow(() -> new IllegalArgumentException("Parameter 'term' of type string is required"));
		OrgSagebionetworksRepoModelSearchSearchResults results = searchService
				.postRepoV1Search(new OrgSagebionetworksRepoModelSearchQuerySearchQuery().addQueryTermItem(term));
		return results.toJson();
	}

	@Override
	public String getActionGroup() {
		return "discovery";
	}

	@Override
	public String getFunction() {
		return "search";
	}

}
