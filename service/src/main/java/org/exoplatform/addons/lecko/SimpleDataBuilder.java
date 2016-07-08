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

import org.exoplatform.addons.lecko.dao.JobStatus;
import org.exoplatform.addons.lecko.social.client.rest.connector.ExoSocialConnector;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.PrivilegedFileHelper;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
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

import javax.persistence.EntityManager;
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
public class SimpleDataBuilder implements DataBuilder {
   private static Log LOG = ExoLogger.getLogger(SimpleDataBuilder.class);

   private ExoSocialConnector exoSocialConnector;

   private SpaceService spaceService;

   private final String leckoTempDirectory;

   private final String leckoOutputName;

   private JobStatusService jobStatusService;

   private static final String LECKO_OUTPUT_NAME= "exo.addons.lecko.out.name";

   private static boolean runBuild = false;

   private final int spaceLimit;

   private final int userLimit;

   public SimpleDataBuilder(ExoSocialConnector exoSocialConnector, SpaceService spaceService, JobStatusService jobStatusService)

   {
      this.exoSocialConnector = exoSocialConnector;
      this.spaceService = spaceService;
      this.jobStatusService=jobStatusService;
      this.leckoTempDirectory = PropertyManager.getProperty("java.io.tmpdir") + "/lecko";
      File directory = new File(this.leckoTempDirectory );
      if (!PrivilegedFileHelper.exists(directory))
      {
         PrivilegedFileHelper.mkdirs(directory);
      }
      String  name = PropertyManager.getProperty(LECKO_OUTPUT_NAME);
      leckoOutputName = (name != null && ! name.isEmpty()) ? name : "dump";

      if (PropertyManager.getProperty("exo.addon.lecko.spaceLimit")!=null) {
         spaceLimit = Integer.parseInt(PropertyManager.getProperty("exo.addon.lecko.spaceLimit"));
      } else {
         spaceLimit=-1;
      }
      if (PropertyManager.getProperty("exo.addon.lecko.userLimit")!=null) {
         userLimit = Integer.parseInt(PropertyManager.getProperty("exo.addon.lecko.userLimit"));
      } else {
         userLimit=-1;
      }

   }

   public void stopBuild() {
      runBuild=false;
   }

   @Override
   public boolean getBuildStatus() {
      return runBuild;
   }

   @Override
   public int getPercent() {
      ListAccess<Space> spaceListAccess = spaceService.getAllSpacesWithListAccess();
      ListAccess<Identity> userListAccess = CommonsUtils.getService(IdentityManager.class).getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, new ProfileFilter(), false);

