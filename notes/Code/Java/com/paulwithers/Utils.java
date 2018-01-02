package com.paulwithers;

/*
 	Copyright 2017 Paul Withers Licensed under the Apache License, Version 2.0
	(the "License"); you may not use this file except in compliance with the
	License. You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
	or agreed to in writing, software distributed under the License is distributed
	on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
	express or implied. See the License for the specific language governing
	permissions and limitations under the License
	
*/

import java.io.IOException;
import java.io.PrintWriter;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.openntf.domino.Database;
import org.openntf.domino.Document;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.utils.Factory.SessionType;
import org.openntf.domino.xots.Xots;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.util.JsonWriter;
import com.ibm.domino.services.HttpServiceConstants;
import com.ibm.xsp.webapp.XspHttpServletResponse;
import com.paulwithers.xots.SchedTask;

/**
 * @author Paul Withers
 * 
 *         Utility class for standalone XPages / REST calls
 * 
 */
public class Utils {

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
	 *            Docuemnt to be archived
	 * @return success or failure
	 */
	public static boolean archiveDoc(Document doc) {
		try {
			System.out.println("archiving doc " + doc.getUniversalID());
			Database archDb = Factory.getSession(SessionType.CURRENT).getCurrentDatabase();
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
	 * A generic method that performs boilerplate code to extract XspHttpServletRequest and HttpServletResponse;
	 * triggers a callback method passed in giving it access to the request, response and a JsonJavaObject; then closes
	 * everything down successfully
	 * 
	 * @param callback
	 *            anonymous inner class callback that implements IXspHttpServletResponse, so has a process() method that
	 *            can be called from here
	 * @throws IOException
	 *             that may be caused by manipulating the response
	 * @throws JsonException
	 *             caused by malformed JSON, shouldn't happen
	 */
	public static void initialiseAndProcessResponseAsJson(IXspHttpServletJsonResponseCallback callback)
			throws IOException, JsonException {
		FacesContext ctx = FacesContext.getCurrentInstance();
		ExternalContext ext = ctx.getExternalContext();
		XspHttpServletResponse response = (XspHttpServletResponse) ext.getResponse();
		response.setContentType(HttpServiceConstants.CONTENTTYPE_APPLICATION_JSON);
		response.setHeader("Cache-Control", "no-cache");
		JsonJavaObject result = new JsonJavaObject();
		callback.process((HttpServletRequest) ext.getRequest(), response, result);
		JsonWriter jsw = new JsonWriter(response.getWriter(), true);
		jsw.outObject(result);
		jsw.close();
		//  Terminate the request processing lifecycle.
		FacesContext.getCurrentInstance().responseComplete();
	}

	/**
	 * A more basic generic method that performs boilerplate code to extract XspHttpServletRequest and
	 * HttpServletResponse; triggers a callback method passed in, passing it the request and response; then terminates
	 * the response
	 * 
	 * It's down to you to handle printing something to the response
	 * 
	 * @param callback
	 *            anonymous inner class callback that implements IXspHttpServletResponse, so has a process() method that
	 *            can be called from here
	 * @throws IOException
	 *             that may be caused by manipulating the response
	 */
	public static void initialiseAndProcessResponse(IXspHttpServletResponseCallback callback) throws IOException {
		FacesContext ctx = FacesContext.getCurrentInstance();
		ExternalContext ext = ctx.getExternalContext();
		XspHttpServletResponse response = (XspHttpServletResponse) ext.getResponse();
		response.setContentType(HttpServiceConstants.CONTENTTYPE_APPLICATION_JSON);
		response.setHeader("Cache-Control", "no-cache");
		callback.process((HttpServletRequest) ext.getRequest(), response);
		//  Terminate the request processing lifecycle.
		FacesContext.getCurrentInstance().responseComplete();
	}

}
