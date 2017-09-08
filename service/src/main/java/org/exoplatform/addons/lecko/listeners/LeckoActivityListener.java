package org.exoplatform.addons.lecko.listeners;

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
    public static final String SPACE_ACTIVITY_TYPE = "SPACE_ACTIVITY";


    public enum excludedTypes {
        SPACE_ACTIVITY;
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
        LOG.debug("Save Activity Event, store it to USERS_EVENTS");
        ExoSocialActivity activity = activityLifeCycleEvent.getSource();
        activity = CommonsUtils.getService(ActivityManager.class).getActivity(activity.getId());
        if (!excludedTypes.contains(activity.getType())) {
            LOG.debug("Activity is type {}, which is not an excluded type", activity.getType());
        } else {
            LOG.debug("Activity is type {}, which is an excluded type", activity.getType());
        }

    }

    @Override
    public void updateActivity(ActivityLifeCycleEvent activityLifeCycleEvent) {

    }

    @Override
    public void saveComment(ActivityLifeCycleEvent activityLifeCycleEvent) {

    }

    @Override
    public void likeActivity(ActivityLifeCycleEvent activityLifeCycleEvent) {

    }
}
