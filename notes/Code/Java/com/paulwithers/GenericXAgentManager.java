package com.paulwithers;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.xsp.webapp.XspHttpServletResponse;

import extlib.DataInitializer;

/**
 * This is a sophisticated example used by the GenericXAgent XPage. That XAgent calls the doGet method, passing the
 * parameters. Another XPage could do a POST request
 *
 * @author Paul Withers
 *
 */
public class GenericXAgentManager {

	/**
	 * Generic GET request method
	 */
	public static void doGet() {
		try {
			// Call initialiseAndProcessResponse passing an anonymous inner class that includes a process() method, which
			// initialiseAndProcessResponse will run. In Java 8, we can use a lambda making it much more readable, () -> {...}
			Utils.initialiseAndProcessResponse(new IXspHttpServletResponseCallback() {

				/* (non-Javadoc)
				 * @see com.paulwithers.IXspHttpServletResponseCallback#process(javax.servlet.http.HttpServletRequest, com.ibm.xsp.webapp.XspHttpServletResponse)
				 */
				public void process(HttpServletRequest request, XspHttpServletResponse response) throws IOException {
					Map params = request.getParameterMap(); // query string parameters
					if (!params.containsKey("process")) {
						response.sendError(HttpServletResponse.SC_BAD_REQUEST,
								"This endpoint expects a process type to be passed");
					} else {
						// pre-Java 8 we need an if statement. switch statements using string are Java 8 only
						if ("loadData".equals(params.get("process"))) {
							try {
								int userCount = 200;
								if (params.containsKey("userCount")) {
									userCount = Integer.parseInt((String) params.get("userCount"));
								}
								DataInitializer d = new DataInitializer();
								d.initUsers(userCount);
								d.initStates();
								d.initAllTypes();
								d.run();
							} catch (Throwable t) {
								t.printStackTrace();
								response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
							}
						} else if ("reloadData".equals(params.get("process"))) {
							// Same as above, just including deletion
							try {
								int userCount = 200;
								if (params.containsKey("userCount")) {
									userCount = Integer.parseInt((String) params.get("userCount"));
								}
								DataInitializer d = new DataInitializer();
								d.initDeleteDocuments();
								d.initUsers(userCount);
								d.initStates();
								d.initAllTypes();
								d.run();
							} catch (Throwable t) {
								t.printStackTrace();
								response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
							}
						} else if ("deleteData".equals(params.get("process"))) {
							// Just delete
							try {
								DataInitializer d = new DataInitializer();
								d.initDeleteDocuments();
								d.run();
							} catch (Throwable t) {
								t.printStackTrace();
								response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
							}
						} else {
							response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid process parameter passed");
						}
					}
				}
			});
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
