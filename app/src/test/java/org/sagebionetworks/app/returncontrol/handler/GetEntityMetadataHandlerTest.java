package org.sagebionetworks.app.returncontrol.handler;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.EntityBundleServicesV2Api;
import org.sagebionetworks.agent.action.model.Parameter;
import org.sagebionetworks.app.returncontrol.ReturnControlEvent;

import com.google.gson.Gson;

@ExtendWith(MockitoExtension.class)
public class GetEntityMetadataHandlerTest {

	private GetEntityMetadataHandler handler;

	@BeforeEach
	public void before() {
		handler = new GetEntityMetadataHandler(new EntityBundleServicesV2Api(), new Gson());
	}

	@Test
	public void testWithDoesNotExist() throws ApiException {

		String result = handler
				.handleEvent(new ReturnControlEvent(null, null, List.of(new Parameter("synId", "string", "syn123"))));
		assertTrue(result.contains("does not exist"));
	}
	
	@Test
	public void testWithKnowns() throws ApiException {
		String result = handler
				.handleEvent(new ReturnControlEvent(null, null, List.of(new Parameter("synId", "string", "syn38550710"))));
		assertTrue(result.contains("How Cows Beat cancer"));
	}
}
