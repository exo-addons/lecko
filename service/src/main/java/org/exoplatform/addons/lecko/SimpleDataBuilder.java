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
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.PrivilegedFileHelper;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
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

   private SpaceService spaceService;

   private final String leckoTempDirectory;

   private final String leckoOutputName;

   private static final String LECKO_OUTPUT_NAME= "exo.addons.lecko.out.name";

   public SimpleDataBuilder(ExoSocialConnector exoSocialConnector, SpaceService spaceService)
   {
      this.exoSocialConnector = exoSocialConnector;
      this.spaceService = spaceService;
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
      File file = null;
      boolean state = true;
      try
      {
         String extractOutputPath = leckoTempDirectory +"/" +leckoOutputName;
         file = new File(extractOutputPath );
         if (!PrivilegedFileHelper.exists(file))
         {
            PrivilegedFileHelper.delete(file);
         }

         out = new PrintWriter(new FileWriter(extractOutputPath));
         ListAccess<Space> spaceListAccess = spaceService.getAllSpacesWithListAccess();
         ListAccess<Identity> userListAccess = CommonsUtils.getService(IdentityManager.class).getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, new ProfileFilter(), false);

         LOG.info("Lecko-Addons : Begin Extraction...");
         //Get All spaces
         int offset = 0;
         int size = 20;
         boolean hasNextSpace = true;
         int countSpace= 1;

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

               LOG.info("Extract Data from space:" + jsonObject.get("groupId") + " progress ..."+ countSpace +"/"+ (spaceListAccess.getSize()));
               SocialActivity sa = new SpaceActivity(spaceId, exoSocialConnector);
               sa.loadActivityStream(out);
               countSpace ++;
            }
            offset += size;
            out.flush();
         }
         /** Load User activity*/
         offset = 0;
         size = 20;
         boolean hasNextUser = true;
         int countUsUser=1;

         while (hasNextUser)
         {
            //Extract all users by limit
            String json = exoSocialConnector.getUsers(offset, size);
            JSONArray userList = null;

            if (json == null)
            {
               break;
            }
            else
            {
               userList = SocialActivity.parseJSONArray(json, "users");
            }

            if (userList == null || userList.size() == 0)
            {
               break;
            }
            else if (userList.size() < size)
            {
               hasNextUser = false;
            }

            //Extract all activities by user ID
            for (Object obj : userList)
            {
               JSONObject jsonObject = (JSONObject)obj;
               //user ID
               String userId = (String)jsonObject.get("username");
               LOG.info("Extract Data from user:" + jsonObject.get("username") + " progress ..."+ countUsUser +"/"+ (userListAccess.getSize()));
               SocialActivity ua = new UserActivity(userId, exoSocialConnector);
               ua.loadActivityStream(out);
               countUsUser++;
            }
            offset += size;
            out.flush();
         }
         LOG.info("Lecko-Addons : End Extraction");
      }
      catch (Exception ex)
      {
         LOG.error(ex);
         state = false;
      }
      finally
      {
         if(out != null)
         {
            out.flush();
            out.close();
         }

         if (!state && file != null && PrivilegedFileHelper.exists(file))
         {
            PrivilegedFileHelper.delete(file);
         }
      }
      return state;
   }


}
