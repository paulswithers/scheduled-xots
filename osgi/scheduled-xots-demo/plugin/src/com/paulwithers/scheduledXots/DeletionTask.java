package com.paulwithers.scheduledXots;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.ws.rs.core.MediaType;

import org.openntf.domino.Database;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.utils.Factory.SessionType;
import org.openntf.domino.xots.AbstractXotsRunnable;
import org.openntf.domino.xots.Tasklet;
import org.openntf.domino.xots.Tasklet.Context;
import org.openntf.domino.xots.Tasklet.Session;

@Tasklet(context = Context.DEFAULT, session = Session.NATIVE)
public class DeletionTask extends AbstractXotsRunnable {
	private String redirectUrl;
	private String appId;
	private String appSecret;

	public DeletionTask(String redirectUrl, String appId, String appSecret) {
		this.redirectUrl = redirectUrl;
		this.appId = appId;
		this.appSecret = appSecret;
	}

	@Override
	public void run() {
		try {
			Database db = Factory.getSession(SessionType.CURRENT).getDatabase(Activator.LIVE_DATABASE_PATH);
			db.getAllDocuments().removeAll(true);
			Database archiveDb = Factory.getSession(SessionType.CURRENT).getDatabase(Activator.ARCHIVE_DATABASE_PATH);
			archiveDb.getAllDocuments().removeAll(true);

			URL url = new URL(redirectUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", MediaType.APPLICATION_JSON);
			conn.setRequestProperty("appId", appId);
			conn.setRequestProperty("appSecret", appSecret);
			conn.connect();
			if (conn.getResponseCode() == 201 || conn.getResponseCode() == 200) {
				System.out.println("Chain request sent successfully");
			} else {
				System.out.println("ERROR SENDING REQUEST: " + conn.getResponseCode());
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
