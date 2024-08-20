package org.sagebionetworks.app.returncontrol.handler;

import org.json.JSONObject;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.EntityBundleServicesV2Api;
import org.openapitools.client.model.OrgSagebionetworksRepoModelEntitybundleV2EntityBundleRequest;
import org.sagebionetworks.agent.action.parameter.ParameterUtils;
import org.sagebionetworks.app.returncontrol.ReturnControlEvent;

import com.google.gson.Gson;
import com.google.inject.Inject;

public class GetEntityMetadataHandler implements ReturnControlHandler {

	private final EntityBundleServicesV2Api bundleApi;
	private final Gson gson;

	@Inject
	public GetEntityMetadataHandler(EntityBundleServicesV2Api bundleApi, Gson gson) {
		super();
		this.bundleApi = bundleApi;
		this.gson = gson;
	}

	@Override
	public String getActionGroup() {
		return "discovery";
	}

	@Override
	public String getFunction() {
		return "get_entity_metadata";
	}

	@Override
	public String handleEvent(ReturnControlEvent event) throws ApiException {
		String synId = ParameterUtils.extractParameter(String.class, "synId", event.params())
				.orElseThrow(() -> new IllegalArgumentException("Parameter 'synId' of type string is required"));
		try {
			var result = bundleApi.postRepoV1EntityIdBundle2(synId,
					new OrgSagebionetworksRepoModelEntitybundleV2EntityBundleRequest().includeAccessControlList(true)
							.includeAnnotations(true).includeAnnotations(true).includeBenefactorACL(true)
							.includeEntity(true).includePermissions(false).includeRestrictionInformation(false)
							.includeTableBundle(true));
			return result.toJson();
		} catch (ApiException e) {
			return gson.toJson(new JSONObject().put("requestFailedMessage", e.getMessage()));
		}
	}

}
