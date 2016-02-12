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
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.FileCleanerHolder;
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
   @Override
   public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
   {
      LOG.info("Execution Send Lecko DATA JOB");
      FileCleaner cleaner = null;
      FileCleanerHolder holder = getService(FileCleanerHolder.class);
      if (holder != null)
      {
         cleaner = holder.getFileCleaner();
      }
      DataBuilder builder = getService(SimpleDataBuilder.class);
      File file = null ;
      try
      {
         boolean success = false;
         if (builder != null)
         {
            success = builder.build();
         }
         if(success && file != null && file.exists())
         {
            SftpClient client =  new SftpClient() ;
            client.send(file.getAbsolutePath());
         }
         else
         {
            return;
         }

      }
      finally
      {
         if (file != null && file.exists())
         {
            cleaner.addFile(file);
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
