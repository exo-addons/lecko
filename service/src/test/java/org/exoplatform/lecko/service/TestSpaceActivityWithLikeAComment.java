package org.exoplatform.lecko.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;

import org.exoplatform.addons.lecko.LeckoServiceController;
import org.exoplatform.addons.lecko.SimpleDataBuilder;
import org.exoplatform.lecko.test.AbstractServiceTest;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.model.Space;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 24/03/17.
 */
public class TestSpaceActivityWithLikeAComment extends AbstractServiceTest {

  private List<Space> tearDown         = new ArrayList<Space>();

  private String      spaceDisplayName = "General Discussions";

  private String      spacePrettyName = "general_discussions";
//  private SpaceService spaceService;
//  private IdentityManager identityManager;
//  private ActivityManager activityManager;
//  private JobStatusService jobStatusService;

  @Before
  public void setUp() throws Exception {
    super.setUp();

//    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
//    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
//    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
//    jobStatusService = (JobStatusService) getContainer().getComponentInstanceOfType(JobStatusService.class);

    // john post activity

    Identity johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", true);
    Identity maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", true);
    Identity jackIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo", true);

    Space space1 = createSpace(spacePrettyName,spaceDisplayName, "description", "john", new String[]{"mary","demo"});
    tearDown.add(space1);

    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space1.getPrettyName(), true);
    assertNotNull(spaceIdentity);

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("My Activity");
    activity.setUserId(johnIdentity.getId());
    activity.setType("DEFAULT_ACTIVITY");
    activityManager.saveActivityNoReturn(spaceIdentity, activity);

    // mary comments and likes the activity
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle("Mary's Comment");
    comment.setUserId(maryIdentity.getId());
    activityManager.saveComment(activity, comment);

    activityManager.saveLike(comment, jackIdentity);


    // Jack comments activity
    ExoSocialActivity commentJack = new ExoSocialActivityImpl();
    commentJack.setTitle("Jack's Comment");
    commentJack.setUserId(jackIdentity.getId());
    commentJack.setParentCommentId(comment.getId());
    activityManager.saveComment(activity, commentJack);

    activityManager.saveLike(commentJack, maryIdentity);



  }

  public void testBuildSpaceExportWithLikeAComment() throws Exception {

    String extractOutputPath = LeckoServiceController.getRootPath() + "/" + LeckoServiceController.getFileName();
    File file = new File(extractOutputPath);
    if (file.exists()) {
      file.delete();
    }

    SimpleDataBuilder dataBuilder = new SimpleDataBuilder(spaceService, identityManager, activityManager, jobStatusService, entityManagerService);
    dataBuilder.build();

    String fileContent = readFile(file);

    System.out.println(fileContent);

    String ls = System.getProperty("line.separator");
    String[] lines = fileContent.split(ls);

    // 1;DEFAULT_ACTIVITY;2017-03-30T10:12:36.743+02:00;space;general_discussions;General
    // Discussions;
    String[] line1 = lines[0].split(";");
    assertTrue(isInteger(line1[0]));
    assertEquals("DEFAULT_ACTIVITY", line1[1]);
    assertEquals("space", line1[3]);
    assertEquals("general_discussions", line1[4]);
    assertEquals("General Discussions", line1[5]);

    // 2;comment;2017-03-30T10:12:36.753+02:00;general_discussions;General
    // Discussions;
    String[] line2 = lines[1].split(";");
    assertTrue(isInteger(line2[0]));
    assertEquals("comment", line2[1]);
    assertEquals("general_discussions", line2[3]);
    assertEquals("General Discussions", line2[4]);

    // 3;like;2017-03-30T10:12:36.743+02:00;;
    String[] line3 = lines[2].split(";");
    assertTrue(isInteger(line3[0]));
    assertEquals("like", line3[1]);
    assertTrue(line3.length==4);

    // 4;comment;2017-03-30T10:12:36.773+02:00;general_discussions;General
    // Discussions;
    String[] line4 = lines[3].split(";");
    assertTrue(isInteger(line4[0]));
    assertEquals("comment", line4[1]);
    assertEquals("general_discussions", line4[3]);
    assertEquals("General Discussions", line4[4]);

    // 5;like;2017-03-30T10:12:36.743+02:00;;
    String[] line5 = lines[4].split(";");
    assertTrue(isInteger(line5[0]));
    assertEquals("like", line5[1]);
    assertTrue(line5.length==4);

  }

  @Override
  protected void tearDown() throws Exception {
    for (Space space : tearDown) {
      spaceService.deleteSpace(space);
    }

    jobStatusService.resetStatus();
    super.tearDown();
  }

  private String readFile(File file) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = null;
    StringBuilder stringBuilder = new StringBuilder();
    String ls = System.getProperty("line.separator");

    try {
      while ((line = reader.readLine()) != null) {
        stringBuilder.append(line);
        stringBuilder.append(ls);
      }

      return stringBuilder.toString();
    } finally {
      reader.close();
    }
  }

  public static boolean isInteger(String s) {
    try {
      Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return false;
    } catch (NullPointerException e) {
      return false;
    }
    // only got here if we didn't return false
    return true;
  }
}
