package com.yunmei.frame.servlet.reflect;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONRPCResult {
	/**
	 * Denotes that the call was a success
	 */
	public final static int CODE_SUCCESS = 0;

	/**
	 * Denotes that an exception was thrown on the server
	 */
	public final static int CODE_REMOTE_EXCEPTION = 490;

	public final static int CODE_ERR_NOAUTH = 595;
	/**
	 * Denotes that an error occured while parsing the request.
	 */
	public final static int CODE_ERR_PARSE = 590;

	/**
	 * Denotes (when calling a constructor) that no method was found with the
	 * given name/arguments.
	 */
	public final static int CODE_ERR_NOCONSTRUCTOR = 594;

	/**
	 * Denotes (when using a callable reference) that no method was found with
	 * the given name and number of arguments.
	 */
	public final static int CODE_ERR_NOMETHOD = 591;

	/**
	 * Denotes that an error occured while unmarshalling the request.
	 */
	public final static int CODE_ERR_UNMARSHALL = 592;

	/**
	 * Denotes that an error occured while marshalling the response.
	 */
	public final static int CODE_ERR_MARSHALL = 593;

	/**
	 * The error method shown when an error occured while parsing the request.
	 */
	public final static String MSG_ERR_PARSE = "couldn't parse request arguments";

	/**
	 * The error method shown when no constructor was found with the given name.
	 */

	/**
	 * The error method shown when no method was found with the given name and
	 * number of arguments.
	 */
	public final static String MSG_ERR_NOMETHOD = "method with the requested number of arguments not found (session may"
			+ " have timed out)";

	private Object result;
	private Object id;

	private int errorCode;

	public JSONRPCResult(int errorCode, Object id, Object o) {
		this.errorCode = errorCode;
		this.id = id;
		this.result = o;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public Object getId() {
		return id;
	}

	public Object getResult() {
		return result;
	}

	public String toString() {
		JSONObject o = new JSONObject();
		try {
			o.put("id", id);
			o.put(errorCode == CODE_SUCCESS ? "result" : "error", result);
			o.put("code", errorCode);
		} catch (JSONException e) {
			throw (RuntimeException) new RuntimeException(e.getMessage())
					.initCause(e);
		}
		return o.toString();
	}
}
