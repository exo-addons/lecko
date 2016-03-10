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

import org.exoplatform.addons.lecko.social.client.rest.connector.ExoSocialConnector;
import org.exoplatform.commons.utils.PrivilegedFileHelper;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * Created by The eXo Platform SAS.
 *
 * Author : Simon
 * 12.02.2016
 *
 * @version $Id$
 */
public class SimpleDataBuilder implements DataBuilder
{
   private static Log LOG = ExoLogger.getLogger(SimpleDataBuilder.class);

   private ExoSocialConnector exoSocialConnector;

   private final String leckoTempDirectory;

   public SimpleDataBuilder(ExoSocialConnector exoSocialConnector)
   {
      this.exoSocialConnector = exoSocialConnector;
      this.leckoTempDirectory = PropertyManager.getProperty("java.io.tmpdir") + "/lecko";
      File directory = new File(this.leckoTempDirectory );
      if (!PrivilegedFileHelper.exists(directory))
      {
         PrivilegedFileHelper.mkdirs(directory);
      }
   }

   @Override
   public boolean build()
   {
      try
      {
         String extractOutput = leckoTempDirectory + "/exo-community.txt";
         File output = new File(extractOutput );
         if (!PrivilegedFileHelper.exists(output))
         {
            PrivilegedFileHelper.delete(output);
         }

         PrintWriter out = null;

         String idEvent = "";
         String date = "";
         String idactor = "";
         String placeName = "";

         //anonymization MAP
         HashMap<String, String> user_map = new HashMap<String, String>();

         try
         {
            out = new PrintWriter(new FileWriter(extractOutput));
         }
         catch (IOException exp)
         {
            LOG.error(exp.getMessage());
         }
         //Get All spaces
         if (LOG.isDebugEnabled())
         {
            LOG.debug("Lecko-Addons : Beginning Extraction");
         }

         int offset = 0;
         int size = 20;
         boolean hasNextSpace = true;

         while (hasNextSpace)
         {
            //Extract all spaces by limit
            String json = exoSocialConnector.getSpaces(offset, size);
            JSONArray spaceList = null;

            if (json == null)
            {
               break;
            }
            else
            {
               spaceList = parseJSONArray(json, "spaces");
            }

            if (spaceList == null || spaceList.size() == 0)
            {
               break;
            }
            else if (spaceList.size() < size)
            {
               hasNextSpace = false;
            }

            //Extract all activities by space ID
            for (Object obj : spaceList)
            {
               int offsetActivities = 0;
               int sizeActivities = 20;
               JSONObject jsonObject = (JSONObject)obj;
               //space ID
               String spaceId = (String)jsonObject.get("id");

               if (LOG.isDebugEnabled())
               {
                  LOG.debug("Extract Data space :" + jsonObject.get("groupId"));
               }

               boolean hasNextActivity = true;

               try
               {
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
                        getExoComments(url_comments, placeName, out, user_map);
                        //Getting Likes
                        //***************************TODO Review
                        getLikes(url_likes, date, placeName, out, user_map);
                     }
                     offsetActivities += sizeActivities;
                     out.flush();
                  }


               }
               catch (NumberFormatException | JSONException ex)
               {
                  LOG.error(ex.getMessage());
               }
            }
            offset += size;
            out.flush();
         }
         if (LOG.isDebugEnabled())
         {
            LOG.debug("Lecko-Addons : Ending Extraction");
         }
      }
      catch (Exception ex)
      {
         LOG.error(ex.getMessage());
         return false;
      }
      return true;
   }

   private JSONArray parseJSONArray(String json, String entry) throws ParseException, JSONException
   {

      JSONParser parser = new JSONParser();

      Object obj = parser.parse(json);

      JSONObject jsonObject = (JSONObject)obj;

      JSONArray listEntry = (JSONArray)jsonObject.get(entry);

      return listEntry;

   }

   private void getExoComments(String url, String placeName, PrintWriter out, HashMap<String, String> user_map)
   {

      if (LOG.isDebugEnabled())
      {
         LOG.debug("Getting Comments : " + placeName);
      }
      String result;
      String idEvent = "";
      String date = "";
      String idactor = "";
      int offset = 0;
      int limit = 20;
      boolean hasNextComments = true;

      try
      {
         while (hasNextComments)
         {
            result = exoSocialConnector.getActivityComments(url, offset, limit);
            JSONArray jsonComments;
            if (result == null)
            {
               break;
            }
            else
            {
               jsonComments = parseJSONArray(result, "comments");
            }

            if (jsonComments == null || jsonComments.size() == 0)
            {
               break;
            }
            else if (jsonComments.size() < limit)
            {
               hasNextComments = false;
            }

            for (Object obj : jsonComments)
            {
               JSONObject js = (JSONObject)obj;
               idactor = ((String)js.get("identity")).split("/")[7];
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

               idEvent = "comment";
               out.print(idEvent + ";");
               date = (String)js.get("createDate");
               out.print(date + ";");
               out.print(placeName + ";");
               out.println();
            }
            offset += limit;
            out.flush();
         }

         if (LOG.isDebugEnabled())
         {
            LOG.debug("End Getting Comments : " + placeName);
         }
      }
      catch (Exception ex)
      {
         LOG.error(ex.getMessage());
      }
   }

   private void getLikes(String url, String date, String placeName, PrintWriter out, HashMap<String, String> user_map)
   {

      if (LOG.isDebugEnabled())
      {
         LOG.debug("Getting Likes : " + placeName);
      }
      String result;
      String idEvent = "";
      String idactor = "";
      int offset = 0;
      int limit = 20;
      boolean hasNextLikes = true;

      try
      {
         while (hasNextLikes)
         {
            result = exoSocialConnector.getActivityLikes(url, offset, limit);
            JSONArray jsonLikes;
            if (result == null)
            {
               break;
            }
            else
            {
               jsonLikes = parseJSONArray(result, "likes");
            }

            if (jsonLikes == null || jsonLikes.size() == 0)
            {
               break;
            }
            else if (jsonLikes.size() < limit)
            {
               hasNextLikes = false;
            }

            for (Object obj : jsonLikes)
            {
               JSONObject js = (JSONObject)obj;
               idactor = ((String)js.get("identity")).split("/")[7];
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

               idEvent = "like";
               out.print(idEvent + ";");
               out.print(date + ";");
               out.print(placeName + ";");
               out.println();
            }
            offset += limit;
            out.flush();
         }

         if (LOG.isDebugEnabled())
         {
            LOG.debug("End Getting Likes : " + placeName);
         }

      }
      catch (Exception ex)
      {
         LOG.error(ex.getMessage());
      }
   }

}
