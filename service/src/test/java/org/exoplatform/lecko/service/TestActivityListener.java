package org.exoplatform.lecko.service;

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

    }

}
