package org.sagebionetworks.app.handler;

import org.openapitools.client.ApiException;
import org.openapitools.client.api.SearchServicesApi;
import org.openapitools.client.model.OrgSagebionetworksRepoModelSearchQuerySearchQuery;
import org.openapitools.client.model.OrgSagebionetworksRepoModelSearchSearchResults;
import org.sagebionetworks.agent.action.parameter.ParameterUtils;

import com.google.gson.Gson;
import com.google.inject.Inject;

public class SearchHandler implements ReturnControlHandler {

	private static final int MAX_NUM_CHARS = 200;
	private final SearchServicesApi searchService;
	private final Gson gson;

	@Inject
	public SearchHandler(SearchServicesApi searchService, Gson gson) {
		this.searchService = searchService;
		this.gson = gson;
	}

	@Override
	public String getActionGroup() {
		return "discovery";
	}

	@Override
	public String getFunction() {
		return "search";
	}

	@Override
	public String handleEvent(ReturnControlEvent event) throws ApiException {
		String term = ParameterUtils.extractParameter(String.class, "term", event.params())
				.orElseThrow(() -> new IllegalArgumentException("Parameter 'term' of type string is required"));
		OrgSagebionetworksRepoModelSearchSearchResults results = searchService
				.postRepoV1Search(new OrgSagebionetworksRepoModelSearchQuerySearchQuery().addQueryTermItem(term));
		results.getHits().forEach(h->{
			if(h.getDescription() != null && h.getDescription().length() > MAX_NUM_CHARS) {
//				System.out.println("truncating: "+h.getId());
				h.setDescription(String.format("%s --truncated--", h.getDescription().subSequence(0, MAX_NUM_CHARS)));
			}
		});
		return results.toJson();
	}

}
