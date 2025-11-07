package io.camunda.getstarted;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.command.TopologyRequestStep1;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.gateway.protocol.GatewayOuterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ManualApplication {


    private static final Logger log = LoggerFactory.getLogger(ManualApplication.class);
    private static final CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) {
        try {
            ZeebeClient zeebeClient = ZeebeClient.newClientBuilder()
                    .preferRestOverGrpc(false)
                    .grpcAddress(URI.create("http://localhost:26500"))
                    .restAddress(URI.create("http://localhost:8088"))
                    .numJobWorkerExecutionThreads(20)
                    .defaultJobWorkerMaxJobsActive(50)
                    .build();

            var topology = zeebeClient.newTopologyRequest().send().join();
            log.info("Topology created [{}]", topology);

            List<JobWorker> workers = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                String workerType = "simulate-" + i;
                log.info("open worker [{}]", workerType);
                workers.add(zeebeClient.newWorker()
                        .jobType(workerType)
                        .handler(new EmailExplicitWorker())
                        .streamEnabled(true)
                        .open());

            }

            // Handle Ctrl+C or SIGTERM gracefully
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutdown signal received. Stopping application...");
                latch.countDown();
            }));

            // Block until shutdown signal is received
            log.info("wait for end-signal");
            latch.await();

            for (JobWorker worker : workers) {
                worker.close();
            }
            log.info("Application stopped cleanly.");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
