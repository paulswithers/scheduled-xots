/*
 * Copyright 2017
 *
 * @author Paul Withers (pwithers@intec.co.uk)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package com.paulwithers.scheduledXots;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openntf.domino.AutoMime;
import org.openntf.domino.ext.Session.Fixes;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.utils.Factory.SessionType;
import org.openntf.domino.utils.Factory.ThreadConfig;
import org.openntf.domino.xsp.ODAPlatform;
import org.openntf.domino.xsp.session.DasCurrentSessionFactory;

import com.ibm.domino.services.AbstractRestServlet;

/**
 * @author Paul Withers
 * @since 1.0.0
 *
 *        Servlet filter,through which all requests to this servlet will run
 *
 */
public class ODADataServlet extends AbstractRestServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public ODADataServlet() {

	}

	/* (non-Javadoc)
	 * @see com.ibm.domino.services.AbstractRestServlet#doDestroy()
	 */
	@Override
	public void doDestroy() {
		super.doDestroy();
		ODAPlatform.stop();
	}

	/* (non-Javadoc)
	 * @see com.ibm.domino.services.AbstractRestServlet#doInit()
	 */
	@Override
	public void doInit() throws ServletException {
		ODAPlatform.start();
		super.doInit();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * com.ibm.domino.services.AbstractRestServlet#doService(javax.servlet.http.
	 * HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void doService(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		try {
			// Initialise the ODA Session Factory for this thread
			Factory.initThread(new ThreadConfig(Fixes.values(), AutoMime.WRAP_ALL, true));
			Factory.setSessionFactory(new DasCurrentSessionFactory(null), SessionType.CURRENT);
			// Add CORS Headers
			response.addHeader("Access-Control-Allow-Origin", "*");
			response.addHeader("Access-Control-Allow-Credentials", "true");
			response.addHeader("Access-Control-Allow-Methods", "POST");
			response.addHeader("Access-Control-Allow-Headers", "Content-Type");
			response.addHeader("Access-Control-Max-Age", "86400");
			super.doService(request, response);
			// Close ODA for this thread
		} finally {
			Factory.termThread();
		}
	}

}
