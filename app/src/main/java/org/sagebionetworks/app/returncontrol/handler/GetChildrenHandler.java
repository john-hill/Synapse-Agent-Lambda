package org.sagebionetworks.app.returncontrol.handler;

import java.util.List;

import org.json.JSONObject;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.EntityServicesApi;
import org.openapitools.client.model.OrgSagebionetworksRepoModelEntityChildrenRequest;
import org.sagebionetworks.agent.action.parameter.ParameterUtils;
import org.sagebionetworks.app.returncontrol.ReturnControlEvent;

import com.google.gson.Gson;
import com.google.inject.Inject;

public class GetChildrenHandler implements ReturnControlHandler {

	private final EntityServicesApi entityApi;
	private final Gson gson;

	@Inject
	public GetChildrenHandler(EntityServicesApi entityApi, Gson gson) {
		super();
		this.entityApi = entityApi;
		this.gson = gson;
	}

	@Override
	public String getActionGroup() {
		return "discovery";
	}

	@Override
	public String getFunction() {
		return "get_entity_children";
	}

	@Override
	public String handleEvent(ReturnControlEvent event) throws ApiException {
		String synId = ParameterUtils.extractParameter(String.class, "synId", event.params())
				.orElseThrow(() -> new IllegalArgumentException("Parameter 'synId' of type string is required"));
		try {
			return entityApi
					.postRepoV1EntityChildren(new OrgSagebionetworksRepoModelEntityChildrenRequest()
							.parentId(synId).includeSumFileSizes(false).includeTotalChildCount(true)
							.sortBy("MODIFIED_ON").sortDirection("DESC").includeTypes(List.of("project", "folder",
									"file", "table", "entityview", "dataset", "datasetcollection", "materializedview")))
					.toJson();
		} catch (ApiException e) {
			return gson.toJson(new JSONObject().put("requestFailedMessage", e.getMessage()));
		}
	}

}
