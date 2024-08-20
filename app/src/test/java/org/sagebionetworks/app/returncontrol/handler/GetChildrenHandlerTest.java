package org.sagebionetworks.app.returncontrol.handler;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.EntityServicesApi;
import org.sagebionetworks.agent.action.model.Parameter;
import org.sagebionetworks.app.returncontrol.ReturnControlEvent;

import com.google.gson.Gson;

public class GetChildrenHandlerTest {

	private GetChildrenHandler handler;
	
	@BeforeEach
	public void before() {
		handler = new GetChildrenHandler(new EntityServicesApi(), new Gson());
	}
	
	@Test
	public void testWithDoesNotExist() throws ApiException {
		String result = handler
				.handleEvent(new ReturnControlEvent(null, null, List.of(new Parameter("synId", "string", "syn123"))));
		assertTrue(result.contains("does not exist"));
	}
	
	@Test
	public void testWithKnown() throws ApiException {
		String result = handler
				.handleEvent(new ReturnControlEvent(null, null, List.of(new Parameter("synId", "string", "syn5048653"))));
		assertTrue(result.contains("leaderboard1"));
	}
}

