package com.paulwithers;

import org.openntf.domino.Database;
import org.openntf.domino.Document;
import org.openntf.domino.View;
import org.openntf.domino.ViewEntry;
import org.openntf.domino.ViewNavigator;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.utils.Factory.SessionType;
import org.openntf.domino.xots.AbstractXotsRunnable;
import org.openntf.domino.xots.Tasklet;

@Tasklet(session = Tasklet.Session.NATIVE, context = Tasklet.Context.DEFAULT)
public class SchedTaskNonXSP extends AbstractXotsRunnable {

	public SchedTaskNonXSP() {

	}

	public void run() {
		try {
			Database currDb = Factory.getSession(SessionType.CURRENT).getDatabase("demos/BootstrapExtLibDemo.nsf");
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
