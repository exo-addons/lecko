/*
 * Copyright (C) 2016 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.addons.lecko.job;

import org.exoplatform.addons.lecko.DataBuilder;
import org.exoplatform.addons.lecko.SimpleDataBuilder;
import org.exoplatform.addons.lecko.Utils.SftpClient;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.listener.Asynchronous;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import java.io.File;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 */
public class LeckoJob implements Job
{
   private static Log LOG = ExoLogger.getLogger(LeckoJob.class);
   private static final String LECKO_ENABLED = "exo.addons.lecko.job.enabled";
   private static final String path = PropertyManager.getProperty("java.io.tmpdir") + "/lecko/exo-community.txt";

   @Override
   public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
   {
      String value = PropertyManager.getProperty(LECKO_ENABLED);
      boolean enable = Boolean.valueOf(value);
      if(enable)
      {
         LOG.info("Execution Send Lecko DATA JOB");

         DataBuilder builder = getService(SimpleDataBuilder.class);
         File file = new File(path);
         boolean success = false;
         try
         {
            if (builder != null)
            {
               success = builder.build();
            }
            if (success && file != null && file.exists())
            {
               SftpClient client = new SftpClient();
               LOG.info("Start Send Lecko data to SFTP server");
               success=client.send(file.getAbsolutePath());
               LOG.info("end Send Lecko data to SFTP server");
            }
            else
            {
               LOG.error("Lecko extraction data not exist");
               return;
            }

         }
         catch (Exception ex)
         {
            LOG.error("Failed send Data"+ex.getMessage());
         }
         finally
         {
            if (file != null && file.exists() && success)
            {
               file.delete();
            }
         }
      }
      else
      {
         if (LOG.isDebugEnabled())
         {
            LOG.debug("Lecko extraction disabled");
         }
      }
   }

   /**
    * Gets the service.
    *
    * @param clazz the class
    *
    * @return the service
    */
   private  <T> T getService(Class<T> clazz) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      String containerName;
      if (container.getComponentInstanceOfType(clazz)==null) {
         containerName = PortalContainer.getCurrentPortalContainerName();
         container = RootContainer.getInstance().getPortalContainer(containerName);
      }
      return clazz.cast(container.getComponentInstanceOfType(clazz));
   }
}
