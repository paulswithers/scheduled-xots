package com.paulwithers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import com.ibm.xsp.webapp.XspHttpServletResponse;

/**
 * This interface allows the developer to pass custom code to be run to process the HttpServletRequest and populate the
 * XspHttpServletResponse
 *
 * @author Paul Withers
 *
 */
public interface IXspHttpServletResponseCallback {

	/**
	 * {@linkplain Utils#initialiseAndProcessResponse(IXspHttpServletResponseCallback)} will extract the
	 * HttpServletRequest and XspHttpServletResponse, call this process() method, then terminate the response.
	 *
	 * By using the callback approach custom code can be generated without needing to remember to code the boilerplate
	 * Java code that should sit around it.
	 *
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            XspHttpServletResponse that will be posted back to the browser, from which the getDelegate() method
	 *            gives access to the HttpServletResponse, if needed
	 */
	public void process(HttpServletRequest request, XspHttpServletResponse response) throws IOException;

}
