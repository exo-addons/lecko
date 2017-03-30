package org.exoplatform.lecko;

import org.exoplatform.addons.lecko.JobStatusService;
import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.rest.impl.ResourceBinder;
import org.exoplatform.services.security.Authenticator;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.junit.Before;

import java.util.logging.Logger;

@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-mop-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.core.test.configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.jcr-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.portal-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/component.search.configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/lecko-test-configuration.xml") })
public class AbstractLeckoTestCase extends BaseExoTestCase {
  static Logger                 log = Logger.getLogger("LeckoTestCase");

  protected SpaceService        spaceService;

  protected OrganizationService organizationService;

  protected IdentityManager     identityManager;

  protected ActivityManager     activityManager;

  protected PortalContainer     container;

  protected ResourceBinder      resourceBinder;

  protected JobStatusService    jobStatusService;

  protected Authenticator       authenticator;

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

    jobStatusService = (JobStatusService) container.getComponentInstanceOfType(JobStatusService.class);
    resourceBinder = (ResourceBinder) container.getComponentInstanceOfType(ResourceBinder.class);

    authenticator = (Authenticator) container.getComponentInstanceOfType(Authenticator.class);

    // add user api in group apiAccess
    User api = organizationService.getUserHandler().findUserByName("api");
    Group group = organizationService.getGroupHandler().findGroupById("api-access");
    MembershipType mb = organizationService.getMembershipTypeHandler().findMembershipType("member");
    organizationService.getMembershipHandler().linkMembership(api, group, mb, true);

    ConversationState c = new ConversationState(authenticator.createIdentity("api"));
    ConversationState.setCurrent(c);

  }

  @Override
  protected void tearDown() throws Exception {

    jobStatusService.resetStatus();
    end();
  }

}
