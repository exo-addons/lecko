package org.exoplatform.addons.lecko.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.exoplatform.addons.lecko.LeckoServiceController;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 */
public class UploadAnResetLeckoDataJob implements Job {

  private static final Log LOG = ExoLogger.getLogger(UploadAnResetLeckoDataJob.class.getName());


  public UploadAnResetLeckoDataJob() throws Exception {
  }

  public void execute(JobExecutionContext context) throws JobExecutionException {
    if (LOG.isInfoEnabled()) {
      LOG.info("Start UploadAnResetLeckoDataJob");
    }

    LeckoServiceController leckoServiceController = (LeckoServiceController) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(LeckoServiceController.class);


    if  (leckoServiceController.getEnableLeckoJob()) {

      String result = leckoServiceController.UploadLeckoData();
      LOG.info(result);
    } else {
      LOG.info("Lecko is not enabled.");
    }

    if (LOG.isInfoEnabled()) {

      LOG.info("End UploadAnResetLeckoDataJob");
    }
  }
}
