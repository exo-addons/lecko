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
package org.exoplatform.addons.lecko.social.client.rest.connector;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Sep
 * 29, 2015
 */
public class HttpUtils {
  private static Log LOG = ExoLogger.getLogger(HttpUtils.class);

  // GET
  public static String get(String url) throws Exception {

    LOG.debug("HTTPUtils : try to get url={}",url);
    HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
    connection.setRequestMethod("GET");
    connection.connect();

    int code = connection.getResponseCode();
    if (code > 300) {
      connection.disconnect();
      return null;
    }
    InputStream in = connection.getInputStream();
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    String line = null;
    StringBuilder builder = new StringBuilder();
    while ((line = reader.readLine()) != null) {
      builder.append(line).append("\n");
    }
    in.close();
    reader.close();
    connection.disconnect();
    return builder.toString();
  }

}
