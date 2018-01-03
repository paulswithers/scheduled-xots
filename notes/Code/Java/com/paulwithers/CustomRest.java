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

import javax.servlet.http.HttpServletResponse;

import org.openntf.domino.xots.Xots;
import org.openntf.xrest.xsp.exec.Context;
import org.openntf.xrest.xsp.exec.CustomRestHandler;

import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.util.JsonWriter;
import com.paulwithers.xots.SchedTaskNonXSP;

/**
 * This is a CustomRestHandler for SmartNSF endpoint, making a Xots call and using sample code from SmartNSF
 * documentation
 * 
 * @author Paul Withers
 * 
 */
public class CustomRest implements CustomRestHandler {

	/* (non-Javadoc)
	 * @see org.openntf.xrest.xsp.exec.CustomRestHandler#processCall(org.openntf.xrest.xsp.exec.Context, java.lang.String)
	 */
	public void processCall(Context context, String path) throws Exception {
		// Schedule the Xots task. Note: we can't re-use the XPages one, there are differences in how SmartNSF works
		Xots.getService().submit(new SchedTaskNonXSP());
		// The rest is pretty standard for SmartNSF
		JsonJavaObject result = new JsonJavaObject();
		result.put("message", "asynchronous xrest task scheduled");
		JsonWriter jsw = new JsonWriter(context.getResponse().getWriter(), true);
		jsw.outObject(result);
		jsw.close();
		// One difference, we'll set the response code to 202 - a request was accepted for processing, but was not completed
		context.getResponse().setStatus(HttpServletResponse.SC_ACCEPTED);
	}

}
