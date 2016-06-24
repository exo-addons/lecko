/*
 * Copyright (C) 2016 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.addons.lecko.rest;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.exoplatform.addons.lecko.DataBuilder;
import org.exoplatform.addons.lecko.LeckoServiceController;
import org.exoplatform.addons.lecko.SimpleDataBuilder;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com
 */
@RolesAllowed("users")
@Path("/lecko")
public class LeckoRestService implements ResourceContainer
{
   private static final Log LOG = ExoLogger.getLogger("org.exoplatform.addons.lecko.rest.LeckoRestService");
   @Path("create/{dumpName}")
   @GET
   public Response createOpp(
      @Context HttpServletRequest request,
      @PathParam("dumpName") String dumpName) throws Exception
   {
      new Thread(new Runnable()
      {
         @Override
         public void run()
         {
            DataBuilder builder = LeckoServiceController.getService(SimpleDataBuilder.class);
            try
            {
               builder.build();
            }
            catch (Exception ex)
            {
               LOG.error(ex.getMessage());
            }

         }
      }, "LeckoDumpService").start();
      return Response.status(Response.Status.OK).build();

   }


}
