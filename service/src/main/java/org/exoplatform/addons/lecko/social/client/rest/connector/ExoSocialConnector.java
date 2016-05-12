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


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 29, 2015  
 */
public interface ExoSocialConnector
{
  
  //User
  public String getUserById(String username) throws Exception;


  public String getSpaces(int offset, int limit) throws Exception;

  public String getUsers(int offset, int limit) throws Exception;

  public String getActivitiesBySpaceID(String ID, int offset, int limit) throws Exception;

  public String getActivitiesByUserID(String ID, int offset, int limit) throws Exception;

  public String getActivityComments(String url , int offset, int limit) throws Exception;

  public String getActivityLikes(String url , int offset, int limit) throws Exception;


}
