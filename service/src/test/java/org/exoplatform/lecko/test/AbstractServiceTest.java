/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.lecko.test;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.addons.lecko.JobStatusService;
import org.exoplatform.addons.lecko.LeckoServiceController;
import org.exoplatform.addons.lecko.UserEventService;
import org.exoplatform.addons.lecko.dao.UserEvent;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.testing.BaseExoTestCase;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.RDBMSActivityStorageImpl;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.ActivityStorage;

import java.util.*;

/**
 * AbstractServiceTest.java
 *
 */
@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.lecko.component.service.test.configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.lecko.component.service.test.dependencies-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.lecko.component.service.test.local-configuration.xml"),
})
public abstract class AbstractServiceTest extends BaseExoTestCase {
  protected static Log LOG = ExoLogger.getLogger(AbstractServiceTest.class.getName());
  protected SpaceService spaceService;
  protected IdentityManager identityManager;
  protected RelationshipManager relationshipManager;
  protected ActivityManager activityManager;
  protected JobStatusService jobStatusService;
  protected LeckoServiceController leckoServiceController;
  protected UserEventService userEventService;
  protected EntityManagerService entityManagerService;


  @Override
  protected void setUp() throws Exception {
    begin();

    identityManager = getService(IdentityManager.class);
    activityManager =  getService(ActivityManager.class);
    relationshipManager = getService(RelationshipManager.class);
    spaceService = getService(SpaceService.class);
    jobStatusService = getService(JobStatusService.class);
    leckoServiceController = getService(LeckoServiceController.class);
    userEventService = getService(UserEventService.class);
    entityManagerService = getService(EntityManagerService.class);

    identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", true);
    identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", true);
    identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo", true);

    //Space space = createSpace("new Space","new_space", "description", "root", new String[]{});


  }

  @Override
  protected void tearDown() throws Exception {

    resetAllActivities();

    //
    end();
  }

  private void resetAllActivities() throws Exception {
    RequestLifeCycle.begin(getContainer());
    try {
      List<Identity> identities = identityManager.getIdentities(OrganizationIdentityProvider.NAME);
      for (Identity identity : identities) {
        activityManager.getActivities(identity).forEach(activityManager::deleteActivity);
      }
    } finally {
      RequestLifeCycle.end();
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getService(Class<T> clazz) {
    return (T) getContainer().getComponentInstanceOfType(clazz);
  }


  protected void loginUser(String userId) {
    MembershipEntry membershipEntry = new MembershipEntry("/platform/user", "*");
    Collection<MembershipEntry> membershipEntries = new ArrayList<MembershipEntry>();
    membershipEntries.add(membershipEntry);
    org.exoplatform.services.security.Identity identity = new org.exoplatform.services.security.Identity(userId, membershipEntries);
    ConversationState state = new ConversationState(identity);
    ConversationState.setCurrent(state);
  }

  protected void sleep(int millis) {
    try {
      LOG.info("Wait {} ms!", millis);
      Thread.sleep(millis);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  protected org.exoplatform.social.core.identity.model.Identity createIdentity(String username) {
    org.exoplatform.social.core.identity.model.Identity identity = identityManager.getOrCreateIdentity
            (OrganizationIdentityProvider.NAME, username, true);
    if(identity.isDeleted() || !identity.isEnable()) {
      identity.setDeleted(false);
      identity.setEnable(true);
      identity = identityManager.updateIdentity(identity);
    }

    return identity;
  }

  protected void deleteAllIdentitiesWithActivities() throws Exception {
    ListAccess<org.exoplatform.social.core.identity.model.Identity> organizationIdentities = identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, new ProfileFilter(), false);
    Arrays.stream(organizationIdentities.load(0, organizationIdentities.getSize()))
            .forEach(identity -> {
              RealtimeListAccess<ExoSocialActivity> identityActivities = activityManager.getActivitiesWithListAccess(identity);
              Arrays.stream(identityActivities.load(0, identityActivities.getSize()))
                      .forEach(activity -> activityManager.deleteActivity(activity));
              identityManager.deleteIdentity(identity);
            });

    ListAccess<org.exoplatform.social.core.identity.model.Identity> spaceIdentities = identityManager.getIdentitiesByProfileFilter(SpaceIdentityProvider.NAME, new ProfileFilter(), false);
    Arrays.stream(spaceIdentities.load(0, spaceIdentities.getSize()))
            .forEach(identity -> {
              RealtimeListAccess<ExoSocialActivity> identityActivities = activityManager.getActivitiesOfSpaceWithListAccess(identity);
              Arrays.stream(identityActivities.load(0, identityActivities.getSize()))
                      .forEach(activity -> activityManager.deleteActivity(activity));
              identityManager.deleteIdentity(identity);
            });
  }

  protected void deleteAllSpaces() throws Exception {
    SpaceService spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    ListAccess<Space> spaces = spaceService.getAllSpacesWithListAccess();
    Arrays.stream(spaces.load(0, spaces.getSize())).forEach(space -> spaceService.deleteSpace(space));
  }

  protected void deleteAllRelationships() throws Exception {
    RelationshipManager relationshipManager = getContainer().getComponentInstanceOfType(RelationshipManager.class);
    IdentityManager identityManager = getContainer().getComponentInstanceOfType(IdentityManager.class);
    ListAccess<org.exoplatform.social.core.identity.model.Identity> identities = identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, new ProfileFilter(), true);
    for(org.exoplatform.social.core.identity.model.Identity identity : identities.load(0, identities.getSize())) {
      ListAccess<org.exoplatform.social.core.identity.model.Identity> relationships = relationshipManager.getAllWithListAccess(identity);
      Arrays.stream(relationships.load(0, relationships.getSize()))
              .forEach(relationship -> relationshipManager.deny(identity, relationship));
    }
  }
  protected Space createSpace(String prettyName, String displayName, String description, String creator, String[] members) throws Exception {
    Space space = new Space();
    space.setPrettyName(prettyName);
    displayName = displayName == null ? prettyName : displayName;
    space.setDisplayName(displayName);
    space.setDescription(description);
    space.setManagers(new String[] { creator });
    space.setMembers(members);
    space.setGroupId("/platform/users");
    space.setRegistration(Space.OPEN);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setUrl(space.getPrettyName());

    space = spaceService.createSpace(space,creator);
    return space;
  }
}

