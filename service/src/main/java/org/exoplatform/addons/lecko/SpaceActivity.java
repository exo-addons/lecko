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

  public SpaceActivity(Space space) {
    super();
    this.space = space;
  }

  @Override
  public void loadActivityStream(PrintWriter out,
                                 IdentityManager identityManager,
                                 ActivityManager activityManager) throws Exception {

    boolean hasNextActivity = true;

    String idEvent = "";
    String date = "";
    String idactor = "";
    String placeName = "";
    int offsetActivities = DEFAULT_OFFSET;
    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getActivitiesOfSpaceWithListAccess(spaceIdentity);
    while (hasNextActivity) {
      // Get All activities by space id
      List<ExoSocialActivity> activities = listAccess.loadAsList(offsetActivities, DEFAULT_LIMIT);

      if (activities.size() == 0) {
        break;
      } else if (activities.size() < DEFAULT_LIMIT) {
        hasNextActivity = false;
      }

      for (ExoSocialActivity activity : activities) {
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
        getExoComments(activity, placeName, space.getDisplayName(), activityManager, out);
        // Getting Likes
        getLikes(activity, date, placeName, space.getDisplayName(), identityManager, out);
      }
      offsetActivities += DEFAULT_LIMIT;
      out.flush();
    }
  }

}
