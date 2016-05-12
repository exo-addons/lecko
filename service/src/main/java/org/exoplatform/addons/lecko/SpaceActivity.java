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

import java.io.PrintWriter;
import org.exoplatform.addons.lecko.social.client.rest.connector.ExoSocialConnector;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 */
public class SpaceActivity extends SocialActivity
{

   String spaceId;

   public SpaceActivity(String id, ExoSocialConnector exoSocialConnector)
   {
      this.spaceId = id;
      this.exoSocialConnector = exoSocialConnector;
   }
   @Override
   public void loadActivityStream(PrintWriter out) throws Exception
   {
      int offsetActivities = 0;
      int sizeActivities = 20;
      boolean hasNextActivity = true;

      String idEvent = "";
      String date = "";
      String idactor = "";
      String placeName = "";
      while (hasNextActivity)
      {
         //Get All activities by space id
         String activitiesJson = exoSocialConnector.getActivitiesBySpaceID(spaceId, offsetActivities, sizeActivities);
         JSONArray activitiesList;
         if (activitiesJson == null)
         {
            break;
         }
         else
         {
            activitiesList = parseJSONArray(activitiesJson, "activities");
         }

         if (activitiesList == null || activitiesList.size() == 0)
         {
            break;
         }
         else if (activitiesList.size() < sizeActivities)
         {
            hasNextActivity = false;
         }
         for (Object a : activitiesList)
         {
            JSONObject js = (JSONObject)a;
            placeName = "none";
            String type_space = "";
            String url_comments = "no_url";
            String url_likes = "no_url";

            idactor = (String)(((JSONObject)js.get("owner")).get("id"));

            //constuction de la map des users au fur et  mesure pour l'anonymisation
            if (!user_map.containsKey(idactor))
            {
               user_map.put(idactor, Integer.toString(user_map.size() + 1));
               idactor = user_map.get(idactor);
            }
            else
            {
               idactor = user_map.get(idactor);
            }
            out.print(idactor + ";");

            idEvent = (String)js.get("type");
            out.print(idEvent + ";");
            date = (String)js.get("createDate");
            out.print(date + ";");
            type_space = (String)(((JSONObject)js.get("activityStream")).get("type"));
            out.print(type_space + ";");
            placeName = (String)(((JSONObject)js.get("activityStream")).get("id"));
            if (type_space.equals("user"))
            {
               placeName = "";
            }
            out.print(placeName + ";");
            out.println();
            url_comments = (String)js.get("comments");
            url_likes = (String)js.get("likes");

            //Getting Comments
            getExoComments(url_comments, placeName, out);
            //Getting Likes
            getLikes(url_likes, date, placeName, out);
         }
         offsetActivities += sizeActivities;
         out.flush();
      }
   }


}
