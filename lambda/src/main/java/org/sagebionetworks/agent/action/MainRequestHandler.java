package org.sagebionetworks.agent.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * The main entry point for the lambda function. This class should not contain
 * any business logic.
 */
public class MainRequestHandler implements RequestStreamHandler {

	private final DelegatingEventHandler handler;
	private final Gson gson;

	public MainRequestHandler() {
		Injector injector = Guice.createInjector(new AgentModule());
		this.handler = injector.getInstance(DelegatingEventHandler.class);
		this.gson = injector.getInstance(Gson.class);
	}


	@Override
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
		StringBuilder builder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.US_ASCII))) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		}
		String result = handler.handleRequest(builder.toString(), context);
		try(Writer writer = new OutputStreamWriter(output, StandardCharsets.US_ASCII)){
			writer.append(result);
		}
	}

}
