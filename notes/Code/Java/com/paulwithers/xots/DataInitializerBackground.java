package com.paulwithers.xots;

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
