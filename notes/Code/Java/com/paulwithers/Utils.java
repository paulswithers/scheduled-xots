package com.paulwithers;

/*
 	Copyright 2018 Paul Withers Licensed under the Apache License, Version 2.0
	(the "License"); you may not use this file except in compliance with the
	License. You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
	or agreed to in writing, software distributed under the License is distributed
	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
	express or implied. See the License for the specific language governing
	permissions and limitations under the License

*/

import java.io.PrintWriter;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.openntf.domino.Database;
import org.openntf.domino.Document;
import org.openntf.domino.View;
import org.openntf.domino.ViewEntry;
import org.openntf.domino.ViewNavigator;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.utils.Factory.SessionType;
import org.openntf.domino.xots.Xots;

import com.ibm.xsp.webapp.XspHttpServletResponse;
import com.paulwithers.forOda.GenericHttpRequestUtils;
import com.paulwithers.xots.SchedTask;

/**
 * @author Paul Withers
 *
 *         Utility class for standalone XPages / REST calls
 *
 */
public class Utils {

	public static String CURRENT_DB_PATH = "openntf-demo/scheduledXotsDemo.nsf";
	public static String ARCHIVE_DB_PATH = "openntf-demo/scheduledXotsDemoArchive.nsf";

	/**
	 * This is triggered from the DataView
	 *
	 * @param ids
	 *            selected documents NoteIDs
	 */
	public static void archiveSelected(String[] ids) {
		try {
			for (String id : ids) {
				Database db = Factory.getSession(SessionType.CURRENT).getCurrentDatabase();
				Document doc = db.getDocumentByID(id);
				if (!archiveDoc(doc)) {
					System.out.println("FAILED!");
				} else {
					doc.remove(true);
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * Shared method
	 *
	 * @param doc
	 *            Document to be archived
	 * @return success or failure
	 */
	public static boolean archiveDoc(Document doc) {
		try {
			System.out.println("archiving doc " + doc.getUniversalID());
			Database archDb = Factory.getSession(SessionType.CURRENT).getDatabase(ARCHIVE_DB_PATH);
			Document archDoc = archDb.createDocument();
			doc.copyAllItems(archDoc, true);
			System.out.println("archived doc " + doc.getUniversalID());
			return archDoc.save();
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}

	/**
	 * XAgent called from beforeRenderResponse of SchedArchive XPage. Most basic code, using just a PrintWriter and
	 * writing JSON as a plain text string, returning basic 200 status code.
	 *
	 * This shows the most basic and should NEVER be used
	 */
	public static void processBackgroundTask() {
		try {
			FacesContext ctx = FacesContext.getCurrentInstance();
			ExternalContext ext = ctx.getExternalContext();
			XspHttpServletResponse response = (XspHttpServletResponse) ext.getResponse();
			response.setContentType("application/json");
			response.setHeader("Cache-Control", "no-cache");
			PrintWriter writer = response.getWriter();
			Xots.getService().submit(new SchedTask());
			// This line highlights why you should use a JsonWriter - painful and prone to error
			writer.write("{\"message\": \"asynchronous task running\"}");

			//  Terminate the request processing lifecycle.
			FacesContext.getCurrentInstance().responseComplete();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * XAgent called from beforeRenderResponse of SchedReallyEasyJavaArchive XPage. Easiest to write of all.
	 */
	public static void processBackgroundCallback() {
		GenericHttpRequestUtils.initialiseAndProcessBackgroundTask((Map<String, String> params) -> {
			try {
				// Iterate 100 entries from the AllContacts view and archive them
				Database currDb = Factory.getSession(SessionType.CURRENT).getCurrentDatabase();
				View contacts = currDb.getView("AllContacts");
				ViewNavigator nav = contacts.createViewNav();
				ViewEntry ent = nav.getFirst();
				Integer archCount = Integer.parseInt(params.get("userCount"));
				for (int x = 0; x < archCount; x++) {
					if (null == ent) {
						break;
					}
					ViewEntry next = nav.getNext();
					Document doc = ent.getDocument();
					if (Utils.archiveDoc(doc)) {
						doc.remove(true);
					}
					ent = next;
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}

		});
	}

	/**
	 * XAgent called from beforeRenderResponse of SchedReallyEasyJavaChain XPage. Easiest to write of all.
	 */
	public static void processBackgroundChainingCallback() {
		GenericHttpRequestUtils.initialiseProcessBackgroundTaskAndChain((params, conn) -> {
			try {
				// Iterate 100 entries from the AllContacts view and archive them
				Database currDb = Factory.getSession(SessionType.CURRENT).getCurrentDatabase();
				View contacts = currDb.getView("AllContacts");
				ViewNavigator nav = contacts.createViewNav();
				ViewEntry ent = nav.getFirst();
				Integer archCount = Integer.parseInt(params.get("userCount"));
				for (int x = 0; x < archCount; x++) {
					if (null == ent) {
						break;
					}
					ViewEntry next = nav.getNext();
					Document doc = ent.getDocument();
					if (Utils.archiveDoc(doc)) {
						doc.remove(true);
					}
					ent = next;
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		});
	}

}
