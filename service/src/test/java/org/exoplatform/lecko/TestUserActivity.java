package org.exoplatform.lecko;

import org.exoplatform.addons.lecko.LeckoServiceController;
import org.exoplatform.addons.lecko.SimpleDataBuilder;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.junit.Before;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Romain Dénarié (romain.denarie@exoplatform.com) on 24/03/17.
 */
public class TestUserActivity extends AbstractLeckoTestCase {

  @Before
  public void setUp() throws Exception {
    super.setUp();

    // john post activity
    Identity johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", true);
    Identity maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", true);
    Identity jackIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "jack", true);

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

    activityManager.saveLike(activity, maryIdentity);

    // Jack comments activity
    ExoSocialActivity commentJack = new ExoSocialActivityImpl();
    commentJack.setTitle("Jack's Comment");
    commentJack.setUserId(jackIdentity.getId());
    activityManager.saveComment(activity, commentJack);

  }

  public void testBuildUserExport() throws Exception {

    String extractOutputPath = LeckoServiceController.getRootPath() + "/" + LeckoServiceController.getFileName();
    File file = new File(extractOutputPath);
    if (file.exists()) {
      file.delete();
    }

    SimpleDataBuilder dataBuilder = new SimpleDataBuilder(spaceService, identityManager, activityManager, jobStatusService);
    dataBuilder.build();

    String fileContent = readFile(file);
    String ls = System.getProperty("line.separator");
    String[] lines = fileContent.split(ls);

    // 1;DEFAULT_ACTIVITY;2017-03-30T10:37:03.110+02:00;user;;
    String[] line1 = lines[0].split(";");
    assertTrue(isInteger(line1[0]));
    assertEquals("DEFAULT_ACTIVITY", line1[1]);
    assertEquals("user", line1[3]);
    // 2;comment;2017-03-30T10:37:03.113+02:00;;;
    String[] line2 = lines[1].split(";");
    assertTrue(isInteger(line2[0]));
    assertEquals("comment", line2[1]);
    // 3;comment;2017-03-30T10:37:03.126+02:00;;;7
    String[] line3 = lines[2].split(";");
    assertTrue(isInteger(line3[0]));
    assertEquals("comment", line3[1]);
    // 2;like;2017-03-30T10:37:03.110+02:00;;;
    String[] line4 = lines[3].split(";");
    assertTrue(isInteger(line4[0]));
    assertEquals("like", line4[1]);
    // 1;DEFAULT_ACTIVITY;2017-03-30T10:37:03.110+02:00;user;;
    String[] line5 = lines[4].split(";");
    assertTrue(isInteger(line5[0]));
    assertEquals("DEFAULT_ACTIVITY", line5[1]);
    assertEquals("user", line5[3]);
    // 2;comment;2017-03-30T10:37:03.113+02:00;;;
    String[] line6 = lines[5].split(";");
    assertTrue(isInteger(line6[0]));
    assertEquals("comment", line6[1]);
    // 3;comment;2017-03-30T10:37:03.126+02:00;;;
    String[] line7 = lines[6].split(";");
    assertTrue(isInteger(line7[0]));
    assertEquals("comment", line7[1]);
    // 2;like;2017-03-30T10:37:03.110+02:00;;;
    String[] line8 = lines[7].split(";");
    assertTrue(isInteger(line8[0]));
    assertEquals("like", line8[1]);
    // 1;DEFAULT_ACTIVITY;2017-03-30T10:37:03.110+02:00;user;;
    String[] line9 = lines[8].split(";");
    assertTrue(isInteger(line9[0]));
    assertEquals("DEFAULT_ACTIVITY", line9[1]);
    assertEquals("user", line9[3]);
    // 2;comment;2017-03-30T10:37:03.113+02:00;;;
    String[] line10 = lines[9].split(";");
    assertTrue(isInteger(line10[0]));
    assertEquals("comment", line10[1]);
    // 3;comment;2017-03-30T10:37:03.126+02:00;;;
    String[] line11 = lines[10].split(";");
    assertTrue(isInteger(line11[0]));
    assertEquals("comment", line11[1]);
    // 2;like;2017-03-30T10:37:03.110+02:00;;;
    String[] line12 = lines[11].split(";");
    assertTrue(isInteger(line4[0]));
    assertEquals("like", line12[1]);

  }

  protected void tearDown() throws Exception {
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
