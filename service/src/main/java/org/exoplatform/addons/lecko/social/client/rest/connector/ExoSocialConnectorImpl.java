/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.addons.lecko.social.client.rest.connector;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Sep
 * 29, 2015
 */
public class ExoSocialConnectorImpl implements ExoSocialConnector
{

   private static final Log log = ExoLogger.getLogger(ExoSocialConnectorImpl.class);

   private String userName;

   private String password;

   private String baseUrl;

   private String defaultBaseUrl = "http://localhost:8080";


   public ExoSocialConnectorImpl(InitParams initParams)
   {
      userName = initParams.getValueParam("username").getValue();

      password = initParams.getValueParam("password").getValue();

      baseUrl = initParams.getValueParam("baseUrl").getValue();

      if (userName == null || userName.length() == 0)
      {
         log.warn("Property 'userName' needs to be provided.");
      }

      if (baseUrl == null || baseUrl.length() == 0)
      {
         baseUrl = defaultBaseUrl;
      }


      Authenticator.setDefault(new Authenticator()
      {
         @Override
         protected PasswordAuthentication getPasswordAuthentication()
         {
            return new PasswordAuthentication(userName, password.toCharArray());
         }
      });

   }

   public String getUserById(String username) throws Exception
   {
      String url = baseUrl + ServiceInfo.getUserUri(username);
      String json = HttpUtils.get(url);
      return json;
   }

   @Override
   public String getSpaces(int offset, int limit) throws Exception
   {
      String url = baseUrl + ServiceInfo.getSpacesUri(offset, limit);
      String json = HttpUtils.get(url);
      return json;
   }

   @Override
   public String getUsers(int offset, int limit) throws Exception
   {
      String url = baseUrl + ServiceInfo.getUsersUri(offset, limit);
      String json = HttpUtils.get(url);
      return json;
   }

   @Override
   public String getActivitiesBySpaceID(String id, int offset, int limit) throws Exception
   {
      String url = baseUrl + ServiceInfo.getSpaceActivities(id, offset, limit);
      String json = HttpUtils.get(url);
      return json;
   }

   @Override
   public String getActivitiesByUserID(String id, int offset, int limit) throws Exception
   {
      String url = baseUrl + ServiceInfo.getUserActivities(id, offset, limit);
      String json = HttpUtils.get(url);
      return json;
   }

   @Override
   public String getActivityComments(String url, int offset, int limit) throws Exception
   {
      String newUrl = ServiceInfo.getActivityData(url, offset, limit);
      String json = HttpUtils.get(newUrl);
      return json;
   }

   @Override
   public String getActivityLikes(String url, int offset, int limit) throws Exception
   {
      String newUrl = ServiceInfo.getActivityData(url, offset, limit);
      String json = HttpUtils.get(newUrl);
      return json;
   }

}
