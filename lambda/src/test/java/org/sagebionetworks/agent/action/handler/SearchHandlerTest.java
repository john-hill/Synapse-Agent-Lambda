package org.sagebionetworks.agent.action.handler;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openapitools.client.ApiException;
import org.openapitools.client.api.SearchServicesApi;
import org.sagebionetworks.agent.action.model.AgentInputEvent;
import org.sagebionetworks.agent.action.model.Parameter;

import com.amazonaws.services.lambda.runtime.Context;
import com.google.gson.Gson;

@ExtendWith(MockitoExtension.class)
public class SearchHandlerTest {

	@Mock
	private Context mockContext;

	private SearchHandler handler;

	@BeforeEach
	public void before() {
		handler = new SearchHandler(new SearchServicesApi(), new Gson());
	}

	@Test
	public void testSearch() throws ApiException {
		List<Parameter> param = List.of(new Parameter("term", "string", "cows"));
		AgentInputEvent event = new AgentInputEvent(null, null, null, null, null, null, param, null, null);
		// call under test
		String result = handler.handleEvent(event, mockContext);
		assertNotNull(result);
		System.out.println(result.toString());
	}
}
