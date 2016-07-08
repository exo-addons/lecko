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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.exoplatform.addons.lecko.social.client.rest.connector.ExoSocialConnector;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 */
abstract class SocialActivity
{
   protected static Log LOG = ExoLogger.getLogger(SimpleDataBuilder.class);

   //anonymization MAP
   protected static Map<String, String> user_map = new ConcurrentHashMap<String, String>();

   ExoSocialConnector exoSocialConnector;

   public abstract void loadActivityStream(PrintWriter out) throws Exception;

   public static JSONArray parseJSONArray(String json, String entry) throws ParseException, JSONException
   {

      JSONParser parser = new JSONParser();

      Object obj = parser.parse(json);

      JSONObject jsonObject = (JSONObject)obj;

      JSONArray listEntry = (JSONArray)jsonObject.get(entry);

      return listEntry;

   }

   protected void getExoComments(String url, String placeName, PrintWriter out) throws Exception
   {

      if (LOG.isDebugEnabled())
      {
         LOG.debug("Getting Comments : " + placeName);
      }
      String result;
      String idEvent = "";
      String date = "";
      String idactor = "";
      result = exoSocialConnector.getActivityComments(url);
      JSONArray jsonComments;
      if (result == null)
      {
         return;
      }
      else
      {
         jsonComments = parseJSONArray(result, "comments");
      }

      if (jsonComments == null || jsonComments.size() == 0)
      {
         return;
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
      out.flush();


      if (LOG.isDebugEnabled())
      {
         LOG.debug("End Getting Comments : " + placeName);
      }

   }

   protected void getLikes(String url, String date, String placeName, PrintWriter out) throws Exception
   {
      if (LOG.isDebugEnabled())
      {
         LOG.debug("Getting Likes : " + placeName);
      }
      String result;
      String idEvent = "";
      String idactor = "";

      result = exoSocialConnector.getActivityLikes(url);
      JSONArray jsonLikes;
      if (result == null)
      {
         return;
      }
      else
      {
         jsonLikes = parseJSONArray(result, "likes");
      }

      if (jsonLikes == null || jsonLikes.size() == 0)
      {
         return;
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
      out.flush();

      if (LOG.isDebugEnabled())
      {
         LOG.debug("End Getting Likes : " + placeName);
      }
   }
}
