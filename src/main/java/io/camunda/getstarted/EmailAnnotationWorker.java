package io.camunda.getstarted;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailAnnotationWorker {

  private final static Logger logger = LoggerFactory.getLogger(EmailAnnotationWorker.class);
  // pollInterval = 100,
  @JobWorker(type = "annotation-email", maxJobsActive = 50, pollInterval = 10000, streamEnabled = false)
  public void sendEmail(final ActivatedJob job) {
    final String message_content = (String) job.getVariablesAsMap().get("message_content");
    logger.info(">>>>>>>>>>>>>>> Sending annotation-email with message content: {}", message_content);
  }


}
