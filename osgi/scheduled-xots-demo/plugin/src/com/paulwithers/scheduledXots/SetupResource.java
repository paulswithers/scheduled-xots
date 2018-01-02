/*
 * Copyright 2017
 *
 * @author Paul Withers (pwithers@intec.co.uk)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package com.paulwithers.scheduledXots;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.openntf.domino.Database;
import org.openntf.domino.Session;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.utils.Factory.SessionType;

import com.ibm.commons.util.io.StreamUtil;
import com.ibm.commons.util.io.json.JsonJavaObject;

/**
 * @author Paul Withers
 * @since 1.0.0
 *
 *        Adds the database to the server, signs it and calls REST services to load data
 *        <em>protocol://server.host.name/optionalDbPath/scheduled-xots-demoApp/setup</em>
 *
 */
@Path("/setup")
public class SetupResource {

	/**
	 * Basic endpoint, using path of class with no additional URL path
	 *
	 * @return Json response with "Hello World " + current user + current
	 *         database path; or error
	 */
	@SuppressWarnings("restriction")
	@GET
	public Response getMessage() {
		final JsonJavaObject jjo = new JsonJavaObject();
		try {
			final Session session = Factory.getSession(SessionType.NATIVE);
			Database db = session.getDatabase("openntf-demo/scheduledXotsDemo.nsf");
			if (null != db) {
				throw new WebApplicationException(
						new Throwable("Database already found at location openntf-demo/scheduledXotsDemo.nsf"),
						Status.INTERNAL_SERVER_ERROR);
			}

			String dir = session.getEnvironmentString("directory", true);
			String s = File.separator;
			File folder = new File(dir + s + "openntf-demo");
			if (!folder.exists()) {
				folder.mkdir();
			}
			File dbFile = new File(dir + s + "openntf-demo" + s + "scheduledXotsDemo.nsf");
			FileOutputStream fs = new FileOutputStream(dbFile);
			InputStream is = SetupResource.class.getResourceAsStream("scheduledXotsDemo.nsf");
			StreamUtil.copyStream(is, fs);
			fs.flush();
			StreamUtil.close(is);
			StreamUtil.close(fs);

			db = session.getDatabase("openntf-demo/scheduledXotsDemo.nsf");
			if (null == db) {
				throw new WebApplicationException(
						new Throwable("Database could not be copied to location openntf-demo/scheduledXotsDemo.nsf"),
						Status.INTERNAL_SERVER_ERROR);
			}
			db.sign();

			final ResponseBuilder builder = Response.ok(jjo.toString(), MediaType.APPLICATION_JSON);
			return builder.build();
		} catch (final Exception e) {
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
	}

}