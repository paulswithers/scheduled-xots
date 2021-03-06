/*
 * Copyright 2018
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.openntf.domino.ACL;
import org.openntf.domino.ACL.Level;
import org.openntf.domino.ACLEntry;
import org.openntf.domino.Database;
import org.openntf.domino.Document;
import org.openntf.domino.NoteCollection;
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
 *        <em>protocol://server.host.name/optionalDbPath/scheduled-xots-demo/setup</em>
 *
 */
@Path("/setup")
public class SetupResource {

	/**
	 * Basic endpoint, using path of class with no additional URL path
	 *
	 * @return Json response with details of processing or error
	 */
	@SuppressWarnings("restriction")
	@GET
	public Response getMessage(@Context HttpServletRequest request) {
		final JsonJavaObject jjo = new JsonJavaObject();
		try {
			final Session session = Factory.getSession(SessionType.NATIVE);
			// Remove existing NSFs if found
			Database db = session.getDatabase(Activator.LIVE_DATABASE_PATH);
			if (null != db) {
				db.remove();
				Database tmpArch = session.getDatabase(Activator.ARCHIVE_DATABASE_PATH);
				if (null != tmpArch) {
					tmpArch.remove();
				}
			}

			// Create folder and NSF (backend)
			String dir = session.getEnvironmentString("directory", true);
			File folder = new File(dir + Activator.SEPARATOR + Activator.FOLDER);
			if (!folder.exists()) {
				folder.mkdir();
			}
			File dbFile = new File(dir + Activator.SEPARATOR + Activator.LIVE_DATABASE_PATH);
			FileOutputStream fs = new FileOutputStream(dbFile);
			InputStream is = SetupResource.class.getResourceAsStream("ScheduledXotsDemo.nsf");
			StreamUtil.copyStream(is, fs);
			fs.flush();
			StreamUtil.close(is);
			StreamUtil.close(fs);

			// Create the database
			db = session.getDatabase(Activator.LIVE_DATABASE_PATH);
			if (null == db) {
				throw new WebApplicationException(
						new Throwable("Database could not be copied to location " + Activator.LIVE_DATABASE_PATH),
						Status.INTERNAL_SERVER_ERROR);
			}

			// Sign the design notes.
			// database.sign only works on a workstation. AdministrationProcess.signWithServerID() throws not authorized
			// error from here
			NoteCollection nc = db.createNoteCollection(true);
			nc.buildCollection();
			for (int nid : nc.getNoteIDs()) {
				Document doc = db.getDocumentByID(nid);
				doc.sign();
				doc.save();
			}

			// Set ACL - temporarily set Anonymous access, so we can create data in the database without needing
			// username and password in HTTP REST service request
			ACL acl = db.getACL();
			String serverName = session.getEffectiveUserName();
			ACLEntry server = acl.createACLEntry(serverName, Level.MANAGER);
			server.setUserType(ACLEntry.TYPE_SERVER);
			ACLEntry defaultUser = acl.getEntry("-Default-");
			defaultUser.setLevel(Level.EDITOR);
			ACLEntry anon = acl.createACLEntry("Anonymous", Level.EDITOR);
			acl.save();

			// Create the archive and sign (signing is not instant, not sure it's synchronous, so may not have completed
			// on main database before creating the archive)
			Database archiveDb = db.createCopy(db.getServer(), Activator.ARCHIVE_DATABASE_PATH);
			if (null == archiveDb) {
				throw new WebApplicationException(
						new Throwable(
								"Database could not be copied to location " + Activator.ARCHIVE_DATABASE_PATH),
						Status.INTERNAL_SERVER_ERROR);
			}
			jjo.put("message", "created databases");

			// REST service call to XAgent in live, and set Anonymous to No Access
			String serverUrl = request.getScheme() + "://" + request.getServerName();
			URL url = new URL(
					serverUrl + "/" + Activator.LIVE_DATABASE_PATH
							+ "/genericXAgent.xsp?process=loadData&userCount=400");
			processUrl(jjo, url, true);
			acl = db.getACL();
			acl.getEntry("Anonymous").setLevel(Level.NOACCESS);
			acl.save();

			acl = archiveDb.getACL();
			acl.getEntry("Anonymous").setLevel(Level.NOACCESS);
			acl.save();

			// Output response
			final ResponseBuilder builder = Response.ok(jjo.toString(), MediaType.APPLICATION_JSON);
			return builder.build();
		} catch (final Exception e) {
			e.printStackTrace();
			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	private void processUrl(final JsonJavaObject jjo, URL url, boolean isLive) throws IOException, ProtocolException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);
		conn.connect();
		String jsonElem = "live";
		if (!isLive) {
			jsonElem = "archive";
		}
		jjo.put("response-" + jsonElem, conn.getResponseCode());
		if (conn.getResponseCode() == 201 || conn.getResponseCode() == 200) {
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
			jjo.put("data-" + jsonElem, sb.toString());
		} else {

		}
	}

}