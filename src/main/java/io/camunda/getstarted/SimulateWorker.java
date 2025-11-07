package io.camunda.getstarted;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulateWorker  implements JobHandler {
    private final static Logger logger = LoggerFactory.getLogger(SimulateWorker.class);

    private int signature;
    public SimulateWorker(int signature) {
        this.signature = signature;
    }
    @Override
    public void handle(JobClient client, ActivatedJob job) throws Exception {
        logger.info(">>>>>>>>>>>>>>> SimulateWorker : "+signature);
        try {
            client.newCompleteCommand(job).send().join();
        } catch (Exception e) {
            logger.error("During complete Message", e);
        }
    }
}