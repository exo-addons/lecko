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
import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Created by The eXo Platform SAS.
 *
 * Author : Aymen Boughzela .aboughzela@exoplatform.com
 * 12.02.2016
 *
 * @version $Id$
 */
public class SimpleDataBuilder implements DataBuilder
{
   ExoSocialConnector exoSocialConnector;

   public SimpleDataBuilder(ExoSocialConnector exoSocialConnector)
   {
      this.exoSocialConnector = exoSocialConnector;
   }

   @Override
   public boolean build()
   {
      try
      {
         //Get All spaces
         String json = exoSocialConnector.getSpaces();

         JSONArray spaceList = parseJSONArray(json, "spaces");

         for (Object obj : spaceList)
         {
            JSONObject jsonObject = (JSONObject)obj;
            String spaceId = (String)jsonObject.get("id");

            //Get All activities by space id
            String activitiesJson = exoSocialConnector.getActivitiesBySpaceID(spaceId);
         }


      }
      catch (Exception e)
      {
         e.printStackTrace();
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
}
