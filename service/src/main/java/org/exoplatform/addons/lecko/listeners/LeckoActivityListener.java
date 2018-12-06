package org.exoplatform.addons.lecko.listeners;

import org.exoplatform.addons.lecko.UserEventService;
import org.exoplatform.addons.lecko.dao.UserEvent;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent;
import org.exoplatform.social.core.activity.ActivityListenerPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 30/08/17.
 */
public class LeckoActivityListener extends ActivityListenerPlugin {

    private static final Log LOG = ExoLogger.getLogger(LeckoActivityListener.class.getName());

    private ActivityManager activityManager;
    private UserEventService userEventService;

    public LeckoActivityListener(ActivityManager activityManager, UserEventService userEventService) {
        this.activityManager = activityManager;
        this.userEventService = userEventService;
    }

    public enum excludedTypes {
        SPACE_ACTIVITY,USER_ACTIVITIES_FOR_RELATIONSHIP,USER_COMMENTS_ACTIVITY_FOR_RELATIONSHIP;
        public static boolean contains(String s)
        {
            for(excludedTypes type:values())
                if (type.name().equals(s))
                    return true;
            return false;
        }
    }

    @Override
    public void saveActivity(ActivityLifeCycleEvent activityLifeCycleEvent) {
        LOG.debug("Save Activity Event.");
        ExoSocialActivity activity = activityLifeCycleEvent.getSource();
        activity = CommonsUtils.getService(ActivityManager.class).getActivity(activity.getId());
        if (!excludedTypes.contains(activity.getType())) {
            LOG.debug("Activity is type {}, which is not an excluded type", activity.getType());
            userEventService.storeEvent(activity.getPosterId(), UserEvent.eventType.CREATE.name(), activity.getUpdated(), activity.getId());
        } else {
            LOG.debug("Activity is type {}, which is an excluded type", activity.getType());
        }

    }


    @Override
    public void updateActivity(ActivityLifeCycleEvent activityLifeCycleEvent) {

    }

    @Override
    public void saveComment(ActivityLifeCycleEvent activityLifeCycleEvent) {
        LOG.debug("Save Comment Event");
        ExoSocialActivity comment = activityLifeCycleEvent.getSource();
        ExoSocialActivity activity = activityManager.getParentActivity(comment);
        while (activity.isComment()) {
            //this is for when we will be able to comment a comment.
            activity=activityManager.getParentActivity(activity);
        }

        if (!excludedTypes.contains(activity.getType())) {
            LOG.debug("Parent activity is type {}, which is not an excluded type", activity.getType());
            userEventService.storeEvent(comment.getPosterId(), UserEvent.eventType.CREATE.name(), comment.getUpdated(), comment.getId());
        } else {
            LOG.debug("Parent activity is type {}, which is an excluded type", activity.getType());
        }
    }

    @Override
    public void updateComment(ActivityLifeCycleEvent event) {
    }

    @Override
    public void likeActivity(ActivityLifeCycleEvent activityLifeCycleEvent) {
        LOG.debug("Save Activity Like Event.");
        ExoSocialActivity activity = activityLifeCycleEvent.getSource();
        activity = CommonsUtils.getService(ActivityManager.class).getActivity(activity.getId());
        if (!excludedTypes.contains(activity.getType())) {
            LOG.debug("Activity is type {}, which is not an excluded type", activity.getType());
            userEventService.storeEvent(activity.getPosterId(), UserEvent.eventType.LIKE.name(), activity.getUpdated(), activity.getId());
        } else {
            LOG.debug("Activity is type {}, which is an excluded type", activity.getType());
        }
    }

    @Override
    public void likeComment(ActivityLifeCycleEvent activityLifeCycleEvent) {
        LOG.debug("Save Comment Like Event.");
        ExoSocialActivity comment = activityLifeCycleEvent.getSource();
        comment = CommonsUtils.getService(ActivityManager.class).getActivity(comment.getId());
        ExoSocialActivity activity = activityManager.getParentActivity(comment);
        while (activity.isComment()) {
            //this is for when we will be able to comment a comment.
            activity=activityManager.getParentActivity(activity);
        }

        if (!excludedTypes.contains(activity.getType())) {
            LOG.debug("Activity is type {}, which is not an excluded type", activity.getType());
            userEventService.storeEvent(comment.getPosterId(), UserEvent.eventType.LIKE.name(), comment.getUpdated(), comment.getId());
        } else {
            LOG.debug("Activity is type {}, which is an excluded type", activity.getType());
        }
    }

}
