package com.paulwithers.xots;

import org.openntf.domino.Database;
import org.openntf.domino.Document;
import org.openntf.domino.View;
import org.openntf.domino.ViewEntry;
import org.openntf.domino.ViewNavigator;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.utils.Factory.SessionType;
import org.openntf.domino.xots.Tasklet;
import org.openntf.domino.xsp.xots.AbstractXotsXspRunnable;

import com.paulwithers.Utils;

@Tasklet(session = Tasklet.Session.CLONE, context = Tasklet.Context.XSPSCOPED)
public class SchedTask extends AbstractXotsXspRunnable {

	public SchedTask() {

	}

	public void run() {
		try {
			Database currDb = Factory.getSession(SessionType.CURRENT).getCurrentDatabase();
			View contacts = currDb.getView("AllContacts");
			ViewNavigator nav = contacts.createViewNav();
			ViewEntry ent = nav.getFirst();
			for (int x = 0; x < 100; x++) {
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
	}

}
