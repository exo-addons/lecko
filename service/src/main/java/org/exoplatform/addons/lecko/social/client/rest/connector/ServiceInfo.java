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
public class ServiceInfo {
  
  public static final String REST_URL = "/rest/private/v1/social";
  
  //User
  public static String getUsersUri(int offset, int limit) {
    return REST_URL + "/users?offset="+offset+"&limit="+limit;
  }
  public static String getUserUri(String username) {
    return REST_URL + "/users/" + username; 
  }
  public static String getUserConnectionsUri(String username) {
    return REST_URL + "/users/" + username + "/connections";
  }
  public static String getUserSpacesUri(String username) {
    return REST_URL + "/users/" + username + "/spaces";
  }
  public static String getUserActivitiesUri(String username) {
    return REST_URL + "/users/" + username + "/activities";
  }
  
  //UserRelationship
  public static String getUserRelationshipsUri() {
    return REST_URL + "/usersRelationships";
  }
  public static String getUserRelationshipUri(String id) {
    return REST_URL + "/usersRelationships/" + id; 
  }
  
  //Get Spaces
  public static String getSpacesUri(int offset, int limit) {
    return REST_URL + "/spaces?offset="+offset+"&limit="+limit;
  }
  
  //SpaceMembership
  public static String getSpaceMembershipsUri() {
    return REST_URL + "/spacesMemberships";
  }
  
  //Activity
  public static String getLikesUri(String activity_id) {
    return REST_URL + "/activities/" + activity_id + "/likes";
  }
  public static String getCommentsUri(String activity_id) {
    return REST_URL + "/activities/" + activity_id + "/comments";
  }

  public static String getSpaceActivities(String id, int offset, int limit) {
    return REST_URL + "/spaces/" + id + "/activities?offset="+offset+"&limit="+limit;
  }

  public static String getUserActivities(String id, int offset, int limit) {
    return REST_URL + "/users/" + id + "/activities?type=owner&offset="+offset+"&limit="+limit;
  }


  public static String getActivityData(String url, int offset, int limit){
    return url.replace("rest","rest/private")+"?offset="+offset+"&limit="+limit;
  }

}
