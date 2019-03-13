/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
 *
 * This file is part of Lecko Analytics Add-on - Service.
 *
 * Lecko Analytics Add-on - Service is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * Lecko Analytics Add-on - Service software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Lecko Analytics Add-on - Service; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.addons.lecko;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.social.core.manager.ActivityManager;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * Created by The eXo Platform SAS. Author : Simon 12.02.2016
 *
 * @version $Id$
 */
public class SimpleDataBuilder implements DataBuilder {
  private static Log       LOG      = ExoLogger.getLogger(SimpleDataBuilder.class);

  private SpaceService     spaceService;

  private final String     leckoTempDirectory;

  private final String     leckoOutputName;

  private JobStatusService jobStatusService;

  private IdentityManager  identityManager;

  private ActivityManager  activityManager;

  private EntityManagerService entityManagerService;

  private int initialNbSpaces;

  private int initialNbUsers;

  private static boolean   runBuild = false;

  private final int        spaceLimit;

  private final int        userLimit;

  public SimpleDataBuilder(SpaceService spaceService,
                           IdentityManager identityManager,
                           ActivityManager activityManager,
                           JobStatusService jobStatusService,
                           EntityManagerService entityManagerService) {
    this.spaceService = spaceService;
    this.jobStatusService = jobStatusService;
    this.activityManager = activityManager;
    this.identityManager = identityManager;
    this.entityManagerService = entityManagerService;

    this.leckoTempDirectory = LeckoServiceController.getRootPath();

    this.initialNbSpaces=-1;
    this.initialNbUsers=-1;

    File directory = new File(this.leckoTempDirectory);
    leckoOutputName = LeckoServiceController.getFileName();

    if (PropertyManager.getProperty("exo.addon.lecko.spaceLimit") != null) {
      spaceLimit = Integer.parseInt(PropertyManager.getProperty("exo.addon.lecko.spaceLimit"));
    } else {
      spaceLimit = -1;
    }
    if (PropertyManager.getProperty("exo.addon.lecko.userLimit") != null) {
      userLimit = Integer.parseInt(PropertyManager.getProperty("exo.addon.lecko.userLimit"));
    } else {
      userLimit = -1;
    }

  }

  public void stopBuild() {
    runBuild = false;
  }

  @Override
  public boolean getBuildStatus() {
    return runBuild;
  }

