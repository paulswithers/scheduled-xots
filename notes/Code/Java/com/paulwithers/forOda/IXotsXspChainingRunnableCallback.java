package com.paulwithers.forOda;

import java.net.HttpURLConnection;
import java.util.Map;

/**
 * This interface allows the developer to pass custom code to be run to a Xots task using HttpRequest parameters
 * 
 * @author Paul Withers
 * 
 */
public interface IXotsXspChainingRunnableCallback {

	/**
	 * {@linkplain GenericHttpRequestUtils#initialiseAndProcessBackgroundTask(IXotsXspRunnableCallback)} will extract
	 * the query params from the HttpServletRequest, pass them to a BasicXotsCallbackRunnable Xots task, then kick off
	 * that Xots task.
	 * 
	 * By using the callback approach custom code can be generated for a REST service without needing to remember to
	 * code the boilerplate Java code that should sit around it.
	 * 
	 * At the end it will call a REST service using the URL in the redirectUrl query parameter passed to this XAgent
	 * 
	 * @param params
	 *            Map of queryString parameters
	 * @param conn
	 *            HTTP URL Connection into which any custom parameters can be added
	 */
	public void process(Map<String, String> params, HttpURLConnection conn);

}
