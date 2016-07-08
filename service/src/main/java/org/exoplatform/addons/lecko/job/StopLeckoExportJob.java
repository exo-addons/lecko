package org.exoplatform.addons.lecko.job;

import org.exoplatform.addons.lecko.LeckoServiceController;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.CronJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 */
public class StopLeckoExportJob implements Job {

  private static final Log LOG = ExoLogger.getLogger(StopLeckoExportJob.class.getName());


  public StopLeckoExportJob() throws Exception {
  }

  public void execute(JobExecutionContext context) throws JobExecutionException {
    if (LOG.isInfoEnabled()) {
      LOG.info("Start StopLeckoExportJob");
    }
    LeckoServiceController leckoServiceController = (LeckoServiceController) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(LeckoServiceController.class);


    if  (leckoServiceController.getEnableLeckoJob()) {
      leckoServiceController.stopLeckoExport();
    } else {
      LOG.info("Lecko is not enabled.");
    }

    if (LOG.isInfoEnabled()) {

      LOG.info("End StopLeckoExportJob");
    }
  }
}
