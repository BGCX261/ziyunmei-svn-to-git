package com.yunmei.frame.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.SchedulerFactory;
import org.quartz.ee.servlet.QuartzInitializerListener;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.DirectFieldAccessor;

public class $ {
	private static Set<Class> allowType = new HashSet<Class>();
	private static Set<String> paramsType = new HashSet<String>();
	private static Map<String, String> paramsTypeMapping = new HashMap<String, String>();
	private static Logger log = Logger.getLogger($.class);
	private static ThreadLocal<Map> threadLocal = new ThreadLocal<Map>();
	static {
		// TODO 需要添加
		paramsTypeMapping.put("Y-m-d", "yyyy-MM-dd");
		paramsTypeMapping.put("yyyy-MM-dd", "yyyy-MM-dd");
		// -------------------------------------------
		paramsType.add("Y-m-d");
		paramsType.add("Y-m-d H:i:s");
		paramsType.add("int");
		paramsType.add("long");
		paramsType.add("double");
		allowType.add(short.class);
		allowType.add(Short.class);
		allowType.add(int.class);
		allowType.add(Integer.class);
		allowType.add(Long.class);
		allowType.add(long.class);
		allowType.add(Float.class);
		allowType.add(float.class);
		allowType.add(Double.class);
		allowType.add(double.class);
		allowType.add(char.class);
		allowType.add(Character.class);
		allowType.add(String.class);
		allowType.add(java.sql.Date.class);
		allowType.add(java.util.Date.class);
	}

	public static Object copy(Object src, Object dest) {

		for (Field f : src.getClass().getDeclaredFields()) {
			try {
				if (PropertyUtils.isWriteable(dest, f.getName())
						&& PropertyUtils.isReadable(src, f.getName())) {
					Object retVal = PropertyUtils.getSimpleProperty(src, f
							.getName());
					if (retVal == null)
						continue;
					PropertyUtils.setProperty(dest, f.getName(), retVal);
				}
			} catch (Exception e) {
				if (log.isDebugEnabled())
					log.error("属性复制失败");
			}
		}
		return dest;
	}

