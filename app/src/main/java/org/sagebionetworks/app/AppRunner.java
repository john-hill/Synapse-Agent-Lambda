package org.sagebionetworks.app;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;;

public class AppRunner {

	private final Scanner in;
	private final PrintStream out;
	private final InvokeWorker invokeWorker;

	@Inject
	public AppRunner(Scanner in, PrintStream out, InvokeWorker invokeWorker) {
		super();
		this.in = in;
		this.out = out;
		this.invokeWorker = invokeWorker;
	}

	public void start() throws InterruptedException, ExecutionException, IOException {
		out.println("Welcome to the Synapse Bedrock Agent Client Application Demo!");
		String sessionId = UUID.randomUUID().toString();
		out.println("Starting a new session: "+sessionId);
		out.println("You can terminate the session by typing 'exit'");
		out.println("How can the Agent help today?");
		out.println();

		while (true) {
			String line = in.nextLine();
			if(line.startsWith("exit")) {
				break;
			}
			if(StringUtils.isBlank(line)) {
				continue;
			}
			Future<String> future = invokeWorker.invokeAgent(sessionId, line);
			printProgress(future);
			out.println(future.get());
		}
		out.println("Good bye");
	}

	/**
	 * This call will block as long as the passed future is still running. Each
	 * second progress will be printed to out. If the call fails, this method will
	 * throw the exception.
	 * 
	 * @param future
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	void printProgress(Future<String> future) throws InterruptedException, ExecutionException {
		while (!future.isDone()) {
			out.print(".");
			Thread.sleep(1000L);
		}
		out.println();
	}

}
