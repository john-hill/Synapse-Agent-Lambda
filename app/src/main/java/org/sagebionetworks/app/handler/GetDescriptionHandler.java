package org.sagebionetworks.app.handler;

import java.math.BigDecimal;

import org.openapitools.client.ApiException;
import org.openapitools.client.api.WikiPageServicesApi;
import org.sagebionetworks.agent.action.parameter.ParameterUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

public class GetDescriptionHandler implements ReturnControlHandler {

	private final WikiPageServicesApi wikiApi;
	private final Gson gson;

	@Inject
	public GetDescriptionHandler(WikiPageServicesApi wikiApi, Gson gson) {
		super();
		this.wikiApi = wikiApi;
		this.gson = gson;
	}

	@Override
	public String getActionGroup() {
		return "discovery";
	}

	@Override
	public String getFunction() {
		return "get_description";
	}

	@Override
	public String handleEvent(ReturnControlEvent event) throws ApiException {
		String synId = ParameterUtils.extractParameter(String.class, "synId", event.params())
				.orElseThrow(() -> new IllegalArgumentException("Parameter 'synId' of type string is required"));
//		System.out.println("getting description for: " + synId);
		StringBuilder builder = new StringBuilder();
		var offset = BigDecimal.valueOf(0L);
		var limit = BigDecimal.valueOf(5L);
		var headers = wikiApi.getRepoV1EntityOwnerIdWikiheadertree(synId, offset, limit).getResults();
		if (headers != null) {
			for (var h : headers) {
				builder.append(wikiApi.getRepoV1EntityOwnerIdWikiWikiId(synId, h.getId(), null).getMarkdown());
				builder.append("\n");
			}
		}
		JsonObject object = new JsonObject();
		object.addProperty("description", builder.toString());
		return gson.toJson(object);
	}

}
