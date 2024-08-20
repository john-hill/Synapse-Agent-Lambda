package org.sagebionetworks.app;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sagebionetworks.app.returncontrol.ReturnControlHandlerProvider;
import org.sagebionetworks.app.returncontrol.handler.GetChildrenHandler;
import org.sagebionetworks.app.returncontrol.handler.GetDescriptionHandler;
import org.sagebionetworks.app.returncontrol.handler.GetEntityMetadataHandler;
import org.sagebionetworks.app.returncontrol.handler.SearchHandler;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeAsyncClient;

public class AppModule extends AbstractModule {

	/**
	 * Create a new instance of the AmazonBedrockRuntime
	 * 
	 * @return
	 * 
	 * @return
	 */
	@Provides
	public BedrockAgentRuntimeAsyncClient createBedrockRuntime() {
		return BedrockAgentRuntimeAsyncClient.builder().credentialsProvider(DefaultCredentialsProvider.create())
				.region(Region.US_EAST_1).httpClient(NettyNioAsyncHttpClient.builder().build()).build();
	}

	@Provides
	Scanner inputStream() {
		return new Scanner(System.in, StandardCharsets.UTF_8);
	}

	@Provides
	PrintStream outputStream() {
		return System.out;
	}

	@Provides
	ExecutorService executorService() {
		return Executors.newFixedThreadPool(1);
	}

	@Provides
	Configuration configuration() {
		return new Configuration(System.getProperties());
	}

	@Provides
	ReturnControlHandlerProvider handlerProvider(SearchHandler searchHandler, GetDescriptionHandler descriptionHandler,
			GetEntityMetadataHandler getEntityMetadataHandler, GetChildrenHandler getChildrenHandler) {
		return new ReturnControlHandlerProvider(
				List.of(searchHandler, descriptionHandler, getEntityMetadataHandler, getChildrenHandler));
	}
}
