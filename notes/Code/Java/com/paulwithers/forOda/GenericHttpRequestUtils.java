package com.paulwithers.forOda;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openntf.domino.xots.Xots;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.domino.services.HttpServiceConstants;
import com.ibm.xsp.webapp.XspHttpServletResponse;

public class GenericHttpRequestUtils {

	/**
	 * XAgent called from beforeRenderResponse of SchedReallyEasyJavaChain XPage. Most basic code, using just a
	 * PrintWriter and writing JSON as a plain text string, returning basic 200 status code.
	 *
	 * This shows the most basic and should NEVER be used
	 */
	public static void initialiseProcessBackgroundTaskAndChain(IXotsXspChainingRunnableCallback callback) {
		try {
			FacesContext ctx = FacesContext.getCurrentInstance();
			ExternalContext ext = ctx.getExternalContext();
			HttpServletRequest request = (HttpServletRequest) ext.getRequest();
			Enumeration queryParams = request.getParameterNames();
			Map<String, String> params = new HashMap<String, String>();
			while (queryParams.hasMoreElements()) {
				String key = (String) queryParams.nextElement();
				params.put(key, request.getParameter(key));
			}
			BasicXotsChainingCallbackRunnable xotsTask = new BasicXotsChainingCallbackRunnable(callback, params);
			Xots.getService().submit(xotsTask);

			XspHttpServletResponse response = (XspHttpServletResponse) ext.getResponse();
			response.setContentType("application/json");
			response.setHeader("Cache-Control", "no-cache");
			PrintWriter writer = response.getWriter();
			// This line highlights why you should use a JsonWriter - painful and prone to error
			writer.write("{\"message\": \"asynchronous task running\"}");

			//  Terminate the request processing lifecycle.
			FacesContext.getCurrentInstance().responseComplete();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * XAgent called from beforeRenderResponse of SchedReallyEasyJavaArchive XPage. Most basic code, using just a
	 * PrintWriter and writing JSON as a plain text string, returning basic 200 status code.
	 *
	 * This shows the most basic and should NEVER be used
	 */
	public static void initialiseAndProcessBackgroundTask(IXotsXspRunnableCallback callback) {
		try {
			FacesContext ctx = FacesContext.getCurrentInstance();
			ExternalContext ext = ctx.getExternalContext();
			HttpServletRequest request = (HttpServletRequest) ext.getRequest();
			Enumeration queryParams = request.getParameterNames();
			Map<String, String> params = new HashMap<String, String>();
			while (queryParams.hasMoreElements()) {
				String key = (String) queryParams.nextElement();
				params.put(key, request.getParameter(key));
			}
			BasicXotsCallbackRunnable xotsTask = new BasicXotsCallbackRunnable(callback, params);
			Xots.getService().submit(xotsTask);

			XspHttpServletResponse response = (XspHttpServletResponse) ext.getResponse();
			response.setContentType("application/json");
			response.setHeader("Cache-Control", "no-cache");
			PrintWriter writer = response.getWriter();
			// This line highlights why you should use a JsonWriter - painful and prone to error
			writer.write("{\"message\": \"asynchronous task running\"}");

			//  Terminate the request processing lifecycle.
			FacesContext.getCurrentInstance().responseComplete();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * A more basic generic method that performs boilerplate code to extract XspHttpServletRequest and
	 * HttpServletResponse; triggers a callback method passed in, passing it the request and response; then terminates
	 * the response
	 *
	 * It's down to you to handle printing something to the response
	 *
	 * @param callback
	 *            anonymous inner class callback that implements IXspHttpServletResponse, so has a process() method that
	 *            can be called from here
	 * @throws IOException
	 *             that may be caused by manipulating the response
	 */
	public static void initialiseAndProcessResponse(IXspHttpServletResponseCallback callback) throws IOException {
		FacesContext ctx = FacesContext.getCurrentInstance();
		ExternalContext ext = ctx.getExternalContext();
		XspHttpServletResponse response = (XspHttpServletResponse) ext.getResponse();
		response.setContentType(HttpServiceConstants.CONTENTTYPE_APPLICATION_JSON);
		response.setHeader("Cache-Control", "no-cache");
		callback.process((HttpServletRequest) ext.getRequest(), response);
		//  Terminate the request processing lifecycle.
		FacesContext.getCurrentInstance().responseComplete();
	}

	/**
	 * A generic method that performs boilerplate code to extract XspHttpServletRequest and HttpServletResponse;
	 * triggers a callback method passed in giving it access to the request, response and a JsonJavaObject; then closes
	 * everything down successfully
	 *
	 * @param callback
	 *            anonymous inner class callback that implements IXspHttpServletResponse, so has a process() method that
	 *            can be called from here
	 * @throws IOException
	 *             that may be caused by manipulating the response
	 * @throws JsonException
	 *             caused by malformed JSON, shouldn't happen
	 */
	public static void initialiseAndProcessResponseAsJson(IXspHttpServletJsonResponseCallback callback)
			throws IOException, JsonException {
		FacesContext ctx = FacesContext.getCurrentInstance();
		ExternalContext ext = ctx.getExternalContext();
		XspHttpServletResponse response = (XspHttpServletResponse) ext.getResponse();
		response.setContentType(HttpServiceConstants.CONTENTTYPE_APPLICATION_JSON);
		response.setHeader("Cache-Control", "no-cache");
		JsonJavaObject result = new JsonJavaObject();
		callback.process((HttpServletRequest) ext.getRequest(), response, result);
		if (!response.isStatusSet()) {
			response.setStatus(HttpServletResponse.SC_OK);
		}
		PrintWriter writer = response.getWriter();
		writer.write(result.toString());
		//  Terminate the request processing lifecycle.
		FacesContext.getCurrentInstance().responseComplete();
	}

}
