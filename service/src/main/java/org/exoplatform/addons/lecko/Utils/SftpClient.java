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
package org.exoplatform.addons.lecko.Utils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;


/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com

 */
public class SftpClient
{
   private static Log LOG = ExoLogger.getLogger(SftpClient.class);

   private static final int DEFAULT_PORT = 20;


   private static final String LECKO_HOST = "exo.addons.lecko.SftpHost";
   private static final String LECKO_USER = "exo.addons.leckoSftpUser";
   private static final String LECKO_PASSWORD = "exo.addons.leckoSftpPassword";
   private static final String LECKO_PORT = "exo.addons.leckoSftPortNumber";
   private static final String LECKO_ACTIVE_PROXY = "exo.addons.leckoSftpActiveProxy";
   private static final String LECKO_PROXY_ADDRESS = "exo.addons.leckoSftpProxyAddress";
   private static final String LECKO_PROXY_PORT = "exo.addons.leckoSftpProxyPort";
   private static final String LECKO_REMOTE_PATH = "exo.addons.leckoSftpRemotePath";


   private static String host;
   private static String user;
   private static String pwd;
   private static int port = DEFAULT_PORT;
   private static boolean active;
   private static String remotePpath;
   private static String proxyAdress;
   private static String proxyPort;


   static
   {

      host = PropertyManager.getProperty(LECKO_HOST);
      user = PropertyManager.getProperty(LECKO_USER);
      pwd = PropertyManager.getProperty(LECKO_PASSWORD);
      remotePpath= PropertyManager.getProperty(LECKO_REMOTE_PATH);

      String value = PropertyManager.getProperty(LECKO_PORT);
      if (value != null)
      {
         try
         {
            port = Integer.valueOf(value);
         }
         catch (NumberFormatException ex)
         {
            port = DEFAULT_PORT;
         }
      }

      value = PropertyManager.getProperty(LECKO_ACTIVE_PROXY);
      active = Boolean.valueOf(value);

      if (active)
      {
         proxyAdress = PropertyManager.getProperty(LECKO_PROXY_ADDRESS);
         proxyPort = PropertyManager.getProperty(LECKO_PROXY_PORT);
      }

   }

   public void send(String fileName)
   {
      Session session = null;
      Channel channel = null;
      ChannelSftp channelSftp = null;

      try
      {
         JSch jsch = new JSch();
         session = jsch.getSession(user, host, port);

         session.setPassword(pwd);

         Properties config = new Properties();
         config.put("StrictHostKeyChecking", "no");
         session.setConfig(config);
         session.connect();
         channel = session.openChannel("sftp");
         channel.connect();
         channelSftp = (ChannelSftp)channel;
         if(remotePpath != null)
         {
            channelSftp.cd(remotePpath);
         }

         File f = new File(fileName);
         channelSftp.put(new FileInputStream(f), f.getName());
      }
      catch (Exception ex)
      {
         LOG.error(ex.getMessage());
         try
         {
            Thread.sleep(30000L);
         }
         catch (InterruptedException e)
         {
            LOG.error(e.getMessage());
         }
      }
      finally
      {
         channelSftp.exit();
         channel.disconnect();
         session.disconnect();
      }

   }
}
