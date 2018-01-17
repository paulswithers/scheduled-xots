package com.paulwithers;

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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openntf.domino.xots.Xots;

import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.xsp.webapp.XspHttpServletResponse;
import com.paulwithers.forOda.GenericHttpRequestUtils;
import com.paulwithers.forOda.IXspHttpServletJsonResponseCallback;
import com.paulwithers.xots.DataInitializerBackground;
import com.paulwithers.xots.DataInitializerBackground.InitializerType;

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
	 * Generic GET request method. Don't panic, take a deep breath, and follow me down the rabbit hole to Wonderland!
	 */
	public static void doGet() {
		try {
			/*
			 * One method call to Utils.initialiseAndProcessResponseAsJson() does all the boilerplating for extracting the request, response and JsonJavaObject
			 * and properly terminating the response. JsonJavaObject is basically the same as a Java Map. All gotchas are handled for you!
			 *
			 * The bit that could be new to most is that Utils.initialiseAndProcessResponseAsJson() takes an anonymous inner class as its method. This is just
			 * a way to pass in a process() method that can interact with the request and response set up by Utils.initialiseAndProcessResponseAsJson()
			 *
			 * With Java 8 it becomes more readable with lambdas:
			 *
			 * Utils.initialiseAndProcessResponseAsJson((request, response, jsonObj) -> {
			 * 	   // do stuff here
			 * });
			 */
			GenericHttpRequestUtils.initialiseAndProcessResponseAsJson(new IXspHttpServletJsonResponseCallback() {

				/* (non-Javadoc)
				 * @see com.paulwithers.IXspHttpServletResponseCallback#process(javax.servlet.http.HttpServletRequest, com.ibm.xsp.webapp.XspHttpServletResponse)
				 */
				public void process(HttpServletRequest request, XspHttpServletResponse response, JsonJavaObject jsonObj)
						throws IOException {

					// A. Has the user requested that the process runs asynchronously?
					boolean async = false;
					if (null != request.getParameter("doAsync")) {
						async = true;
						response.setStatus(HttpServletResponse.SC_ACCEPTED); // Tell the user it's queued, not processed
					}

					// B. Has the user included the "process" that should be run
					if (null == request.getParameter("process")) {
						// Invalid request!!
						response.sendError(HttpServletResponse.SC_BAD_REQUEST,
								"This endpoint expects a process type to be passed");
					} else {

						// We're good to go
						try {
							// pre-Java 8 we need an if statement. switch statements using strings are Java 7+, so FP10+
							if ("loadData".equals(request.getParameter("process"))) {
								// 1. LOAD ADDITIONAL USERS WITHOUT CLEARING THIS DATABASE DOWN
								// Was a number of users passed? We'll default to 200
								int userCount = 200;
								if (null != request.getParameter("userCount")) {
									userCount = Integer.parseInt(request.getParameter("userCount"));
								}
								if (async) {
									// Async request, run in background and confirm in JSON
									Xots.getService().submit(
											new DataInitializerBackground(InitializerType.LOAD, userCount));
									jsonObj.put("message", userCount + " users queued for asynchronous generation");
								} else {
									// In real-time request, run and confirm in JSON
									DataInitializer d = new DataInitializer();
									d.initUsers(userCount);
									d.initStates();
									d.initAllTypes();
									d.run();
									jsonObj.put("message", userCount + " users created");
								}
							} else if ("reloadData".equals(request.getParameter("process"))) {
								// 2. DELETE AND RELOAD ADDITIONAL USERS
								// Same as above, just including deletion. Was a number of users passed? We'll default to 200
								int userCount = 200;
								if (null != request.getParameter("userCount")) {
									userCount = Integer.parseInt(request.getParameter("userCount"));
								}
								if (async) {
									// Async request, run in background and confirm in JSON
									Xots.getService().submit(
											new DataInitializerBackground(InitializerType.RELOAD, userCount));
									jsonObj.put("message", "data clearance and " + userCount
											+ " users queued for asynchronous generation");
								} else {
									// In real-time request, run and confirm in JSON
									DataInitializer d = new DataInitializer();
									d.initDeleteDocuments();
									d.initUsers(userCount);
									d.initStates();
									d.initAllTypes();
									d.run();
									jsonObj.put("message", "data cleared and " + userCount + " users created");
								}
							} else if ("deleteData".equals(request.getParameter("process"))) {
								// 3. JUST CLEAR DOWN THE DATABASE. IF YOU WANT TO CLEAR DOWN THE ARCHIVE, GUESS WHAT - CALL THE REST SERVICE THERE!
								if (async) {
									// Async request, run in background and confirm in JSON
									Xots.getService().submit(new DataInitializerBackground(InitializerType.DELETE));
									jsonObj.put("message", "data clearance queued for asynchronous generation");
								} else {
									// In real-time request, run and confirm in JSON
									DataInitializer d = new DataInitializer();
									d.initDeleteDocuments();
									d.run();
									jsonObj.put("message", "data cleared");
								}
							} else {
								response.sendError(HttpServletResponse.SC_BAD_REQUEST,
										"Invalid process parameter passed");
							}

						} catch (Throwable t) {
							// Whoops! We've hit an unexpected error. Return an error 500
							t.printStackTrace();
							response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage());
						}
					}
				}
			});
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
