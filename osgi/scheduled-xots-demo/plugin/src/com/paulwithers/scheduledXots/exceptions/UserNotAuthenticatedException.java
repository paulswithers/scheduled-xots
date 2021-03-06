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

/**
 * @author Paul Withers
 * @since 1.0.0
 *
 *        Use this Exception if the user is not authenticated. This is not
 *        designed to pass a message back to the REST service, but to throw the
 *        error within this servlet.
 */
public class UserNotAuthenticatedException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor allowing you to pass in a specific message
	 * 
	 * @param msg
	 *            String message to pass
	 */
	public UserNotAuthenticatedException(String msg) {
		super(msg);
	}
}
