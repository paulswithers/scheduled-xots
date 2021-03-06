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
package com.paulwithers.scheduledXots.exceptions;

import com.ibm.domino.osgi.core.context.ContextInfo;

/**
 * @author Paul Withers
 * @since 1.0.0
 *
 *        Use this Exception if the URL is not used with the correct database
 *        context. For example, if the REST endpoint should only be used after a
 *        specific database path, throw this error if
 *        {@link ContextInfo#getUserDatabase()} is not that particular database.
 *        This is not designed to pass a message back to the REST service, but
 *        to throw the error within this servlet.
 */
public class DatabaseModuleException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor allowing you to pass in a specific message
	 * 
	 * @param msg
	 *            String message to pass
	 */
	public DatabaseModuleException(String msg) {
		super(msg);
	}
}
