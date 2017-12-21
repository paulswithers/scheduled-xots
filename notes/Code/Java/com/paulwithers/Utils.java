package com.paulwithers;

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
	 * A generic method that performs boilerplate code to extract XspHttpServletRequest and HttpServletResponse, trigger
	 * a calllback method passed in, then terminate the response
	 *
	 * @param callback
	 *            anonymous inner class callback that implements IXspHttpServletResponse, so has a process() method that
	 *            can be called from here
	 * @throws IOException
	 *             that may be caused by manipulating the response
	 */
	public static void initialiseAndProcessResponse(IXspHttpServletResponseCallback callback)
			throws IOException {
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
