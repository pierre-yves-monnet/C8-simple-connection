package io.camunda.getstarted;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.worker.BackoffSupplier;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.util.Map;

@SpringBootApplication
@EnableZeebeClient
@EnableScheduling
@Deployment(resources = "classpath:send-email.bpmn")
public class ProcessApplication implements CommandLineRunner {

  private final static Logger LOG = LoggerFactory.getLogger(ProcessApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(ProcessApplication.class, args);
  }

  @Autowired
  private ZeebeClient zeebeClient;

  @Override
  public void run(final String... args) throws Exception {

    // register the worker in the ZeebeClient
    zeebeClient.newWorker().jobType("explicit-email").handler(new EmailExplicitWorker())
        .pollInterval(Duration.ofMillis(100 ))
        .backoffSupplier(new BackoffSupplier() {
          @Override
          public long supplyRetryDelay(long currentRetryDelay) {
            return 0;
          }
        })
        .open();


    final ProcessInstanceEvent event =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("send-email")
            .latestVersion()
            .variables(Map.of("message_content", "Hello from the Spring Boot get started"))
            .send()
            .join();

    LOG.info("Started instance for processDefinitionKey='{}', bpmnProcessId='{}', version='{}' with processInstanceKey='{}'",
        event.getProcessDefinitionKey(), event.getBpmnProcessId(), event.getVersion(), event.getProcessInstanceKey());
  }
  @Scheduled(cron = "0 */10 * * * *") // Runs at every 10th minute
  public void runTask() {
    final ProcessInstanceEvent event =
        zeebeClient
            .newCreateInstanceCommand()
            .bpmnProcessId("send-email")
            .latestVersion()
            .variables(Map.of("message_content", "Hello from the Spring Boot get started"))
            .send()
            .join();
    LOG.info(">>>>>>>>>>>>>>>>>>>> Started instance for processDefinitionKey='{}', bpmnProcessId='{}', version='{}' with processInstanceKey='{}'",
        event.getProcessDefinitionKey(), event.getBpmnProcessId(), event.getVersion(), event.getProcessInstanceKey());

  }
}
