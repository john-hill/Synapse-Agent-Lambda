# Synapse-Agent-Lambda

## About this Project
This project includes a working proof-of-concent application that uses an [AWS bedrock agent](https://docs.aws.amazon.com/bedrock/latest/userguide/agents.html) to allow a user to chat with an LLM about data found in Synapse.  One of our main  goal of this project was to leverage existing Synapse APIs as a data source the user and LLM interactions.

Initially, we setup this project to define the [AWS lambda functions](https://docs.aws.amazon.com/lambda/latest/dg/welcome.html) to handle the agent action group function calls.  This allowed us to test interactions with the agent from within the AWS bedrock agent web console, since everything was in the cloud.  However, it became clear that we would need to provide our own application to invoke the agent calls to support use case involving user authentication.  Therefore, we extended this project to include a console application.

The application is a simple console application that makes agent_invoke call on the user's behalf.  The application starts by forwarding user prompts to the agent via the AWS bedrock agent runtime invoke_agent API.  The agent can either directly reply to a user's prompt or request more information from Synapse with a "return control" response.  The application will print direct replies from the agent into the console.  However, when the application receives a "return control" response from the agent, the response will include an action group, function name, and function parameters that define what information the agent would like the application to provide.  The application will then match and forward the response to the appropriate local ReturnControlHandler.  Typically a ReturnControlHandler will make a Synapse API call to fetch the data requested by the agent.  The application will then forward the data to the agent via a second subsequent invoke_agent.  This process will continue until the agent decides it has enough information to respond to the user's original request, thus returning a direct reply.

The agent uses a client-provided sessionId to that identifies a single user's conversion with the agent.  The agent will associate all user prompts, return control provide data, and previous agent response to the provided sessionId.  This allows the agent to maintain a long running "memory" of a single user's conversation.  Currently, the application will generate a new UUID as a sessionID, so the user's conversation is preserved for the duration of the application's run.  However, it should be possible to extend the application to accept a sesionId from a previous run to extend the conversation beyond the life of the application.

    

## Getting Started
### Local machine
If you would like to run the application on your own machine you will need Java 17+ and a recent version of maven.  This can be checked by running:

```
mvn -v
```

Should give you something like:

>Apache Maven 3.9.7 (8b094c9513efc1b9ce2d952b3b9c8eaedaf8cbf0)

>Java version: 17.0.12, vendor: Amazon.com Inc., runtime: C:\Program Files\Amazon Corretto\jdk17.0.12_7

### Agent Setup
You will need access to an AWS account with the permissions to run both CloudFormation scripts and Bedrock.
You will need to run the provided `agent_template.json` cloud formation script which will setup a bedrock agent and associated action groups.

Once cloud formation creates your agent you will need to copy the agent's ID that will be need in the next step.  The agent ID is circled in the following screen shot:

![agent ID](images/AgetnId.png)

### Run Application
Once your machine is setup with Java and maven and you have your AWS bedrock agent Id you are ready to run the local application in a terminal.


Note: This application uses the AWS SDK to make invoke_agent calls to the agent you created in the previous step.  Therefore, you will need to ensure that AWS can find your credentials on your machine.  For more information see [Configuration and credentials precedence](https://docs.aws.amazon.com/cli/v1/userguide/cli-chap-configure.html#configure-precedence).  If you decide to provide the credentials using java properties then use:
```
-Daws.accessKeyId=<your-key>
-Daws.secretAccessKey=<your-secret>
```


First, let's get the project from github:

```
git clone https://github.com/Sage-Bionetworks/Synapse-Agent-Lambda.git
```
Followed by:

```
cd Synapse-Agent-Lambda
```
Run a full build to generate the application:

```
mvn clean intall
```
We can now run the application by replacing `<your-agent-id>` with the agent ID from the previous step. Be sure to make your AWS credentials available as noted above.

```
java -Dorg.sagebionetworks.agent.id=<your-agent-id> -cp app/target/app-develop-SNAPSHOT.jar org.sagebionetworks.app.AppMain
```





