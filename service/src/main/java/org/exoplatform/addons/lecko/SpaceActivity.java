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

import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 */
public class SpaceActivity extends SocialActivity {

  Space space;
  private static Log LOG      = ExoLogger.getLogger(SpaceActivity.class);

  public SpaceActivity(Space space) {
    super();
    this.space = space;
  }

  @Override
  public void loadActivityStream(PrintWriter out,
                                 IdentityManager identityManager,
                                 ActivityManager activityManager) throws Exception {


//    RequestLifeCycle.begin(PortalContainer.getInstance());
//    try {

      LOG.debug("Start extraction for space {}", space.getDisplayName());
      boolean hasNextActivity = true;

      String idEvent = "";
      String date = "";
      String idactor = "";
      String placeName = "";
      int offsetActivities = DEFAULT_OFFSET;
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);

      RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesOfSpaceWithListAccess(spaceIdentity);


      int activityCountToTreat = listAccess.getSize();
      int activityTreated = 0;

      while (hasNextActivity) {
        // Get All activities by space id
        List<String> activitiesId = listAccess.loadIdsAsList(offsetActivities, DEFAULT_LIMIT);

        if (activitiesId.size() != 0) {

          for (String activityId : activitiesId) {
            ExoSocialActivity activity = activityManager.getActivity(activityId);
            String type_space = "";
            String url_comments = "no_url";
            String url_likes = "no_url";

            idactor = activity.getPosterId();

            // constuction de la map des users au fur et mesure pour l'anonymisation
            if (!user_map.containsKey(idactor)) {
              user_map.put(idactor, Integer.toString(user_map.size() + 1));
              idactor = user_map.get(idactor);
            } else {
              idactor = user_map.get(idactor);
            }
            out.print(idactor + ";");

            idEvent = activity.getType();
            out.print(idEvent + ";");
            Calendar createdDate = Calendar.getInstance();
            createdDate.setTime(new Date(activity.getPostedTime()));
            date = ISO8601.format(createdDate);
            out.print(date + ";");
            type_space = activity.getActivityStream().getType().toString();
            out.print(type_space + ";");
            placeName = activity.getActivityStream().getPrettyId();
            if (type_space.equals("user")) {
              out.print(";;");
            }
            out.print(placeName + ";" + space.getDisplayName() + ";");
            out.println();

            // Getting Comments
            getExoComments(activity, placeName, space.getDisplayName(), activityManager, identityManager, out);
            // Getting Likes
            getLikes(activity, date, placeName, space.getDisplayName(), identityManager, out);
            activityTreated++;
          }
          offsetActivities += DEFAULT_LIMIT;
          out.flush();
        } else {
          hasNextActivity = false;
        }
      }
      if (activityCountToTreat != activityTreated) {
        throw new ExportException("Exported acitvities for user " + space.getDisplayName() + " doesn't correspond to the number of activities. An error occured during the export.");
      }
//    } finally {
//      RequestLifeCycle.end();
//    }
      LOG.debug("End extraction for space {}", space.getDisplayName());

  }

}
