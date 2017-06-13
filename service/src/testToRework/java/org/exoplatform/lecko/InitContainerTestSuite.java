package org.exoplatform.lecko;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.exoplatform.commons.testing.BaseExoContainerTestSuite;
import org.exoplatform.commons.testing.ConfigTestCase;

@RunWith(Suite.class)
@SuiteClasses({ TestSpaceActivity.class, TestUserActivity.class })
@ConfigTestCase(AbstractLeckoTestCase.class)
public class InitContainerTestSuite extends BaseExoContainerTestSuite {

  @BeforeClass
  public static void setUp() throws Exception {
    initConfiguration(InitContainerTestSuite.class);
    beforeSetup();
  }

  @AfterClass
  public static void tearDown() {
    afterTearDown();
  }
}
