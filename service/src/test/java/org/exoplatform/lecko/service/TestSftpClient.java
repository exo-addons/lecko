package org.exoplatform.lecko.service;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import org.exoplatform.addons.lecko.Utils.SftpClient;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.lecko.test.AbstractServiceTest;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;

@RunWith(MockitoJUnitRunner.class)
public class TestSftpClient extends AbstractServiceTest {
  @Mock
  private Appender            mockAppender;

  private static final String LECKO_HOST     = "exo.addons.lecko.SftpHost";

  private static final String LECKO_USER     = "exo.addons.lecko.SftpUser";

  private static final String LECKO_PASSWORD = "exo.addons.lecko.SftpPassword";

  @Before
  public void setup() {
    final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.addAppender(mockAppender);
  }

  @After
  public void teardown() throws Exception {
    final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    logger.detachAppender(mockAppender);
  }

  @Test
  public void shouldLogErrorsWhenMissingMandatoryParameters() throws Exception {
    // given -- no Host defined
    PropertyManager.setProperty(LECKO_USER, "monuser");
    PropertyManager.setProperty(LECKO_PASSWORD, "monpwd");

    // when
    SftpClient client = new SftpClient();

    // then
    ArgumentCaptor<ch.qos.logback.classic.spi.LoggingEvent> captorLoggingEvent =
                                                                               ArgumentCaptor.forClass(ch.qos.logback.classic.spi.LoggingEvent.class);

    // check errors messages correctly log
    verify(mockAppender, Mockito.times(2)).doAppend(captorLoggingEvent.capture());

    LoggingEvent loggingEvent = captorLoggingEvent.getValue();
    // Check log level error property
    assertThat(loggingEvent.getLevel(), is(Level.ERROR));
    // Check the message being logged
    assertThat(loggingEvent.getFormattedMessage(),
               is("Missing mandatory properties, SFTP Client is not configured ... so the Lecko dump file will not be send to the FTP server."));

    // check client.send return false
    assertFalse(client.send("toto.txt"));
  }

  @Test
  public void shouldNotLogErrorsWhenNoMissingMandatoryParameters() throws Exception {
    // given
    PropertyManager.setProperty(LECKO_HOST, "monhost");
    PropertyManager.setProperty(LECKO_USER, "monuser");
    PropertyManager.setProperty(LECKO_PASSWORD, "monpwd");

    // when
    SftpClient client = new SftpClient();

    // then
    ArgumentCaptor<ch.qos.logback.classic.spi.LoggingEvent> captorLoggingEvent =
                                                                               ArgumentCaptor.forClass(ch.qos.logback.classic.spi.LoggingEvent.class);

    // Verify no log append
    verify(mockAppender, Mockito.times(0)).doAppend(captorLoggingEvent.capture());

    // check client.send return false
    assertFalse(client.send("toto.txt"));
  }
}
