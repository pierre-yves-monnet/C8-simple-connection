package io.camunda.getstarted;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailExplicitWorker implements JobHandler {
  private final static Logger logger = LoggerFactory.getLogger(EmailAnnotationWorker.class);

  @Override
  public void handle(JobClient client, ActivatedJob job) throws Exception {
    final String message_content = (String) job.getVariablesAsMap().get("message_content");
    logger.info(">>>>>>>>>>>>>>> Sending explicit-email with message content: {}", message_content);
    client.newCompleteCommand(job).send();
  }
}
