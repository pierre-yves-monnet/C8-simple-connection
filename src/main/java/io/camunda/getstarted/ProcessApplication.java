package io.camunda.getstarted;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.client.api.worker.BackoffSupplier;
import io.camunda.zeebe.client.api.worker.JobWorker;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
// @Deployment(resources = "classpath:send-email.bpmn")
public class ProcessApplication implements CommandLineRunner {

    private final static Logger log = LoggerFactory.getLogger(ProcessApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ProcessApplication.class, args);
    }

    @Autowired
    private ZeebeClient zeebeClient;


    @Override
    public void run(final String... args) throws Exception {
        /*
        log.info("Run");
        // register the worker in the ZeebeClient
        zeebeClient.newWorker().jobType("explicit-email").handler(new EmailExplicitWorker())
                .pollInterval(Duration.ofMillis(100))
                .backoffSupplier(new BackoffSupplier() {
                    @Override
                    public long supplyRetryDelay(long currentRetryDelay) {
                        return 0;
                    }
                })
                .open();


        final ProcessInstanceEvent processInstanceEvent =
                zeebeClient
                        .newCreateInstanceCommand()
                        .bpmnProcessId("send-email")
                        .latestVersion()
                        .variables(Map.of("message_content", "Hello from the Spring Boot get started"))
                        .send()
                        .join();

        log.info("Started instance[{}] for processDefinitionKey[{}], bpmnProcessId[{}], version[{}] with processInstanceKey[{}]",
                processInstanceEvent.getProcessInstanceKey(),
                processInstanceEvent.getProcessDefinitionKey(),
                processInstanceEvent.getBpmnProcessId(),
                processInstanceEvent.getVersion(),
                processInstanceEvent.getProcessInstanceKey());
                */
    }


    @Scheduled(cron = "0 */1 * * * *") // Runs at every 10th minute
    public void runTask() {
        final ProcessInstanceEvent processInstanceEvent =
                zeebeClient
                        .newCreateInstanceCommand()
                        .bpmnProcessId("send-email")
                        .latestVersion()
                        .variables(Map.of("message_content", "Hello from the Spring Boot get started"))
                        .send()
                        .join();
        log.info("Started instance[{}] for processDefinitionKey[{}], bpmnProcessId[{}], version[{}] with processInstanceKey[{}]",
                processInstanceEvent.getProcessInstanceKey(),
                processInstanceEvent.getProcessDefinitionKey(),
                processInstanceEvent.getBpmnProcessId(),
                processInstanceEvent.getVersion(),
                processInstanceEvent.getProcessInstanceKey());
        final ProcessInstanceEvent processInstanceEventWorker =
                zeebeClient
                        .newCreateInstanceCommand()
                        .bpmnProcessId("multiple-worker")
                        .latestVersion()
                        .variables(Map.of("message_content", "Run simulateworker"))
                        .send()
                        .join();
    }

    @PostConstruct
    public void postConstruct() {
        log.info("PostConstruct");
        /*
        ZeebeClient zeebeClient = ZeebeClient.newClientBuilder()
                .gatewayAddress("localhost:26500")
                .numJobWorkerExecutionThreads(20)
                .defaultJobWorkerMaxJobsActive(50)
                .build();
         */
        zeebeClient.newWorker().jobType("explicit-email").handler(new EmailExplicitWorker())
                .streamEnabled(true).open();

        //var topology = zeebeClient.newTopologyRequest().send().join();
        // log.info("Topology created [{}]", topology);

        List<JobWorker> workers = new ArrayList<>();

        for (int i = 0; i <= 1000; i++) {
            String workerType = "simulate-" + i;
            workers.add(zeebeClient.newWorker().jobType(workerType)
                    .handler(new SimulateWorker(i))
                    .streamEnabled(true).open());
            log.info("open worker [{}]", workerType);
        }
        log.info("Start simulate-worker");
        final ProcessInstanceEvent processInstanceEvent =
                zeebeClient
                        .newCreateInstanceCommand()
                        .bpmnProcessId("multiple-worker")
                        .latestVersion()
                        .variables(Map.of("message_content", "Run simulateworker"))
                        .send()
                        .join();

    }
}
