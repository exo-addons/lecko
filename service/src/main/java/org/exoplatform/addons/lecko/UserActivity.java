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
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 */
public class UserActivity extends SocialActivity {
  private static Log LOG      = ExoLogger.getLogger(UserActivity.class);
  private Identity userIdentity;

  public UserActivity(Identity identity) {
    super();
    this.userIdentity = identity;
  }

  @Override
  public void loadActivityStream(PrintWriter out,
                                 IdentityManager identityManager,
                                 ActivityManager activityManager) throws Exception {

    RequestLifeCycle.begin(PortalContainer.getInstance());
//    try {

      int offsetActivities = DEFAULT_OFFSET;
      int sizeActivities = DEFAULT_LIMIT;
      boolean hasNextActivity = true;

      String idEvent = "";
      String date = "";
      String idactor = "";
      String placeName = "";
      RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesWithListAccess(userIdentity);

      int activityCountToTreat = listAccess.getSize();
      int activityTreated = 0;

      while (hasNextActivity) {

        List<String> activitiesId = listAccess.loadIdsAsList(offsetActivities, DEFAULT_LIMIT);
        if (activitiesId.size() != 0) {
          for (String activityId : activitiesId) {
            ExoSocialActivity activity=activityManager.getActivity(activityId);
            String type_space = "";
            String url_comments = "no_url";
            String url_likes = "no_url";
            type_space = activity.getActivityStream().getType().toString();
            if ("organization".equals(type_space)) {
              type_space = "user";
              idactor = activity.getPosterId();

              // constuction de la map des users au fur et mesure pour
              // l'anonymisation
              if (!user_map.containsKey(idactor)) {
                user_map.put(idactor, Integer.toString(user_map.size() + 1));
                idactor = user_map.get(idactor);
              } else {
                idactor = user_map.get(idactor);
              }
              out.print(idactor + ";");
              out.print(activity.getType() + ";");
              Calendar createdDate = Calendar.getInstance();
              createdDate.setTime(new Date(activity.getPostedTime()));
              date = ISO8601.format(createdDate);
              out.print(date + ";");
              out.print(type_space + ";");
              out.print(placeName + ";");
              out.println();

              // Getting Comments
              getExoComments(activity, placeName, "", activityManager, identityManager, out);
              // Getting Likes
              getLikes(activity, date, placeName, "", identityManager, out);

            }
            activityTreated++;
          }
          offsetActivities += sizeActivities;
          out.flush();
        } else {
          hasNextActivity=false;
        }
      }

      if (activityCountToTreat!=activityTreated) {
        throw new ExportException("Exported activities for user "+userIdentity.getRemoteId()+" doesn't correspond to the number of activities. An error occured during the export.");
      }
/**
      } finally {
      RequestLifeCycle.end();
    }
 */
  }
}
