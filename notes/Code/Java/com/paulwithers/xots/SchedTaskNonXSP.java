package com.paulwithers.xots;

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

import org.openntf.domino.Database;
import org.openntf.domino.Document;
import org.openntf.domino.View;
import org.openntf.domino.ViewEntry;
import org.openntf.domino.ViewNavigator;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.utils.Factory.SessionType;
import org.openntf.domino.xots.AbstractXotsRunnable;
import org.openntf.domino.xots.Tasklet;

import com.paulwithers.Utils;

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
