package org.exoplatform.lecko.service;

import org.exoplatform.addons.lecko.dao.UserEvent;
import org.exoplatform.lecko.test.AbstractServiceTest;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.junit.Before;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 02/10/17.
 */
public class TestActivityListener extends AbstractServiceTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testNewActivity() throws Exception {

        Identity johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", true);
        Identity maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", true);


        ExoSocialActivity activity = new ExoSocialActivityImpl();
        activity.setTitle("My Activity");
        activity.setUserId(johnIdentity.getId());
        activity.setType("DEFAULT_ACTIVITY");
        activityManager.saveActivityNoReturn(johnIdentity, activity);

        assertTrue(userEventService.findEventsByObjectId(activity.getId()).size() == 1);

        // mary comments and likes the activity
        ExoSocialActivity comment = new ExoSocialActivityImpl();
        comment.setTitle("Mary's Comment");
        comment.setUserId(maryIdentity.getId());
        activityManager.saveComment(activity, comment);

        assertTrue(userEventService.findEventsByObjectId(comment.getId()).size() == 1);

        //like the activity
        activityManager.saveLike(activity, maryIdentity);
        assertTrue(userEventService.findEventsByObjectId(activity.getId()).size() == 2);
        assertTrue(userEventService.findEventsByObjectIdAndEventType(activity.getId(), UserEvent.eventType.LIKE.name()).size() == 1);


        //like the comment
        activityManager.saveLike(comment, johnIdentity);
        assertTrue(userEventService.findEventsByObjectId(comment.getId()).size() == 2);
        assertTrue(userEventService.findEventsByObjectIdAndEventType(comment.getId(), UserEvent.eventType.LIKE.name()).size() == 1);

    }

    public void testActivityTypes() throws Exception {

        Identity johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", true);

        String[] possibleActivityTypesToStore= {"sharefiles:spaces",
                "DEFAULT_ACTIVITY",
                "DOC_ACTIVITY",
                "files:spaces",
                "sharecontents:spaces",
                "contents:spaces",
                "cs-calendar:spaces",
                "ks-forum:spaces",
                "ks-answer:spaces",
                "ks-poll:spaces",
                "ks-wiki:spaces"
        };

        String[] possibleActivityTypesToNOTStore= {"SPACE_ACTIVITY",
                "USER_PROFILE_ACTIVITY",
                "USER_ACTIVITIES_FOR_RELATIONSHIP"
        };

        for (String type : possibleActivityTypesToStore) {
            ExoSocialActivity activity = new ExoSocialActivityImpl();
            activity.setTitle("My Activity");
            activity.setUserId(johnIdentity.getId());
            activity.setType(type);
            activityManager.saveActivityNoReturn(johnIdentity, activity);
            assertTrue(userEventService.findEventsByObjectId(activity.getId()).size() == 1);
        }

        for (String type : possibleActivityTypesToNOTStore) {
            ExoSocialActivity activity = new ExoSocialActivityImpl();
            activity.setTitle("My Activity");
            activity.setUserId(johnIdentity.getId());
            activity.setType(type);
            activityManager.saveActivityNoReturn(johnIdentity, activity);
            assertTrue(userEventService.findEventsByObjectId(activity.getId()).size() == 0);
        }




    }
}
