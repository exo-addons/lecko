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
package org.exoplatform.addons.lecko;

import org.exoplatform.addons.lecko.Utils.SftpClient;
import org.exoplatform.commons.utils.PrivilegedFileHelper;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;
import java.io.File;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 */
@Managed
@NameTemplate(@Property(key = "service", value = "LeckoServiceController"))
@ManagedDescription("Lecko management jobs")
public class LeckoServiceController implements Startable
{
   private static final Log LOG = ExoLogger.getLogger("org.exoplatform.addons.lecko.LeckoServiceController");
   private static final String rootPath = PropertyManager.getProperty("java.io.tmpdir")+"/lecko";
   private static final String LECKO_ENABLED = "exo.addons.lecko.job.enabled";
   private static final String LECKO_OUTPUT_NAME= "exo.addons.lecko.out.name";
   private static String path;

   public LeckoServiceController()
   {
      File directory = new File(this.rootPath );
      if (!PrivilegedFileHelper.exists(directory))
      {
         PrivilegedFileHelper.mkdirs(directory);
      }
      String  name = PropertyManager.getProperty(LECKO_OUTPUT_NAME);
      path = rootPath+"/"+((name != null && ! name.isEmpty()) ? name : "dump");
   }
   /**
    * Build dump data.
    */
   @Managed
   @ManagedDescription("Build lecko data.")
   public String buildLeckoData()
   {
      DataBuilder builder = getService(SimpleDataBuilder.class);
      try
      {
         builder.build();
      }
      catch (Exception ex)
      {
         LOG.error(ex.getMessage());
         return "Failed";
      }
      return "Success";
   }

   /**
    * Upload dump to lecko server.
    */
   @Managed
   @ManagedDescription("Upload data to lecko server.")
   public String UploadLeckoData()
   {
      boolean status = false;
      File file = new File(path);
      try
      {
         if(file.exists())
         {
            SftpClient client = new SftpClient();
            LOG.info("Stat send Data to lecko server");
            status =client.send(file.getAbsolutePath());
            if(status)
            {
               LOG.info("End  send Data to lecko server");
            }
            else
            {
               LOG.info("Failed send Data to lecko server");
            }
         }
         else
         {
            LOG.info("Failed send Data to lecko server file not exist : " + path);
            return "Failed";
         }
      }
      catch (Exception ex)
      {
         LOG.error("Failed send Data to lecko server : " +ex.getMessage());
         return "Failed";
      }
      finally
      {
         if (file != null && file.exists() && status)
         {
            file.delete();
         }
      }
      return status ?"Success":"Failed";
   }

   @Managed
   @ManagedDescription("Enable/Disable lecko job. ")
   public void enableLeckoJob(@ManagedName("enable") boolean enable)
   {
      PropertyManager.setProperty(LECKO_ENABLED,Boolean.toString(enable));
   }

   @Managed
   @ManagedDescription("Enable/Disable lecko job. ")
   public boolean getEnableLeckoJob()
   {
     return Boolean.parseBoolean(PropertyManager.getProperty(LECKO_ENABLED));
   }

   private  <T> T getService(Class<T> clazz) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      String containerName;
      if (container.getComponentInstanceOfType(clazz)==null) {
         containerName = PortalContainer.getCurrentPortalContainerName();
         container = RootContainer.getInstance().getPortalContainer(containerName);
      }
      return clazz.cast(container.getComponentInstanceOfType(clazz));
   }

   @Override
   public void start()
   {
   }

   @Override
   public void stop()
   {

   }
}
