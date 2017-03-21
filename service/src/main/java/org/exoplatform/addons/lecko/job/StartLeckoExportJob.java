/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
 *
 * This file is part of Lecko Analytics Add-on - Service.
 *
 * Lecko Analytics Add-on - Service is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * Lecko Analytics Add-on - Service software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Lecko Analytics Add-on - Service; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see <http://www.gnu.org/licenses/>.
 */
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

    LeckoServiceController leckoServiceController =
                                                  (LeckoServiceController) ExoContainerContext.getCurrentContainer()
                                                                                              .getComponentInstanceOfType(LeckoServiceController.class);

    if (leckoServiceController.getEnableLeckoJob()) {
      leckoServiceController.buildLeckoData();
    } else {
      LOG.info("Lecko is not enabled.");
    }

    if (LOG.isInfoEnabled()) {

      LOG.info("End StartLeckoExportJob");
    }
  }
}
