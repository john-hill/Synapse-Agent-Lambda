package org.sagebionetworks.app;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * This application provides a simple console interaction with a bedrock agent
 * configured with "return control" action groups. All agent interactions occur
 * via the bedrock-agent-runtime invoke_agent AWS API. This application will
 * make calls to the Synapse API (unauthenticated for now) to gather all data to be
 * forwarded to the agent for agent "return control" responses.
 * </p>
 * In order to run this application the default AWS credential provider must be
 * able to find your AWS credentials. In addition, you will need to provide the
 * following properties:
 * <ol>
 * <li>The bedrock agent id needs to be provided:
 * {@code org.sagebionetworks.agent.id}</li>
 * <li>One option for providing AWS credentials is to include the standard AWS
 * java properties: {@code aws.accessKeyId} and {@code aws.secretAccessKey}</li>
 * </ol>
 * Input from the console is sent to the configured agent
 */
public class AppMain {

	public static void main(String[] args) throws Exception {
		Injector injector = Guice.createInjector(new AppModule());
		AppRunner runner = injector.getInstance(AppRunner.class);
		runner.start();
	}

}
