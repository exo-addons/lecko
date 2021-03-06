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
import org.exoplatform.social.core.space.model.Space;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 24/03/17.
 */
public class TestUserActivityWithLikeAComment extends AbstractServiceTest {

  private List<Space> tearDown         = new ArrayList<Space>();

  @Before
  public void setUp() throws Exception {
    super.setUp();

    // john post activity

    Identity johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    Identity maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary");
    Identity jackIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo");



    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("My Activity");
    activity.setUserId(johnIdentity.getId());
    activity.setType("DEFAULT_ACTIVITY");
    activityManager.saveActivityNoReturn(johnIdentity, activity);

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

  public void testBuildUserExportWithLikeAComment() throws Exception {

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

    // 1;DEFAULT_ACTIVITY;2017-03-30T10:12:36.743+02:00;user;;
    // Discussions;
    String[] line1 = lines[0].split(";");
    assertTrue(isInteger(line1[0]));
    assertEquals("DEFAULT_ACTIVITY", line1[1]);
    assertEquals("user", line1[3]);
    assertEquals(4, line1.length);

    // 2;comment;2017-03-30T10:12:36.753+02:00;;
    String[] line2 = lines[1].split(";");
    assertTrue(isInteger(line2[0]));
    assertEquals("comment", line2[1]);
    assertEquals(3, line2.length);

    // 3;like;2017-03-30T10:12:36.743+02:00;;
    String[] line3 = lines[2].split(";");
    assertTrue(isInteger(line3[0]));
    assertEquals("like", line3[1]);
    assertEquals(3, line3.length);


    // 4;comment;2017-03-30T10:12:36.773+02:00;;
    String[] line4 = lines[3].split(";");
    assertTrue(isInteger(line4[0]));
    assertEquals("comment", line4[1]);
    assertEquals(3, line4.length);

    // 5;like;2017-03-30T10:12:36.743+02:00;;
    String[] line5 = lines[4].split(";");
    assertTrue(isInteger(line5[0]));
    assertEquals("like", line5[1]);
    assertEquals(3, line5.length);

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
