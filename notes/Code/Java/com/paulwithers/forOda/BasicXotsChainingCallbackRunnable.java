package com.paulwithers.forOda;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.openntf.domino.xots.Tasklet;
import org.openntf.domino.xots.XotsUtil;
import org.openntf.domino.xsp.xots.AbstractXotsXspRunnable;

import com.ibm.domino.services.HttpServiceConstants;

@Tasklet(session = Tasklet.Session.CLONE, context = Tasklet.Context.XSPSCOPED)
public class BasicXotsChainingCallbackRunnable extends AbstractXotsXspRunnable {

	private IXotsXspChainingRunnableCallback callback;
	private Map<String, String> params;

	public BasicXotsChainingCallbackRunnable(IXotsXspChainingRunnableCallback callback, Map<String, String> params) {
		this.callback = callback;
		this.params = params;
	}

	@Override
	public void run() {
		try {
			URL url = new URL(params.get("redirectUrl"));
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", HttpServiceConstants.CONTENTTYPE_APPLICATION_JSON);

			callback.process(params, conn);

			conn.connect();
			if (conn.getResponseCode() == 201 || conn.getResponseCode() == 200) {
				System.out.println("Chain request sent successfully");
			} else {
				System.out.println("ERROR SENDING REQUEST: " + conn.getResponseCode());
			}
		} catch (Throwable t) {
			XotsUtil.handleException(t, getContext());
		}
	}

}
