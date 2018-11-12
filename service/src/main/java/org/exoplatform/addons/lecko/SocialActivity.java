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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 */
abstract class SocialActivity {
  protected static Log                 LOG            = ExoLogger.getLogger(SimpleDataBuilder.class);

  // anonymization MAP
  protected static Map<String, String> user_map       = new ConcurrentHashMap<String, String>();

  protected int                        DEFAULT_OFFSET = 0;

  protected int                        DEFAULT_LIMIT  = 20;

  public abstract void loadActivityStream(PrintWriter out,
                                          IdentityManager identityManager,
                                          ActivityManager activityManager) throws Exception;

  protected void getExoComments(ExoSocialActivity activity,
                                String placeName,
                                String displayName,
                                ActivityManager activityManager,
                                IdentityManager identityManager,
                                PrintWriter out) throws Exception {

    LOG.debug("Getting Comments : {} ", placeName);
    String result;
    String idEvent = "";
    String date = "";
    String idactor = "";
    int offsetComments = DEFAULT_OFFSET;
    boolean hasNextComments = true;

    RealtimeListAccess<ExoSocialActivity> commentsWithListAccess = activityManager.getCommentsWithListAccess(activity,true);
    int commentCountToTreat = commentsWithListAccess.getSize();
    int commentTreated = 0;
    while (hasNextComments) {
      List<ExoSocialActivity> comments = commentsWithListAccess.loadAsList(offsetComments, DEFAULT_LIMIT);
      if (comments.size() != 0) {

        for (ExoSocialActivity comment : comments) {
          idactor = comment.getPosterId();
          if (!user_map.containsKey(idactor)) {
            user_map.put(idactor, Integer.toString(user_map.size() + 1));
            idactor = user_map.get(idactor);
          } else {
            idactor = user_map.get(idactor);
          }
          out.print(idactor + ";");

          idEvent = "comment";
          out.print(idEvent + ";");
          Calendar createdDate = Calendar.getInstance();
          createdDate.setTime(new Date(comment.getPostedTime()));
          date = ISO8601.format(createdDate);
          out.print(date + ";");
          out.print(placeName + ";" + displayName + ";");
          out.println();

          getLikes(comment, date, placeName, "", identityManager, out);
          getSubComments(comment,placeName,displayName,activityManager,identityManager,out);
          commentTreated++;
        }
        offsetComments += DEFAULT_LIMIT;
        out.flush();
      } else {
        hasNextComments=false;
      }
    }

    // This tast is due to product's bug
    // Business implements within method CommentsRealtimeListAccess.getSize() is not exactly the same as we have within method CommentsRealtimeListAccess.loadAsList
    if (commentCountToTreat > commentTreated) {
      throw new ExportException("Exported comments for activity "+activity.getId()+" doesn't correspond to the number of comments. An error occured during the export.");
    }
    LOG.debug("End Getting Comments : {} ", placeName);

  }

  protected void getSubComments(ExoSocialActivity activity,
                                String placeName,
                                String displayName,
                                ActivityManager activityManager,
                                IdentityManager identityManager,
                                PrintWriter out) throws Exception {

    LOG.debug("Getting Sub Comments : {} ", placeName);
    String result;
    String idEvent = "";
    String date = "";
    String idactor = "";
    int offsetComments = DEFAULT_OFFSET;
    boolean hasNextComments = true;

    List<ExoSocialActivity> comments = activityManager.getSubComments(activity);
    int commentCountToTreat = comments.size();
    int commentTreated = 0;
    if (comments.size() != 0) {

        for (ExoSocialActivity comment : comments) {
          idactor = comment.getPosterId();
          if (!user_map.containsKey(idactor)) {
            user_map.put(idactor, Integer.toString(user_map.size() + 1));
            idactor = user_map.get(idactor);
          } else {
            idactor = user_map.get(idactor);
          }
          out.print(idactor + ";");

          idEvent = "comment";
          out.print(idEvent + ";");
          Calendar createdDate = Calendar.getInstance();
          createdDate.setTime(new Date(comment.getPostedTime()));
          date = ISO8601.format(createdDate);
          out.print(date + ";");
          out.print(placeName + ";" + displayName + ";");
          out.println();

          getLikes(comment, date, placeName, "", identityManager, out);
          commentTreated++;
        }
        out.flush();
      }


    if (commentCountToTreat!=commentTreated) {
      throw new ExportException("Exported Sub comments for activity "+activity.getId()+" doesn't correspond to the number of comments. An error occured during the export.");
    }

    LOG.debug("End Getting Sub Comments : {} ", placeName);

  }


  protected void getLikes(ExoSocialActivity activity,
                          String date,
                          String placeName,
                          String displayName,
                          IdentityManager identityManager,
                          PrintWriter out) throws Exception {

    LOG.debug("Getting Likes : {}", placeName);

    String result;
    String idEvent = "";
    String idactor = "";

    List<String> likerIds = Arrays.asList(activity.getLikeIdentityIds());
    int likeCountToTreat = likerIds.size();
    int likeTreated = 0;
    for (String likerId : likerIds) {
      idactor = identityManager.getIdentity(likerId, false).getRemoteId();

      if (!user_map.containsKey(idactor)) {
        user_map.put(idactor, Integer.toString(user_map.size() + 1));
        idactor = user_map.get(idactor);
      } else {
        idactor = user_map.get(idactor);
      }
      out.print(idactor + ";");

      idEvent = "like";
      out.print(idEvent + ";");

      // here we put the date of the activity because we dont have the date of
      // the like.
      out.print(date + ";");
      out.print(placeName + ";" + displayName + ";");
      out.println();
      out.flush();
      likeTreated++;
    }

    if (likeCountToTreat!=likeTreated) {
      throw new ExportException("Exported like for activity "+activity.getId()+" doesn't correspond to the number of likes. An error occured during the export.");
    }

    LOG.debug("End Getting Likes : {} ", placeName);
  }

}
