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

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

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

  private String              host;

  private String              user;

  private String              pwd;

  private int                 port                = DEFAULT_PORT;

  private boolean             active;

  private String              remotePath;

  private String              proxyAdress;

  private int                 proxyPort;

  private static int          DEFAULT_TIMOUT      = 3600000;

  private boolean             isConfigured        = true;

  public SftpClient() {

    // Mandatory properties
    host = PropertyManager.getProperty(LECKO_HOST);
    if (host != null) {
      host = host.trim();
    } else {
      LOG.error("Property " + LECKO_HOST + " undefined, please define it in the exo.properties file");
      isConfigured = false;
    }

    user = PropertyManager.getProperty(LECKO_USER);
    if (user != null) {
      user = user.trim();
    } else {
      LOG.error("Property " + LECKO_USER + " undefined, please define it in the exo.properties file");
      isConfigured = false;
    }

    pwd = PropertyManager.getProperty(LECKO_PASSWORD);
    if (pwd != null) {
      pwd = pwd.trim();
    } else {
      LOG.error("Property " + LECKO_PASSWORD + " undefined, please define it in the exo.properties file");
      isConfigured = false;
    }

    if (!isConfigured) {
      LOG.error("Missing mandatory properties, SFTP Client is not configured ... so the Lecko dump file will not be send to the FTP server.");
      return;
    }

    // Optionals properties
    remotePath = PropertyManager.getProperty(LECKO_REMOTE_PATH);
    if (remotePath != null) {
      remotePath = remotePath.trim();
    }

    String value = PropertyManager.getProperty(LECKO_PORT);
    if (value != null) {
      try {
        port = Integer.valueOf(value);
      } catch (NumberFormatException ex) {
        LOG.error("Property " + LECKO_PORT + " is invalid, using default port: " + DEFAULT_PORT);
        port = DEFAULT_PORT;
      }
    }

    // Manage proxy configuration -> Not use for the moment
    value = PropertyManager.getProperty(LECKO_ACTIVE_PROXY);
    active = Boolean.valueOf(value);

    if (active) {
      proxyAdress = PropertyManager.getProperty(LECKO_PROXY_ADDRESS);
      if (proxyAdress != null) {
        proxyAdress = proxyAdress.trim();
      } else {
        LOG.error("Mode proxy is enable and property " + LECKO_PROXY_ADDRESS + " undefined");
      }

      String proxyPortString = PropertyManager.getProperty(LECKO_PROXY_PORT);
      if (proxyPortString != null) {
        try {
          proxyPort = Integer.valueOf(proxyPortString);
        } catch (NumberFormatException ex) {
          LOG.error("Mode proxy is enable and property " + LECKO_PROXY_PORT + " is invalid");
        }
      } else {
        LOG.error("Mode proxy is enable and property " + LECKO_PROXY_PORT + " undefined");
      }
    }
  }

  public boolean send(String fileName) {
    Session session = null;
    Channel channel = null;
    ChannelSftp channelSftp = null;

    if (isConfigured) {
      try {
        JSch jsch = new JSch();
        LOG.info("Opening a session on the sftp server host={} port={} user={}", host, port, user);
        session = jsch.getSession(user, host, port);

        if (session != null) {
          session.setPassword(pwd);
          session.setTimeout(DEFAULT_TIMOUT);
        } else {
          LOG.error("Unable to retrieve an sftp session");
          return false;
        }

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        LOG.info("Opening an sftp channel");
        channel = session.openChannel("sftp");
        if (channel != null) {
          channel.connect(DEFAULT_TIMOUT);
        } else {
          LOG.error("Unable to connect to the sftp server");
          return false;
        }
        channelSftp = (ChannelSftp) channel;
        if (remotePath != null) {
          LOG.info("Changing the remote directory to {}", remotePath);
          channelSftp.cd(remotePath);
        }

        LOG.info("Transfering {}", fileName);
        File f = new File(fileName);
        channelSftp.put(new FileInputStream(f), f.getName());

        LOG.info("Transfer of {} on the sftp server done", fileName);
        return true;
      } catch (Exception ex) {
        LOG.error("Unable to transfer " + fileName, ex);
        try {
          Thread.sleep(30000L);
        } catch (InterruptedException e) {
          LOG.error(e);
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
    } else {
      LOG.warn(" SFTP Client is not configured, Unable to transfer Lecko dump file: " + fileName);
      return false;
    }
  }
}
