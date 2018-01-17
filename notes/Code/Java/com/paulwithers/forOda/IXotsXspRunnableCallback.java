package com.paulwithers.forOda;

import java.util.Map;


/**
 * This interface allows the developer to pass custom code to be run to a Xots task using HttpRequest parameters
 * 
 * @author Paul Withers
 * 
 */
public interface IXotsXspRunnableCallback {

	/**
	 * {@linkplain GenericHttpRequestUtils#initialiseAndProcessBackgroundTask(IXotsXspChainingRunnableCallback)} will extract the query params
	 * from the HttpServletRequest, pass them to a BasicXotsCallbackRunnable Xots task, then kick off that Xots task.
	 * 
	 * By using the callback approach custom code can be generated for a REST service without needing to remember to
	 * code the boilerplate Java code that should sit around it.
	 * 
	 * @param params
	 *            Map of queryString parameters
	 */
	public void process(Map<String, String> params);

}
