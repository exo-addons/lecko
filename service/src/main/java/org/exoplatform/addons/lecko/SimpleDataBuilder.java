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

   private final String leckoOutputName;

   private static final String LECKO_OUTPUT_NAME= "exo.addons.lecko.out.name";

   public SimpleDataBuilder(ExoSocialConnector exoSocialConnector)
   {
      this.exoSocialConnector = exoSocialConnector;
      this.leckoTempDirectory = PropertyManager.getProperty("java.io.tmpdir") + "/lecko";
      File directory = new File(this.leckoTempDirectory );
      if (!PrivilegedFileHelper.exists(directory))
      {
         PrivilegedFileHelper.mkdirs(directory);
      }
      String  name = PropertyManager.getProperty(LECKO_OUTPUT_NAME);
      leckoOutputName = (name != null && ! name.isEmpty()) ? name : "dump";

   }

   @Override
   public boolean build()
   {
      PrintWriter out = null;
      File file;
      try
      {
         String extractOutputPath = leckoTempDirectory +"/" +leckoOutputName;
         file = new File(extractOutputPath );
         if (!PrivilegedFileHelper.exists(file))
         {
            PrivilegedFileHelper.delete(file);
         }

         out = new PrintWriter(new FileWriter(extractOutputPath));

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
               spaceList = SocialActivity.parseJSONArray(json, "spaces");
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
               JSONObject jsonObject = (JSONObject)obj;
               //space ID
               String spaceId = (String)jsonObject.get("id");

               if (LOG.isDebugEnabled())
               {
                  LOG.debug("Extract Data space :" + jsonObject.get("groupId"));
               }
               SocialActivity sc = new SpaceActivity(spaceId, exoSocialConnector);
               sc.loadActivityStream(out);
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
      finally
      {
         if(out != null)
         {
            out.flush();
            out.close();
         }
      }
      return true;
   }


}
