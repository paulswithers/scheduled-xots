package com.paulwithers;

import org.openntf.domino.xots.Xots;
import org.openntf.xrest.xsp.exec.Context;
import org.openntf.xrest.xsp.exec.CustomRestHandler;

import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.util.JsonWriter;
import com.paulwithers.xots.SchedTaskNonXSP;

public class CustomRest implements CustomRestHandler {

	public void processCall(Context context, String path) throws Exception {

		Xots.getService().submit(new SchedTaskNonXSP());
		JsonJavaObject result = new JsonJavaObject();
		result.put("message", "asynchronous xrest task scheduled");
		JsonWriter jsw = new JsonWriter(context.getResponse().getWriter(), true);
		jsw.outObject(result);
		jsw.close();
	}

}
