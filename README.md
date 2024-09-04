# Camunda Platform 8 - Get Started - Spring Boot

This guide explains how to setup a Spring Boot project to automate a process using
[Camunda Platform 8](https://camunda.com/products/cloud/).

# Install dependencies

The open source library [spring-zeebe](https://github.com/camunda-community-hub/spring-zeebe)
provides a Zeebe client.

```
<dependency>
  <groupId>io.camunda</groupId>
  <artifactId>spring-zeebe-starter</artifactId>
  <version>8.5.5</version>
</dependency>
```

# Create Client

If we want to connect to a Camunda Platform 8 SaaS cluster we need the `clusterId` from the 
[Clusters details page](https://docs.camunda.io/docs/components/console/manage-clusters/create-cluster/),
a `clientId` and `clientSecret` from a [client credentials pair](https://docs.camunda.io/docs/components/console/manage-clusters/manage-api-clients/). 

The credentails can be added to the application.properties.

```properties
zeebe.client.cloud.clusterId=365eXXXXXe
zeebe.client.cloud.clientId=GZXXXXXXXXXXXXX
zeebe.client.cloud.clientSecret=.RXXXXXXXXXXXXXXXXXXXZ-7WYNJ
```

If you are using a self managed Camunda Platform 8 cluster, you create the client
using the following application config, see
[application.localhost.yaml](src/main/resources/application.localhost.properties).

```properties
zeebe.client.broker.gatewayAddress=127.0.0.1:26500
zeebe.client.security.plaintext=true
```

To enable the Zeebe client integration annotate your application class with
`@EnableZeebeClient`, see
[ProcessApplication.java](src/main/java/io/camunda/getstarted/ProcessApplication.java).

```java
@SpringBootApplication
@EnableZeebeClient
public class ProcessApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProcessApplication.class, args);
  }

}
```

# Deploy Process and Start Instance

To deploy a process you can use the annotation `@Deployment`, which allows
to specify a list of `resources` (e.g. from classpath) to be deployed on start up.

```java
@SpringBootApplication
@EnableZeebeClient
@Deployment(resources = "classpath:send-email.bpmn")
public class ProcessApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProcessApplication.class, args);
  }

}
```

To start a new instance you can specify the `bpmnProcessId`, i.e.
`send-email` and **optionally** process variables.

```java
final ProcessInstanceEvent event =
  client
    .newCreateInstanceCommand()
    .bpmnProcessId("send-email")
    .latestVersion()
    .variables(Map.of("message_content", "Hello from the Spring Boot get started"))
    .send()
    .join();

LOG.info("Started instance for processDefinitionKey='{}', bpmnProcessId='{}', version='{}' with processInstanceKey='{}'",
	event.getProcessDefinitionKey(), event.getBpmnProcessId(), event.getVersion(), event.getProcessInstanceKey());
```

For the complete code see the
[`ProcessApplication.java`](src/main/java/io/camunda/getstarted/ProcessApplication.java) file. You can
run it using the following command.

```bash
mvn spring-boot:run
```

# Job Worker

To complete a
[service task](https://docs.camunda.io/docs/reference/bpmn-workflows/service-tasks/service-tasks/),
a [job worker](https://docs.camunda.io/docs/product-manuals/concepts/job-workers) has
to be subscribed the to task type defined in the process, i.e. `email`. For this
the `@JobWorker` annotation can be used and the `type` has to be specified.

```
@JobWorker(type = "email")
public void sendEmail(final ActivatedJob job) {
  final String message_content = (String) job.getVariablesAsMap().get("message_content");
  LOG.info("Sending email with message content: {}", message_content);
}
```

For the complete code see the
[EmailWorker.java](src/main/java/io/camunda/getstarted/EmailWorker.java) file. You can
run it using the following command.

```bash
mvn spring-boot:run
```

To make an job available, a user task has to be completed, follow the
instructions in [the guide](../README.md#complete-the-user-task).


# Blocking vs. Non-Blocking Code

The code example to start a process instance used 
```
send().join()
```
which is a blocking call to wait for the issues command to be executed on the workflow engine. 
While this is very straightforward to use and produces easy-to-read code, 
blocking code is limited in terms of scalability. 

This is discussed in more detail in [this blog post about writing good workers for Camunda Platform 8](https://blog.bernd-ruecker.com/writing-good-workers-for-camunda-cloud-61d322cad862).

# zbctl  


wsl

export ZEEBE_ADDRESS='b16XXXXXXXXXXXXXXXXXXXXXXXXXXk-1.zeebe.camunda.io:443'
export ZEEBE_CLIENT_ID='1zXXXXXXXXXXXXXXXXXXXXXXXm'
export ZEEBE_CLIENT_SECRET='T6ld9FXXXXXXXXXXXXXXXXXXXXJi_MO'
export ZEEBE_AUTHORIZATION_SERVER_URL='https://login.cloud.camunda.io/oauth/token'
export ZEEBE_REST_ADDRESS='https://jfk-1.zeebe.camunda.io/b16dXXXXXXXXXXXXXXXXXXXXXXXX'
export ZEEBE_GRPC_ADDRESS='grpcs://b1XXXXXXXXXXXXXXXXXXXXXjfk-1.zeebe.camunda.io:443'
export ZEEBE_TOKEN_AUDIENCE='zeebe.camunda.io'
export CAMUNDA_CLUSTER_ID='b16d7XXXXXXXXXXXXXXXXXXXX7c'

export CAMUNDA_CLUSTER_REGION='jfk-1'
export CAMUNDA_CREDENTIALS_SCOPES='Zeebe'
export CAMUNDA_OAUTH_URL='https://login.cloud.camunda.io/oauth/token'

zbctl status

# curl
Not possible to call a GRPC server

# postman
Do a GRPO call
URL: grpc://b16d70cb-b654-4d76-a3a4-d4e438e4447c.jfk-1.zeebe.camunda.io:443
==> Not working

curl -X POST --header 'content-type: application/json' --data '{"client_id": "<client-id>", "client_secret":"<client-secret>","audience":"<audience>","grant_type":"client_credentials"}' https://<authorization server url>





curl -XGET -H'Accept: application/json' -H'Authorization: Bearer <TOKEN>' http://localhost:8080/v2/topology



Local server
grpc://127.0.0.1:26500


## postman REST API
POST https://login.cloud.camunda.io/oauth/token
{
"client_id" : "1zFe.XXXG5Sm",
"client_secret" : "T6XXXXXXXXXi_MO",
"audience" : "zeebe.camunda.io",
"grant_type:"client_credentials"
}

"access_token": "eyJhbGcXXXXXXXXXXXX  lJrUkJOXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXyII1omQ",
"scope": "b16d70cb-b654-4d76-a3a4-d4e438e4447c",


http://b16XXXXXXXXXXXX7c.jfk-1.zeebe.camunda.io:443//v1/topology



https://accounts.cloud.camunda.io/advanced

zbctl status 

