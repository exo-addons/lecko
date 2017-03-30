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
package org.exoplatform.lecko.social;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

import org.exoplatform.addons.lecko.social.client.rest.connector.ExoSocialConnector;
import org.exoplatform.addons.lecko.social.client.rest.connector.ServiceInfo;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Sep
 * 29, 2015
 */
public class ExoSocialMockConnectorImpl implements ExoSocialConnector {

  private static final Log log            = ExoLogger.getLogger(ExoSocialMockConnectorImpl.class);

  private String           userName;

  private String           password;

  private String           baseUrl;

  private String           defaultBaseUrl = "";

  public ExoSocialMockConnectorImpl(InitParams initParams) {
    userName = initParams.getValueParam("username").getValue();

    password = initParams.getValueParam("password").getValue();

    baseUrl = initParams.getValueParam("baseUrl").getValue();

    if (userName == null || userName.length() == 0) {
      log.warn("Property 'userName' needs to be provided.");
    }

    if (baseUrl == null || baseUrl.length() == 0) {
      baseUrl = defaultBaseUrl;
    }

    Authenticator.setDefault(new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(userName, password.toCharArray());
      }
    });

  }

  public String getUserById(String username) throws Exception {
    String url = baseUrl + ServiceInfo.getUserUri(username);
    String json = HttpUtilsMock.get(url);
    return json;
  }

  @Override
  public String getSpaces(int offset, int limit) throws Exception {
    String url = baseUrl + ServiceInfo.getSpacesUri(offset, limit);
    String json = HttpUtilsMock.get(url);
    return json;
  }

  @Override
  public String getUsers(int offset, int limit) throws Exception {
    String url = baseUrl + ServiceInfo.getUsersUri(offset, limit);
    String json = HttpUtilsMock.get(url);
    return json;
  }

  @Override
  public String getActivitiesBySpaceID(String id, int offset, int limit) throws Exception {
    String url = baseUrl + ServiceInfo.getSpaceActivities(id, offset, limit);
    String json = HttpUtilsMock.get(url);
    return json;
  }

  @Override
  public String getActivitiesByUserID(String id, int offset, int limit) throws Exception {
    String url = baseUrl + ServiceInfo.getUserActivities(id, offset, limit);
    String json = HttpUtilsMock.get(url);
    return json;
  }

  @Override
  public String getActivityComments(String url) throws Exception {
    String newUrl = ServiceInfo.getActivityData(url);
    String json = HttpUtilsMock.get(newUrl);
    return json;
  }

  @Override
  public String getActivityLikes(String url) throws Exception {
    String newUrl = ServiceInfo.getActivityData(url);
    String json = HttpUtilsMock.get(newUrl);
    return json;
  }

}
