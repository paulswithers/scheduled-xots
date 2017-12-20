package com.paulwithers;

import java.io.PrintWriter;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.openntf.domino.Database;
import org.openntf.domino.Document;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.utils.Factory.SessionType;
import org.openntf.domino.xots.Xots;

import com.ibm.xsp.webapp.XspHttpServletResponse;
import com.paulwithers.xots.SchedTask;

public class Utils {

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

	public static boolean archiveDoc(Document doc) {
		try {
			System.out.println("archiving doc " + doc.getUniversalID());
			Database archDb = Factory.getSession(SessionType.CURRENT).getDatabase("demos/BootstrapExtLibDemoArchive");
			Document archDoc = archDb.createDocument();
			doc.copyAllItems(archDoc, true);
			System.out.println("archived doc " + doc.getUniversalID());
			return archDoc.save();
		} catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
	}

	public static void processBackgroundTask() {
		try {
			FacesContext ctx = FacesContext.getCurrentInstance();
			ExternalContext ext = ctx.getExternalContext();
			XspHttpServletResponse response = (XspHttpServletResponse) ext.getResponse();
			response.setContentType("application/json");
			response.setHeader("Cache-Control", "no-cache");
			PrintWriter writer = response.getWriter();
			Xots.getService().submit(new SchedTask());
			writer.write("{\"message\": \"asynchronouos task running\"}");

			//  Terminate the request processing lifecycle.
			FacesContext.getCurrentInstance().responseComplete();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
