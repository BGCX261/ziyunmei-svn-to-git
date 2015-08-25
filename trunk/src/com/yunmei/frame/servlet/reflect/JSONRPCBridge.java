package com.yunmei.frame.servlet.reflect;

import java.io.Serializable;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.yunmei.frame.exception.BOException;
import com.yunmei.frame.exception.MarshallException;
import com.yunmei.frame.exception.UnmarshallException;
import com.yunmei.frame.servlet.serializer.JSONSerializer;
import com.yunmei.frame.servlet.serializer.Serializer;
import com.yunmei.frame.servlet.serializer.SerializerState;
import com.yunmei.frame.utils.$;

public class JSONRPCBridge implements Serializable {

	private static class ObjectInstance implements Serializable {
		private final static long serialVersionUID = 2;
		private final Object object;
		private final Class clazz;

		public ObjectInstance(Object object) {
			this.object = object;
			this.clazz = object.getClass();
		}

		public ObjectInstance(Object object, Class clazz) {
			if (!clazz.isInstance(object)) {
				throw new ClassCastException(
						"Attempt to register jsonrpc object with invalid class.");
			}
			this.object = object;
			this.clazz = clazz;
		}

		public Class getClazz() {
			return clazz;
		}

		public Object getObject() {
			return object;
		}
	}

	private static final Map primitiveRankings;

	static {
		int counter = 0;
		primitiveRankings = new HashMap();
		primitiveRankings.put("byte", new Integer(counter++));
		primitiveRankings.put("short", new Integer(counter++));
		primitiveRankings.put("int", new Integer(counter++));
		primitiveRankings.put("long", new Integer(counter++));
		primitiveRankings.put("float", new Integer(counter++));
		primitiveRankings.put("double", new Integer(counter++));
		primitiveRankings.put("boolean", new Integer(counter++));
	}

	public static JSONRPCResult invokeAccessibleObject(
			AccessibleObject accessibleObject, Object context[],
			JSONArray arguments, Object javascriptObject, Object requestId,
			JSONSerializer serializer) {
		JSONRPCResult result;
		// Call the method
		try {
			final Class[] parameterTypes;
			parameterTypes = ((Method) accessibleObject).getParameterTypes();
			// TODO 得到范型信息

			Class clazz = $.getTarget(javascriptObject);
			String parameterType = $.getGenericParameterType(clazz,
					((Method) accessibleObject).getName(), parameterTypes);
			if (parameterType == null)
				$.setGenericParameterType("java.util.HashMap");
			else {
				$.setGenericParameterType(parameterType);
				if (log.isDebugEnabled())
					log
							.debug("泛型类型:"
									+ $.getGenericParameterType());
			}
			// Unmarshall arguments
			final Object javaArgs[] = unmarshallArgs(context, parameterTypes,
					arguments, serializer);

			// Invoke the method
			final Object returnObj = ((Method) accessibleObject).invoke(
					javascriptObject, javaArgs);
			// Marshall the result
			final SerializerState serializerState = new SerializerState();

			final Object json = serializer.marshall(serializerState, null,
					returnObj, "r");
			result = new JSONRPCResult(JSONRPCResult.CODE_SUCCESS, requestId,
					json);
		} catch (UnmarshallException e) {
			result = new JSONRPCResult(JSONRPCResult.CODE_ERR_UNMARSHALL,
					requestId, "解析参数失败");
		} catch (MarshallException e) {
			result = new JSONRPCResult(JSONRPCResult.CODE_ERR_MARSHALL,
					requestId, "编码返回参数失败");
		} catch (Throwable e) {
			String errorMsg = "未知错误,请联系管理员";
			if (e instanceof InvocationTargetException) {
				errorMsg = ((InvocationTargetException) e).getTargetException()
						.getMessage();
			}
			result = new JSONRPCResult(JSONRPCResult.CODE_REMOTE_EXCEPTION,
					requestId, errorMsg);
		}
		return result;
	}

