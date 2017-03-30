/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
 *
 * This file is part of Lecko Analytics Add-on - Service.
 *
 * Lecko Analytics Add-on - Service is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * Lecko Analytics Add-on - Service software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Lecko Analytics Add-on - Service; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.lecko.social;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.ContainerResponseWriter;
import org.exoplatform.services.rest.impl.*;
import org.exoplatform.services.rest.tools.DummyContainerResponseWriter;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Sep
 * 29, 2015
 */
public class HttpUtilsMock {
  private static Log LOG = ExoLogger.getLogger(HttpUtilsMock.class);


  public static String get(String requestURI) throws Exception {
    byte[] data = null;
    Map<String, List<String>> headers=null;
    String baseURI= "";
    String method = "GET";
    requestURI=requestURI.replace("/rest/private","");

    ContainerResponseWriter writer = new DummyContainerResponseWriter();

    if (headers == null) {
      headers = new MultivaluedMapImpl();
    }

    ByteArrayInputStream in = null;
    if (data != null) {
      in = new ByteArrayInputStream(data);
    }

    EnvironmentContext envctx = new EnvironmentContext();
    HttpServletRequest httpRequest = new SocialMockHttpServletRequest("",
            in,
            in != null ? in.available() : 0,
            method,
            headers);
    envctx.put(HttpServletRequest.class, httpRequest);
    EnvironmentContext.setCurrent(envctx);
    ContainerRequest request = new ContainerRequest(method,
            new URI(requestURI),
            new URI(baseURI),
            in,
            new InputHeadersMap(headers));
    ContainerResponse response = new ContainerResponse(writer);

    RequestHandlerImpl requestHandler = (RequestHandlerImpl) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RequestHandlerImpl.class);
    requestHandler.handleRequest(request, response);
    return response.getEntity().toString();
  }

}
