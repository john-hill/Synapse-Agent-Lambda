package org.sagebionetworks.agent.action;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sagebionetworks.agent.action.model.Agent;
import org.sagebionetworks.agent.action.model.AgentInputEvent;
import org.sagebionetworks.agent.action.model.Parameter;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.google.gson.Gson;

public class MainRequestHandlerTest {

	private Gson gson;
	private Context mockContext;
	private LambdaLogger mockLogger;

	@BeforeEach
	public void before() {
		gson = new Gson();
		mockContext = Mockito.mock(Context.class);
		mockLogger = Mockito.mock(LambdaLogger.class);
	}

	@Test
	public void testLiveHandler() throws IOException {
		when(mockContext.getLogger()).thenReturn(mockLogger);
		List<Parameter> param = List.of(new Parameter("term", "string", "cows"));
		AgentInputEvent event = new AgentInputEvent("1.0", new Agent("Bond", "007", "alias", "1.0"), "input", "session",
				"discovery", "search", param, null, null);
		String message = gson.toJson(event);
		// call under test
		MainRequestHandler handler = new MainRequestHandler();
		
//		String result = handler.handleRequest(message, mockContext);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		handler.handleRequest(new ByteArrayInputStream(message.getBytes(StandardCharsets.US_ASCII)), out, mockContext);
		String result = new String(out.toByteArray(), StandardCharsets.US_ASCII);
		
		assertNotNull(result);
		System.out.println(result);
	}
}
