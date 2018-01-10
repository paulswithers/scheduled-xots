package com.paulwithers.scheduledXots;

import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.openntf.domino.xots.Xots;

import com.ibm.commons.util.io.json.JsonJavaObject;
import com.paulwithers.scheduledXots.exceptions.UserNotAuthenticatedException;

/**
 * @author Paul Withers
 * @since 1.0.0
 *
 *        Moves data to the archive database
 *        <em>protocol://server.host.name/optionalDbPath/scheduled-xots-demo/archive</em>
 *
 */
@Path("/doChain")
public class ChainReinitialiseResource {

	@DELETE
	public Response deleteData(@HeaderParam(value = "appId") String appId,
			@HeaderParam(value = "appSecret") String appSecret,
			@HeaderParam(value = "redirectUrl") String redirectUrl) {
		try {
			if (!"1ugqjbq988301lb7t2oh2fsgg6".equals(appId) || !"pianfreuhe0u4ato38mfiibeng".equals(appSecret)) {
				throw new UserNotAuthenticatedException("Incorrect appId and appSecret passed");
			}

			DeletionTask deletion = new DeletionTask(redirectUrl, appId, appSecret);
			Xots.getService().submit(deletion);
			JsonJavaObject jjo = new JsonJavaObject();
			jjo.put("message", "asynchronous task scheduled");
			final ResponseBuilder builder = Response.ok(jjo.toString(), MediaType.APPLICATION_JSON);
			builder.status(Status.ACCEPTED);
			return builder.build();
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebApplicationException(e);
		}
	}

}