	private static AccessibleObject resolveMethod(Map methodMap,
			String methodName, JSONArray arguments, JSONSerializer serializer) {
		AccessibleObjectKey mk = new AccessibleObjectKey(methodName, arguments
				.length());
		List accessibleObjects = (List) methodMap.get(mk);
		if (accessibleObjects == null || accessibleObjects.size() == 0) {
			return null;
		} else if (accessibleObjects.size() == 1) {
			return (AccessibleObject) accessibleObjects.get(0);
		} else {
			List candidate = new ArrayList();
			for (int i = 0; i < accessibleObjects.size(); i++) {
				AccessibleObject accessibleObject = (AccessibleObject) accessibleObjects
						.get(i);
				Class[] parameterTypes = null;
				parameterTypes = ((Method) accessibleObject)
						.getParameterTypes();
				try {
					candidate.add(tryUnmarshallArgs(accessibleObject,
							arguments, parameterTypes, serializer));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			AccessibleObjectCandidate best = null;
			for (int i = 0; i < candidate.size(); i++) {
				AccessibleObjectCandidate c = (AccessibleObjectCandidate) candidate
						.get(i);
				if (best == null) {
					best = c;
					continue;
				}
				final ObjectMatch bestMatch = best.getMatch();
				final ObjectMatch cMatch = c.getMatch();
				if (bestMatch.getMismatch() > cMatch.getMismatch()) {
					best = c;
				} else if (bestMatch.getMismatch() == cMatch.getMismatch()) {
					best = betterSignature(best, c);
				}
			}
			if (best != null) {
				return best.getAccessibleObject();
			}
		}
		return null;
	}

	private static AccessibleObjectCandidate betterSignature(
			AccessibleObjectCandidate methodCandidate,
			AccessibleObjectCandidate methodCandidate1) {
		final Class[] parameters = methodCandidate.getParameterTypes();
		final Class[] parameters1 = methodCandidate1.getParameterTypes();

		int c = 0, c1 = 0;
		for (int i = 0; i < parameters.length; i++) {
			final Class parameterClass = parameters[i];
			final Class parameterClass1 = parameters1[i];
			if (parameterClass != parameterClass1) {
				if (parameterClass.isPrimitive()
						&& parameterClass1.isPrimitive()) {
					if (((Integer) primitiveRankings.get(parameterClass
							.getName())).intValue() < ((Integer) primitiveRankings
							.get(parameterClass1.getName())).intValue()) {
						c++;
					} else {
						c1++;
					}
				} else if (parameterClass.isAssignableFrom(parameterClass1)) {
					c1++;
				} else {
					c++;
				}
			}
		}
		if (c1 > c) {
			return methodCandidate1;
		}
		return methodCandidate;
	}

	private static AccessibleObjectCandidate tryUnmarshallArgs(
			AccessibleObject accessibleObject, JSONArray arguments,
			Class[] parameterTypes, JSONSerializer serializer)
			throws UnmarshallException {
		int i = 0;
		ObjectMatch[] matches = new ObjectMatch[parameterTypes.length];
		try {
			int nonLocalArgIndex = 0;
			for (; i < parameterTypes.length; i++) {
				SerializerState serialiserState = new SerializerState();
				matches[i] = serializer.tryUnmarshall(serialiserState,
						parameterTypes[i], arguments.get(nonLocalArgIndex++));
			}
		} catch (JSONException e) {
			throw (NoSuchElementException) new NoSuchElementException(e
					.getMessage()).initCause(e);
		} catch (UnmarshallException e) {
			throw new UnmarshallException("arg " + (i + 1) + " "
					+ e.getMessage(), e);
		}
		return new AccessibleObjectCandidate(accessibleObject, parameterTypes,
				matches);
	}

	private static Object[] unmarshallArgs(Object context[], Class[] param,
			JSONArray arguments, JSONSerializer serializer)
			throws UnmarshallException {
		Object javaArgs[] = new Object[param.length];
		int i = 0, j = 0;
		try {
			for (; i < param.length; i++) {
				SerializerState serializerState = new SerializerState();
				javaArgs[i] = serializer.unmarshall(serializerState, param[i],
						arguments.get(j++));
			}
		} catch (JSONException e) {
			throw (NoSuchElementException) new NoSuchElementException(e
					.getMessage()).initCause(e);
		} catch (UnmarshallException e) {
			throw new UnmarshallException("arg " + (i + 1)
					+ " could not unmarshall", e);
		}
		return javaArgs;
	}

	private final static long serialVersionUID = 2;
	private final static Logger log = Logger.getLogger(JSONRPCBridge.class);

	private final static JSONRPCBridge globalBridge = new JSONRPCBridge();

	private static JSONSerializer ser = new JSONSerializer();

	static {
		try {
			ser.registerDefaultSerializers();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static JSONRPCBridge getGlobalBridge() {
		return globalBridge;
	}

	public static JSONSerializer getSerializer() {
		return ser;
	}

	public static void setSerializer(JSONSerializer ser) {
		JSONRPCBridge.ser = ser;
	}

	private final Map classMap;

	private final Map objectMap;

	public JSONRPCBridge() {
		classMap = new HashMap();
		objectMap = new HashMap();
	}

	public JSONRPCResult call(Object context[], JSONObject jsonReq) {
		// #1: Parse the request
		final String encodedMethod;
		final Object requestId;
		final JSONArray arguments;
		try {
			encodedMethod = jsonReq.getString("method");
			arguments = jsonReq.getJSONArray("params");
			requestId = jsonReq.opt("id");
		} catch (JSONException e) {
			log.error("no method or parameters in request");
			return new JSONRPCResult(JSONRPCResult.CODE_ERR_NOMETHOD, null,
					JSONRPCResult.MSG_ERR_NOMETHOD);
		}
		final String className;
		final String methodName;
		{
			StringTokenizer t = new StringTokenizer(encodedMethod, ".");
			if (t.hasMoreElements()) {
				className = t.nextToken();
			} else {
				className = null;
			}
			if (t.hasMoreElements()) {
				methodName = t.nextToken();
			} else {
				methodName = null;
			}
		}

		final Map methodMap;
		final Object javascriptObject;
		final AccessibleObject ao;

		javascriptObject = getObjectContext(className);
		methodMap = getAccessibleObjectMap(className, methodName);
		ao = resolveMethod(methodMap, methodName, arguments, ser);
		if (ao == null) {
			return new JSONRPCResult(JSONRPCResult.CODE_ERR_NOMETHOD,
					requestId, "没有所请求的方法");
		}
		JSONRPCResult r = invokeAccessibleObject(ao, context, arguments,
				javascriptObject, requestId, ser);
		return r;
	}

	public void registerObject(Object key, Object o) {
		ObjectInstance oi = new ObjectInstance(o);
		synchronized (objectMap) {
			objectMap.put(key, oi);
		}
		if (log.isDebugEnabled()) {
			log.debug("registered object " + o.hashCode() + " of class "
					+ o.getClass().getName() + " as " + key);
		}
	}

	public void registerObject(Object key, Object o, Class interfaceClass) {
		ObjectInstance oi = new ObjectInstance(o, interfaceClass);
		synchronized (objectMap) {
			objectMap.put(key, oi);
		}
		if (log.isDebugEnabled()) {
			log.debug("registered object " + o.hashCode() + " of class "
					+ interfaceClass.getName() + " as " + key);
		}
	}

	public void registerSerializer(Serializer serializer) throws Exception {
		ser.registerSerializer(serializer);
	}

	@SuppressWarnings("unchecked")
	private Map getAccessibleObjectMap(final String className,
			final String methodName) {
		final Map methodMap = new HashMap();
		final ObjectInstance oi = resolveObject(className);
		final ClassData classData = resolveClass(className);
		methodMap.putAll(ClassAnalyzer.getClassData(oi.getClazz())
				.getMethodMap());
		return methodMap;
	}

	private Object getObjectContext(final String className) {

		final ObjectInstance oi = resolveObject(className);

		return oi != null ? oi.getObject() : null;
	}

	private ClassData resolveClass(String className) {
		Class clazz;
		ClassData cd = null;
		synchronized (classMap) {
			clazz = (Class) classMap.get(className);
		}
		if (clazz != null) {
			cd = ClassAnalyzer.getClassData(clazz);
		}
		if (cd != null) {
			return cd;
		}
		if (this != globalBridge) {
			return globalBridge.resolveClass(className);
		}
		return null;
	}

	private ObjectInstance resolveObject(Object key) {
		ObjectInstance oi;
		synchronized (objectMap) {
			oi = (ObjectInstance) objectMap.get(key);
		}
		if (oi == null && this != globalBridge) {
			return globalBridge.resolveObject(key);
		}
		return oi;
	}
}