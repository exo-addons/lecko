package org.exoplatform.lecko;

import org.exoplatform.addons.lecko.JobStatusService;
import org.exoplatform.addons.lecko.social.client.rest.connector.ExoSocialConnector;
import org.exoplatform.addons.lecko.social.rest.impl.activity.ActivityRestResourcesV1;
import org.exoplatform.addons.lecko.social.rest.impl.space.SpaceRestResourcesV1;
import org.exoplatform.addons.lecko.social.rest.impl.user.UserRestResourcesV1;
import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.rest.impl.ResourceBinder;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.quartz.JobBuilder;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.util.logging.Logger;


@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-mop-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.core.test.configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.portal-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/component.search.configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/lecko-test-configuration.xml")
})
public class AbstractLeckoTestCase extends BaseExoTestCase {
    static Logger log = Logger.getLogger("LeckoTestCase");

    protected SpaceService spaceService;
    protected OrganizationService organizationService;
    protected IdentityManager identityManager;
    protected ActivityManager activityManager;
    protected PortalContainer container;
    protected ResourceBinder resourceBinder;

    protected ExoSocialConnector exoSocialConnector;
    protected JobStatusService jobStatusService;

    protected ActivityRestResourcesV1 activityRestResourcesV1;
    protected SpaceRestResourcesV1 spaceRestResourcesV1;
    protected UserRestResourcesV1 userRestResourcesV1;

    protected Authenticator authenticator;




    @Before
    public void setUp() throws Exception {
        begin();
        initServices();


    }

    private void initServices() throws Exception {
        container = getContainer();
        spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);
        organizationService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
        identityManager = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
        activityManager = (ActivityManager) container.getComponentInstanceOfType(ActivityManager.class);

        exoSocialConnector = (ExoSocialConnector) container.getComponentInstanceOfType(ExoSocialConnector.class);
        jobStatusService = (JobStatusService) container.getComponentInstanceOfType(JobStatusService.class);
        resourceBinder= (ResourceBinder) container.getComponentInstanceOfType(ResourceBinder.class);

        authenticator = (Authenticator)container.getComponentInstanceOfType(Authenticator.class);

        activityRestResourcesV1 = new ActivityRestResourcesV1();
        resourceBinder.addResource(activityRestResourcesV1,null);

        spaceRestResourcesV1 = new SpaceRestResourcesV1();
        resourceBinder.addResource(spaceRestResourcesV1, null);

        userRestResourcesV1 = new UserRestResourcesV1();
        resourceBinder.addResource(userRestResourcesV1, null);

        //add user api in group apiAccess
        User api = organizationService.getUserHandler().findUserByName("api");
        Group group = organizationService.getGroupHandler().findGroupById("api-access");
        MembershipType mb = organizationService.getMembershipTypeHandler().findMembershipType("member");
        organizationService.getMembershipHandler().linkMembership(api,group,mb,true);


        ConversationState c = new ConversationState(authenticator.createIdentity("api"));
        ConversationState.setCurrent(c);

    }


    @Override
    protected void tearDown() throws Exception {
        resourceBinder.removeResource(activityRestResourcesV1.getClass());
        resourceBinder.removeResource(spaceRestResourcesV1.getClass());
        resourceBinder.removeResource(userRestResourcesV1.getClass());

        jobStatusService.resetStatus();
        end();
    }

}