  @Override
  public int getPercent() {


    if (this.initialNbSpaces!=-1 && this.initialNbUsers!=-1) {
      LOG.info("Initial count status : {}, nbSpaces : {}, nb users : {}", jobStatusService.countStatus(), this.initialNbSpaces, this.initialNbUsers);
      int result =  (int) (((double) jobStatusService.countStatus() / (this.initialNbUsers + this.initialNbSpaces)) * 100);
      if (result>100) result = 100;
      return result;
    } else {
      ListAccess<Space> spaceListAccess = spaceService.getAllSpacesWithListAccess();
      ListAccess<Identity> userListAccess = CommonsUtils.getService(IdentityManager.class)
              .getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME,
                      new ProfileFilter(),
                      false);

      try {
        LOG.info("count status : {}, nbSpaces : {}, nb users : {}", jobStatusService.countStatus(), spaceListAccess.getSize(), userListAccess.getSize());
        return (int) (((double) jobStatusService.countStatus() / (spaceListAccess.getSize() + userListAccess.getSize())) * 100);
      } catch (Exception e) {
        LOG.error("Error when counting status", e);
        return 0;
      }
    }

  }

  @Override
  public void resetCounter() {
    this.initialNbSpaces=-1;
    this.initialNbUsers=-1;
  }

  public void resumeBuild() {
    runBuild = true;
  }

  @Override
  public boolean build() {
    ExoContainer currentContainer = ExoContainerContext.getCurrentContainer();
    entityManagerService.startRequest(currentContainer);
    PrintWriter out = null;
    File file = null;
    boolean state = true;
    runBuild = true;
    try {
      String extractOutputPath = leckoTempDirectory + "/" + leckoOutputName;

      out = new PrintWriter(new FileWriter(extractOutputPath, true));
      ListAccess<Space> spaceListAccess = spaceService.getAllSpacesWithListAccess();
      ListAccess<Identity> userListAccess = identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME,
                                                                                      new ProfileFilter(),
                                                                                      false);
      this.initialNbUsers=userListAccess.getSize();
      this.initialNbSpaces=spaceListAccess.getSize();

      LOG.debug("Space to extract : {}, users to extract : {}", this.initialNbSpaces,this.initialNbUsers);


      // verifier si on a deja tout traité. Si oui, sortir directement.
      if (getPercent() == 100) {
        LOG.info("Extraction already finished.");
        return true;
      }

      LOG.info("Lecko-Addons : Begin Extraction...");
      // Get All spaces
      int offset = 0;
      int size = 20;
      boolean hasNextSpace = true;
      int countSpace = 1;

      int lastSpaceLog = 0;
      LOG.info("Lecko-Addons : Begin Space Extraction...");
      while (spaceLimit != 0 && hasNextSpace) {
        // Extract all spaces by limit
        Space[] spaces = spaceListAccess.load(offset, size);

        if (spaces.length == 0) {
          break;
        } else if (spaces.length < size) {
          hasNextSpace = false;
        }

        // Extract all activities by space ID
        for (Space space : spaces) {
          if (runBuild) {
            entityManagerService.endRequest(currentContainer);
            entityManagerService.startRequest(currentContainer);
            // space ID
            String spaceId = space.getId();
            String spaceDisplayName = space.getDisplayName();

            double spacePercent = ((double) countSpace / spaceListAccess.getSize()) * 100;
            if ((int) spacePercent % 5 == 0 && lastSpaceLog != (int) spacePercent) {
              LOG.info("Extract Data from spaces {}%", (int) spacePercent);
              lastSpaceLog = (int) spacePercent;
            }
            if (jobStatusService.findByIdentityIdAndProvider(spaceId,"space") == null) {
              // space not treaten in this iteration
              LOG.debug("Export datas for spaceId={} ", spaceId);
              SocialActivity sa = new SpaceActivity(space);
              sa.loadActivityStream(out, identityManager, activityManager);

              // store the id, to say that the space is treated.
              jobStatusService.storeStatus(spaceId, "space");
            } else {
              LOG.debug("Data already extracted for this space: {} in this iteration.", spaceId);

            }
            countSpace++;
            if (spaceLimit != -1 && countSpace > spaceLimit)
              break;
          } else {
            LOG.info("Export was stopped");
            return true;
          }
        }
        offset += size;
        out.flush();
        if (spaceLimit != -1 && countSpace > spaceLimit)
          break;
      }

      /** Load User activity */
      offset = 0;
      size = 500;
      boolean hasNextUser = true;
      int countUser = 1;
      int lastUserLog = 0;
      LOG.info("Lecko-Addons : Begin User Extraction...");
      while (userLimit != 0 && hasNextUser) {
        // Extract all users by limit
        Identity[] identities = userListAccess.load(offset, size);
        if (identities.length == 0) {
          break;
        } else if (identities.length < size) {
          hasNextUser = false;
        }

        LOG.debug("Identities array size : {}",identities.length);

        // Extract all activities by user ID
        for (Identity identity : identities) {
          if (runBuild) {
            entityManagerService.endRequest(currentContainer);
            entityManagerService.startRequest(currentContainer);
            // user ID
            String userId = identity.getRemoteId();
            double userPercent = ((double) countUser / userListAccess.getSize()) * 100;
            if ((int) userPercent % 5 == 0 && lastUserLog != (int) userPercent) {
              LOG.info("Extract Data from users {}%", (int) userPercent);
              lastUserLog = (int) userPercent;
            }
            if (jobStatusService.findByIdentityIdAndProvider(userId,"organization") == null) {
              LOG.debug("Extract Data from user:{}", userId);

              SocialActivity ua = new UserActivity(identity);
              ua.loadActivityStream(out, identityManager, activityManager);
              jobStatusService.storeStatus(userId, "organization");
            } else {
              LOG.debug("Data already extracted for this user : {} in this iteration.", userId);

            }

            countUser++;
            LOG.debug("Users treaten : {}/{}",countUser,this.initialNbUsers);
            if (userLimit != -1 && countUser > userLimit)
              break;
          } else {
            LOG.info("Export was stopped");
            return true;
          }
        }
        offset += size;
        out.flush();
        if (userLimit != -1 && countUser > userLimit)
          break;

      }
      LOG.info("Lecko-Addons : End Extraction");
    } catch (ExportException ex) {
      LOG.error("Lecko-Addons : Extraction stopped by ExportException. Stop the extract by security", ex);
      state = false;
    } catch (Exception ex) {
      LOG.error("Lecko-Addons : Extraction stopped by exception", ex);
      state = false;
    } finally {
      entityManagerService.endRequest(currentContainer);
      if (out != null) {
        out.flush();
        out.close();
      }

      runBuild = false;
    }
    return state;
  }

  public void deleteDumpFile() {
    String extractOutputPath = leckoTempDirectory + "/" + leckoOutputName;
    Path path = Paths.get(extractOutputPath);
    try{
      Files.delete(path);
      LOG.info("Dump file deleted.");
    }
    catch(NoSuchFileException e) {
      LOG.error("The Lecko dump file to delete (" + path + ") does not exist", e);
    } catch(IOException e) {
      LOG.error("The dump file to delete (" + path + ") does not exist", e);
    }
  }

  // @Override
  public void run() {
    // the em is put in threadLocal and use in the build.
    build();
    // try to upload data
    // will run only if 100% finished
    RequestLifeCycle.begin(entityManagerService);
    LeckoServiceController.getService(LeckoServiceController.class).UploadLeckoData();
    RequestLifeCycle.end();
  }
}
