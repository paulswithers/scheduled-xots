package com.paulwithers.forOda;

import java.util.Map;

import org.openntf.domino.xots.Tasklet;
import org.openntf.domino.xsp.xots.AbstractXotsXspRunnable;

@Tasklet(session = Tasklet.Session.CLONE, context = Tasklet.Context.XSPSCOPED)
public class BasicXotsCallbackRunnable extends AbstractXotsXspRunnable {

	private IXotsXspRunnableCallback callback;
	private Map<String, String> params;

	public BasicXotsCallbackRunnable(IXotsXspRunnableCallback callback, Map<String, String> params) {
		this.callback = callback;
		this.params = params;
	}

	@Override
	public void run() {
		callback.process(params);
	}

}
