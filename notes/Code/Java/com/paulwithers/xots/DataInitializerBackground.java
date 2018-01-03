package com.paulwithers.xots;

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

import org.openntf.domino.xsp.xots.AbstractXotsXspRunnable;

import extlib.DataInitializer;

public class DataInitializerBackground extends AbstractXotsXspRunnable {
	private InitializerType type;
	private int userCount;

	public static enum InitializerType {
		LOAD, RELOAD, DELETE;
	}

	public DataInitializerBackground() {
		this(InitializerType.LOAD, 200);
	}

	public DataInitializerBackground(InitializerType type) {
		this(type, 200);
	}

	public DataInitializerBackground(InitializerType type, int userCount) {
		this.type = type;
		this.userCount = userCount;
	}

	public void run() {
		try {
			DataInitializer d = new DataInitializer();
			if (!InitializerType.LOAD.equals(type)) {
				d.initDeleteDocuments();
			}
			if (!InitializerType.DELETE.equals(type)) {
				d.initUsers(userCount);
				d.initStates();
				d.initAllTypes();
			}
			d.run();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
