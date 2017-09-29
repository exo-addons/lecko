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
public class SftpClient {
  private static Log          LOG                 = ExoLogger.getLogger(SftpClient.class);

  private static final int    DEFAULT_PORT        = 22;

  private static final String LECKO_HOST          = "exo.addons.lecko.SftpHost";

  private static final String LECKO_USER          = "exo.addons.lecko.SftpUser";

  private static final String LECKO_PASSWORD      = "exo.addons.lecko.SftpPassword";

  private static final String LECKO_PORT          = "exo.addons.lecko.SftPortNumber";

  private static final String LECKO_ACTIVE_PROXY  = "exo.addons.lecko.SftpActiveProxy";

  private static final String LECKO_PROXY_ADDRESS = "exo.addons.leckoSftp.ProxyAddress";

  private static final String LECKO_PROXY_PORT    = "exo.addons.lecko.SftpProxyPort";

  private static final String LECKO_REMOTE_PATH   = "exo.addons.lecko.SftpRemotePath";

  private static String       host;

  private static String       user;

  private static String       pwd;

  private static int          port                = DEFAULT_PORT;

  private static boolean      active;

  private static String       remotePpath;

  private static String       proxyAdress;

  private static String       proxyPort;

  private static int          DEFAULT_TIMOUT      = 3600000;

  static {

    host = PropertyManager.getProperty(LECKO_HOST).trim();
    user = PropertyManager.getProperty(LECKO_USER).trim();
    pwd = PropertyManager.getProperty(LECKO_PASSWORD);
    remotePpath = PropertyManager.getProperty(LECKO_REMOTE_PATH).trim();

    String value = PropertyManager.getProperty(LECKO_PORT);
    if (value != null) {
      try {
        port = Integer.valueOf(value);
      } catch (NumberFormatException ex) {
        port = DEFAULT_PORT;
      }
    }

    value = PropertyManager.getProperty(LECKO_ACTIVE_PROXY);
    active = Boolean.valueOf(value);

    if (active) {
      proxyAdress = PropertyManager.getProperty(LECKO_PROXY_ADDRESS);
      proxyPort = PropertyManager.getProperty(LECKO_PROXY_PORT);
    }

  }

  public boolean send(String fileName) {
    Session session = null;
    Channel channel = null;
    ChannelSftp channelSftp = null;

    try {
      JSch jsch = new JSch();
      session = jsch.getSession(user, host, port);

      if (session != null) {
        session.setPassword(pwd);
        session.setTimeout(DEFAULT_TIMOUT);
      } else {
        return false;
      }

      Properties config = new Properties();
      config.put("StrictHostKeyChecking", "no");
      session.setConfig(config);
      session.connect();
      channel = session.openChannel("sftp");
      if (channel != null) {
        channel.connect(DEFAULT_TIMOUT);
      } else {
        return false;
      }
      channelSftp = (ChannelSftp) channel;
      if (remotePpath != null) {
        channelSftp.cd(remotePpath);
      }

      File f = new File(fileName);
      channelSftp.put(new FileInputStream(f), f.getName());
      return true;
    } catch (Exception ex) {
      LOG.error(ex.getMessage());
      try {
        Thread.sleep(30000L);
      } catch (InterruptedException e) {
        LOG.error(e.getMessage());
      }
      return false;
    } finally {
      if (channelSftp != null) {
        channelSftp.exit();
      }
      if (channel != null) {
        channel.disconnect();
      }
      if (session != null) {
        session.disconnect();
      }
    }

  }
}
