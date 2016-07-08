package org.exoplatform.addons.lecko.job;

import org.exoplatform.addons.lecko.LeckoServiceController;
import org.exoplatform.container.ExoContainerContext;
import org.quartz.Job;
import org.quartz.JobDataMap;

import org.exoplatform.commons.utils.ExoProperties;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.CronJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 */
public class StartLeckoExportJob implements Job {

  private static final Log LOG = ExoLogger.getLogger(StartLeckoExportJob.class.getName());


  public StartLeckoExportJob() throws Exception {
  }

  public void execute(JobExecutionContext context) throws JobExecutionException {
    if (LOG.isInfoEnabled()) {
      LOG.info("Start StartLeckoExportJob");
    }

    LeckoServiceController leckoServiceController = (LeckoServiceController) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(LeckoServiceController.class);


    if  (leckoServiceController.getEnableLeckoJob()) {
      leckoServiceController.buildLeckoData();
    } else {
      LOG.info("Lecko is not enabled.");
    }

    if (LOG.isInfoEnabled()) {

      LOG.info("End StartLeckoExportJob");
    }
  }
}