      try {
         return (int)(((double)jobStatusService.countStatus()/(spaceListAccess.getSize()+userListAccess.getSize()))*100);
      } catch (Exception e) {
         e.printStackTrace();
         return 0;
      }

   }

   public void resumeBuild() {
      runBuild=true;
   }



   @Override
   public boolean build()
   {
      PrintWriter out = null;
      File file = null;
      boolean state = true;
      runBuild=true;
      try
      {
         String extractOutputPath = leckoTempDirectory +"/" +leckoOutputName;
         file = new File(extractOutputPath);
         if (!PrivilegedFileHelper.exists(file))
         {
            PrivilegedFileHelper.delete(file);
         }

         out = new PrintWriter(new FileWriter(extractOutputPath,true));
         ListAccess<Space> spaceListAccess = spaceService.getAllSpacesWithListAccess();
         ListAccess<Identity> userListAccess = CommonsUtils.getService(IdentityManager.class).getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, new ProfileFilter(), false);

         LOG.info("Lecko-Addons : Begin Extraction...");
         //Get All spaces
         int offset = 0;
         int size = 20;
         boolean hasNextSpace = true;
         int countSpace= 1;

         int lastSpaceLog=0;
         LOG.info("Lecko-Addons : Begin Space Extraction...");
         while (spaceLimit != 0 && hasNextSpace) {
               //Extract all spaces by limit
               String json = exoSocialConnector.getSpaces(offset, size);
               JSONArray spaceList = null;

               if (json == null) {
                  break;
               } else {
                  spaceList = SocialActivity.parseJSONArray(json, "spaces");
               }

               if (spaceList == null || spaceList.size() == 0) {
                  break;
               } else if (spaceList.size() < size) {
                  hasNextSpace = false;
               }

               //Extract all activities by space ID
               for (Object obj : spaceList) {
                  if (runBuild) {
                     JSONObject jsonObject = (JSONObject) obj;
                     //space ID
                     String spaceId = (String) jsonObject.get("id");
                     double spacePercent = ((double)countSpace/spaceListAccess.getSize())*100;
                     if ((int)spacePercent % 5 == 0 && lastSpaceLog!=(int)spacePercent) {
                        LOG.info("Extract Data from spaces " + (int)spacePercent + "%");
                        lastSpaceLog=(int)spacePercent;
                     }
                     if (jobStatusService.findByIdentityId(spaceId) == null) {
                        //space not treaten in this iteration
                        if (LOG.isDebugEnabled()) {
                           LOG.debug("Export datas for space "+spaceId);
                        }
                        SocialActivity sa = new SpaceActivity(spaceId, exoSocialConnector);
                        sa.loadActivityStream(out);

                        //store the id, to say that the space is treated.
                        jobStatusService.storeStatus(spaceId);
                     } else {
                        if (LOG.isDebugEnabled()) {
                           LOG.debug("Data already extracted for this space:" + spaceId + " in this iteration.");
                        }

                     }
                     countSpace++;
                     if(spaceLimit != -1 && countSpace > spaceLimit )
			break;
                  } else {
                     LOG.info("Export was stopped");
                     return true;
                  }
               }
               offset += size;
               out.flush();
	       if(spaceLimit != -1 && countSpace > spaceLimit )
			break;
         }

         /** Load User activity*/
         offset = 0;
         size = 20;
         boolean hasNextUser = true;
         int countUser=1;
         int lastUserLog = 0;
         LOG.info("Lecko-Addons : Begin User Extraction...");
         while (userLimit!=0 && hasNextUser) {
               //Extract all users by limit
               String json = exoSocialConnector.getUsers(offset, size);
               JSONArray userList = null;

               if (json == null) {
                  break;
               } else {
                  userList = SocialActivity.parseJSONArray(json, "users");
               }

               if (userList == null || userList.size() == 0) {
                  break;
               } else if (userList.size() < size) {
                  hasNextUser = false;
               }

               //Extract all activities by user ID
               for (Object obj : userList) {
                  if (runBuild) {

                     JSONObject jsonObject = (JSONObject) obj;
                     //user ID
                     String userId = (String) jsonObject.get("username");
                     double userPercent = ((double)countUser/userListAccess.getSize())*100;
                     if ((int)userPercent % 5 == 0 && lastUserLog!=(int)userPercent) {
                        LOG.info("Extract Data from users " + (int)userPercent + "%");
                        lastUserLog=(int)userPercent;
                     }
                     if (jobStatusService.findByIdentityId(userId) == null) {
                        if(LOG.isDebugEnabled()) {
                           LOG.debug("Extract Data from user:" + userId);
                        }
                        SocialActivity ua = new UserActivity(userId, exoSocialConnector);
                        ua.loadActivityStream(out);
                        jobStatusService.storeStatus(userId);
                     } else {
                        if (LOG.isDebugEnabled()) {
                           LOG.info("Data already extracted for this user :" + userId + " in this iteration.");
                        }

                     }

                     countUser++;
		             if(userLimit != -1 && countUser > userLimit)  break;
                  } else {
                     LOG.info("Export was stopped");
                     return true;
                  }
               }
               offset += size;
               out.flush();
               if(userLimit != -1 && countUser > userLimit)
                  break;

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


   public void deleteDumpFile() {
      String extractOutputPath = leckoTempDirectory +"/" +leckoOutputName;
      File file = new File(extractOutputPath);
      file.delete();
   }


   //@Override
   public void run() {
      //need to create enttityManager for the thread.
      EntityManagerService service = PortalContainer.getInstance().getComponentInstanceOfType(EntityManagerService.class);
      service.startRequest(PortalContainer.getInstance());
      //the em is put in threadLocal and use in the build.

      build();
      service.endRequest(PortalContainer.getInstance());
   }
}