	public static Object copy(JSONObject src, Object dest) {
		Iterator<String> iter = src.keys();
		String key = null;
		String value = null;
		while (iter.hasNext()) {
			try {
				key = iter.next();
				value = src.get(key).toString();
				int index = value.indexOf("@");
				index = -1;
				if (dest instanceof Map) {
					Map map = (Map) dest;
					if (index == -1) {
						if (!key.startsWith("ext-comp"))
							map.put(key, value);
					} else {
						String type = value.substring(index + 1);
						if (log.isDebugEnabled()) {
							log.debug("key：" + key + ",value:" + value
									+ ",type:" + type);
						}
						if (type.equals("int")) {
							map.put(key, Integer.parseInt(value.substring(0,
									index)));
						} else if (type.equals("long")) {
							map.put(key, Long.parseLong(value.substring(0,
									index)));
						} else if (type.equals("double")) {
							map.put(key, Double.parseDouble(value.substring(0,
									index)));
						} else {
							map.put(key, new Date(value));
						}
					}
				} else {
					Class type = PropertyUtils.getPropertyType(dest, key);
					if (PropertyUtils.isWriteable(dest, key)) {
						if (Date.class.equals(type)) {
							Date d = new Date(Long.parseLong(value));
							BeanUtils.setProperty(dest, key, d);
						} else {
							if (allowType.contains(type)) {
								BeanUtils.setProperty(dest, key, value);
							} else {
								Object t = type.newInstance();
								BeanUtils.setProperty(t, "id", value);
								BeanUtils.setProperty(dest, key, t);
								if (log.isDebugEnabled()) {
									log.debug("value：" + value + ",type:"
											+ type);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (log.isDebugEnabled()) {
					log.error("属性 :" + key + "读写失败");
				}
			}
		}
		return dest;
	}

	public static JSONObject objectToJSON(Object src, String filter) {
		if (src == null)
			return new JSONObject();
		JSONObject dest = null;
		if (src instanceof Map) {
			if (filter != null) {
				String fields[] = filter.split(",");
				for (String field : fields)
					try {
						dest.put(field, ((Map) src).get(field));
					} catch (JSONException e) {
						e.printStackTrace();
					}
			} else
				dest = new JSONObject((Map) src);
		} else {
			dest = new JSONObject();
			Class klass = src.getClass();
			Method[] methods = klass.getMethods();
			for (int i = 0; i < methods.length; i += 1) {
				try {
					Method method = methods[i];
					String name = method.getName();
					String key = "";
					if (name.startsWith("get")) {
						key = name.substring(3);
					} else if (name.startsWith("is")) {
						key = name.substring(2);
					}
					if (key.length() > 0
							&& Character.isUpperCase(key.charAt(0))
							&& method.getParameterTypes().length == 0) {
						if (key.length() == 1) {
							key = key.toLowerCase();
						} else if (!Character.isUpperCase(key.charAt(1))) {
							key = key.substring(0, 1).toLowerCase()
									+ key.substring(1);
						}
						if (filter != null) {
							String fields[] = filter.split(",");
							int j;
							for (j = 0; j < fields.length; j++) {
								if (fields[j].equals(key)) {
									j = fields.length;
								}
							}
							if (j != fields.length + 1)
								continue;
						}
						Class returnType = method.getReturnType();
						if (allowType.contains(returnType)) {
							if (returnType.equals(java.util.Date.class)) {
								Date ret = (Date) method.invoke(src,
										new Object[] {});
								dest.put(key, ret.toGMTString());
							} else
								dest.put(key, method.invoke(src,
										new Object[] {}));
						} else {
							Object obj = method.invoke(src, new Object[] {});
							if (obj != null) {
								Method methodId = returnType.getMethod("getId",
										new Class[] {});
								dest.put(key, methodId.invoke(obj,
										new Object[] {}));
							}
						}
					}
				} catch (Exception e) {
					// e.printStackTrace();
					/* forget about it */
				}
			}
		}
		return dest;
	}

	public static JSONObject objectToJSON(Object src) {
		return objectToJSON(src, null);
	}

	/*
	 * public static JSONObject objectToJSON(Object src) { if (src == null)
	 * return new JSONObject(); JSONObject dest = null; if (src instanceof Map)
	 * { dest = new JSONObject((Map) src); } else { dest = new JSONObject();
	 * Class klass = src.getClass(); Method[] methods = klass.getMethods(); for
	 * (int i = 0; i < methods.length; i += 1) { try { Method method =
	 * methods[i]; String name = method.getName(); String key = ""; if
	 * (name.startsWith("get")) { key = name.substring(3); } else if
	 * (name.startsWith("is")) { key = name.substring(2); } if (key.length() > 0
	 * && Character.isUpperCase(key.charAt(0)) &&
	 * method.getParameterTypes().length == 0) { if (key.length() == 1) { key =
	 * key.toLowerCase(); } else if (!Character.isUpperCase(key.charAt(1))) {
	 * key = key.substring(0, 1).toLowerCase() + key.substring(1); } Class
	 * returnType = method.getReturnType(); if (allowType.contains(returnType))
	 * { if (returnType.equals(java.util.Date.class)) { Date ret = (Date)
	 * method.invoke(src, null); dest.put(key, ret.toGMTString()); } else
	 * dest.put(key, method.invoke(src, null)); } else { Object obj =
	 * method.invoke(src, null); if (obj != null) { Method methodId =
	 * returnType.getMethod("getId", new Class[] {}); dest.put(key,
	 * methodId.invoke(obj, null)); } } } } catch (Exception e) { forget about
	 * it } } } return dest; }
	 */

	/**
	 * 同步树
	 * 
	 * @param list
	 * @return
	 * @throws JSONException
	 */

	public static JSONArray list2Tree(List list) throws JSONException {
		return list2Tree(list, false);
	}

	public static JSONArray list2Tree(List list, boolean check)
			throws JSONException {
		if (list == null)
			return null;
		Stack<JSONObject> stack = new Stack<JSONObject>();
		JSONArray array = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			JSONObject json = objectToJSON(list.get(i));
			json.put("leaf", json.getInt("right") - json.getInt("left") == 1);
			if (check == true)
				json.put("checked", false);
			while (!stack.empty()) {
				JSONObject parent = stack.pop();
				if (parent.getString("id").equals(json.getString("parent"))) {
					if (!parent.has("children")) {
						JSONArray pares = new JSONArray();
						parent.put("children", pares);
					}
					parent.getJSONArray("children").put(json);
					stack.push(parent);
					stack.push(json);
					break;
				}
			}
			if (stack.empty()) {
				array.put(json);
				stack.push(json);
			}
		}
		return array;
	}

	public static JSONArray list2Tree(Iterator iter) throws JSONException {
		return list2Tree(iter, false);
	}

	public static JSONArray list2Tree(Iterator iter, boolean check)
			throws JSONException {
		Stack<JSONObject> stack = new Stack<JSONObject>();
		JSONArray array = new JSONArray();
		while (iter.hasNext()) {
			JSONObject json = objectToJSON(iter.next());
			json.put("leaf", json.getInt("right") - json.getInt("left") == 1);
			if (check == true) {
				json.put("checked", false);
			}
			while (!stack.empty()) {
				JSONObject parent = stack.pop();
				if (parent.getString("id").equals(json.getString("parent"))) {
					if (!parent.has("children")) {
						JSONArray pares = new JSONArray();
						parent.put("children", pares);
					}
					parent.getJSONArray("children").put(json);
					stack.push(parent);
					stack.push(json);
					break;
				}
			}
			if (stack.empty()) {
				array.put(json);
				stack.push(json);
			}
		}
		return array;
	}

	public static JSONArray list2AsyncTree(List list) throws JSONException {
		return list2AsyncTree(list, false);
	}

	/**
	 * 异步树
	 */
	public static JSONArray list2AsyncTree(List list, boolean check)
			throws JSONException {
		if (list == null)
			return null;
		JSONArray array = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			JSONObject json = objectToJSON(list.get(i));
			if (json.has("left") && json.has("right")) {
				json.put("leaf",
						json.getInt("right") - json.getInt("left") == 1);
			} else {
				json.put("leaf", "true".equals(json.getString("leaf")));
			}
			if (check == true) {
				json.put("checked", false);
			}
			array.put(json);
		}
		return array;
	}

	public static JSONArray list2AsyncTree(Iterator iter) throws JSONException {
		return list2AsyncTree(iter, false);
	}

	public static JSONArray list2AsyncTree(Iterator iter, boolean check)
			throws JSONException {
		JSONArray array = new JSONArray();
		while (iter.hasNext()) {
			JSONObject json = objectToJSON(iter.next());
			if (json.has("left") && json.has("right")) {
				json.put("leaf",
						json.getInt("right") - json.getInt("left") == 1);
			} else {
				json.put("leaf", "true".equals(json.getString("leaf")));
			}
			if (check == true) {
				json.put("checked", false);
			}
			array.put(json);
		}
		return array;
	}

	public static void setSession(HttpSession session) {
		Map global = threadLocal.get();
		if (global == null) {
			global = new HashMap();
			threadLocal.set(global);
		}
		global.put(Constraint.SESSION, session);
	}

	public static HttpSession getSession() {
		Map global = threadLocal.get();
		if (global == null)
			return null;
		return (HttpSession) global.get(Constraint.SESSION);
	}

	public static void setRequest(HttpServletRequest request) {
		Map global = threadLocal.get();
		if (global == null) {
			global = new HashMap();
			threadLocal.set(global);
		}
		global.put(Constraint.REQUEST, request);
	}

	public static SchedulerFactory getSchedulerFactory() {
		Map global = threadLocal.get();
		if (global == null)
			return null;
		return (SchedulerFactory) global
				.get(QuartzInitializerListener.QUARTZ_FACTORY_KEY);
	}

	public static void setSchedulerFactory(SchedulerFactory schedulerFactory) {
		Map global = threadLocal.get();
		if (global == null) {
			global = new HashMap();
			threadLocal.set(global);
		}
		global.put(QuartzInitializerListener.QUARTZ_FACTORY_KEY,
				schedulerFactory);
	}

	public static HttpServletRequest getRequest() {
		Map global = threadLocal.get();
		if (global == null)
			return null;
		return (HttpServletRequest) global.get(Constraint.REQUEST);
	}

	public static void setAuthData(String insertSQL) {
		setAuthData(insertSQL, 12);
	}

	public static void setAuthData(String insertSQL, Integer order) {
		Map global = threadLocal.get();
		if (global == null) {
			global = new HashMap();
			threadLocal.set(global);
		}
		Map data = new HashMap();
		data.put("insertSQL", insertSQL);
		data.put("order", order);
		global.put("filter", data);
	}

	public static String getAuthData() {
		Map global = threadLocal.get();
		if (global == null) {
			return null;
		}
		Map data = (Map) global.get("filter");
		if (data == null)
			return null;
		Integer order = (Integer) data.get("order");
		if (order == 1) {
			String insertSQL = (String) data.get("insertSQL");
			global.remove("filter");
			return insertSQL;
		} else {
			if (order > 10) {
				order = -11;
				if (order < 10) {
					return (String) data.get("insertSQL");
				}
			} else
				order--;
			data.put("order", order);
			return null;
		}
	}

	public static void setRequestInfo(Map map) {
		Map global = threadLocal.get();
		if (global == null) {
			global = new HashMap();
			threadLocal.set(global);
		}
		global.put("REQUEST_INFO", map);
	}

	public static Map getRequestInfo() {
		Map global = threadLocal.get();
		if (global == null)
			return null;
		return (Map) global.get("REQUEST_INFO");
	}

	public static void setRequestType(Character character) {
		Map global = threadLocal.get();
		if (global == null) {
			global = new HashMap();
			threadLocal.set(global);
		}
		global.put("REQUEST_TYPE", character);
	}

	public static Character getRequestType() {
		Map global = threadLocal.get();
		if (global == null)
			return Constraint.REQUEST_NORMAL;
		return (Character) global.get("REQUEST_TYPE");
	}

	public static String getGenericParameterType() {
		Map global = threadLocal.get();
		if (global == null)
			return null;
		return (String) global.get(Constraint.PARAMETER_TYPE);
	}

	public static void setGenericParameterType(String clazzString) {
		Map global = threadLocal.get();
		if (global == null) {
			global = new HashMap();
			threadLocal.set(global);
		}
		global.put(Constraint.PARAMETER_TYPE, clazzString);
	}

	public static String getTaskId() {
		Map global = threadLocal.get();
		if (global == null)
			return null;
		return (String) global.get(Constraint.TASK_ID);
	}

	public static void setTaskId(String taskId) {
		Map global = threadLocal.get();
		if (global == null) {
			global = new HashMap();
			threadLocal.set(global);
		}
		global.put(Constraint.TASK_ID, taskId);
	}

	public static JSONArray list2JSONArray(List list) {
		JSONArray array = new JSONArray();
		for (Object obj : list) {
			if (allowType.contains(obj.getClass()))
				array.put(obj);
			else
				array.put(objectToJSON(obj));
		}
		return array;
	}

	public static JSONArray list2JSONArray(Iterator iter) {
		JSONArray array = new JSONArray();
		while (iter.hasNext()) {
			Object obj = iter.next();
			if (allowType.contains(obj.getClass()))
				array.put(obj);
			else
				array.put(objectToJSON(obj));
		}
		return array;
	}

	public static Class getTarget(Object proxy) {
		Class clazz = null;
		if (AopUtils.isJdkDynamicProxy(proxy)) {
			AdvisedSupport advised = (AdvisedSupport) new DirectFieldAccessor(
					Proxy.getInvocationHandler(proxy))
					.getPropertyValue("advised");
			clazz = advised.getTargetClass();
		} else
			clazz = AopUtils.getTargetClass(proxy);
		return clazz;
	}

	public static String getGenericParameterType(Class target, String method,
			Class parameterTypes[]) {
		try {
			Method m = target.getMethod(method, parameterTypes);
			if (m.getGenericParameterTypes().length > 0) {
				Type tys[] = m.getGenericParameterTypes();
				if (tys.length > 0) {
					if (tys[0] instanceof ParameterizedType) {
						ParameterizedType pt = (ParameterizedType) tys[0];
						Type act[] = pt.getActualTypeArguments();
						if (act.length > 0) {
							return act[0].toString().substring(6);
						}
					}
				}
			}
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.error("得到泛型参数错误" + e);
			}
		}
		return null;
	}

	// TODO 进一步修改
	public static String genSQL(Map params) {
		StringBuilder sb = new StringBuilder(" where 1=1 ");
		for (Object obj : params.keySet()) {
			String key = (String) obj;
			Object value = params.get(key);
			if (value instanceof Date) {
				if (key.startsWith("from")) {
					sb.append(" and s.").append(
							key.substring(4, 5).toLowerCase()).append(
							key.substring(5)).append(">=?");
				} else if (key.startsWith("to")) {
					sb.append(" and s.").append(
							key.substring(2, 3).toLowerCase()).append(
							key.substring(3)).append("<=?");
				} else
					sb.append(" and s.").append(key).append("=?");
			} else if (value instanceof String) {
				sb.append(" and s.").append(key).append(" like ?");
			} else
				sb.append(" and s.").append(key).append("=?");
		}
		if (log.isDebugEnabled()) {
			log.debug(" 生成串: " + sb.toString());
		}
		return sb.toString();
	}

	public static String ObjectToString(Object obj, boolean all) {
		if (obj == null)
			return "null";
		if (allowType.contains(obj.getClass()))
			return obj.toString();
		Method[] methds = obj.getClass().getDeclaredMethods();
		StringBuilder sb = new StringBuilder("{");
		for (Method m : methds) {
			if (m.getName().startsWith("get")
					&& m.getParameterTypes().length == 0) {
				try {
					Object result = m.invoke(obj, new Object[0]);
					if (result != null || all != false)
						sb.append(
								m.getName().substring(3, 4).toLowerCase()
										+ m.getName().substring(4)).append(":")
								.append(result).append(",");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		sb.replace(sb.length() - 1, sb.length(), "}");
		return sb.toString();
	}

	public static void clear() {
		threadLocal.set(null);
	}
}
