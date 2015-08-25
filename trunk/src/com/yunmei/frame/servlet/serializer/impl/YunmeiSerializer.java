package com.yunmei.frame.servlet.serializer.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.yunmei.frame.exception.MarshallException;
import com.yunmei.frame.exception.UnmarshallException;
import com.yunmei.frame.servlet.Page;
import com.yunmei.frame.servlet.reflect.ObjectMatch;
import com.yunmei.frame.servlet.serializer.AbstractSerializer;
import com.yunmei.frame.servlet.serializer.SerializerState;
import com.yunmei.frame.utils.$;
import com.yunmei.frame.utils.Constraint;

@SuppressWarnings( { "serial", "unchecked" })
public class YunmeiSerializer extends AbstractSerializer {
	private final static Logger log = Logger
			.getLogger(YunmeiSerializer.class);
	public Class[] getSerializableClasses() {
		return new Class[] { YunmeiSerializer.class };
	}

	// TODO 处理非默认的
	@Override
	public boolean canSerialize(Class clazz, Class jsonClazz) {
		return true;
	}

	public Class[] getJSONClasses() {
		return new Class[] { JSONObject.class, List.class };
	}

	public ObjectMatch tryUnmarshall(SerializerState state, Class clazz,
			Object jso) throws UnmarshallException {
		return ObjectMatch.NULL;
	}

	public Object unmarshall(SerializerState state, Class clazz, Object jso)
			throws UnmarshallException {
		Object object = null;
		JSONObject json = null;
		JSONArray jsonArray = null;
		if (jso instanceof JSONArray) {
			jsonArray = (JSONArray) jso;
			if (log.isDebugEnabled()) {
				log.debug("接收参数为集合");
			}
		} else
			json = (JSONObject) jso;
		try {
			if (clazz.equals(HashMap.class) || clazz.equals(Map.class)) {
				if (log.isDebugEnabled()) {
					log.debug("接收参数为集合");
				}
				object = new HashMap();
				$.copy(json, object);
			} else if (clazz.equals(List.class)
					|| clazz.equals(ArrayList.class)) {
				object = new ArrayList();
				String parameterType = $.getGenericParameterType();
				for (int i = 0; i < jsonArray.length(); i++) {
					Object temp = Class.forName(parameterType).newInstance();
					((List) object).add(temp);
					$.copy(jsonArray.getJSONObject(i), temp);
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug("接收参数为普通POJO类");
				}
				object = clazz.newInstance();
				$.copy(json, object);
			}
		} catch (Exception e) {
			if (log.isDebugEnabled())
				log.error("解析接收数据失败" + e);
			throw new UnmarshallException("解析数据失败", e);
		}
		return object;
	}

	public Object marshall(SerializerState state, Object p, Object result)
			throws MarshallException {
		try {
			if (result instanceof List) {
				List list = (List) result;
				if ($.getRequestType() == Constraint.REQUEST_TREE) {
					if (log.isDebugEnabled()) {
						log.debug("封装tree数据");
					}
					return $.list2Tree(list);
				} else {
				//	$.setRequestType(Constraint.REQUEST_GRID);
					if (log.isDebugEnabled()) {
						log.debug("封装普通集合数据");
					}
					return $.list2JSONArray(list);
				}
			} else if (result instanceof Page) {
				if (log.isDebugEnabled()) {
					log.debug("封装Page对象");
				}
				Page page = (Page) result;
				JSONObject json = new JSONObject();
				json.put("total", page.getCount());
				json.put("results", $
						.list2JSONArray(page.getList()));
				return json;
			} else {
				if (log.isDebugEnabled()) {
					log.debug("封装POJO或Map对象");
				}
				return $.objectToJSON(result);
			}
		} catch (JSONException e) {
			if (log.isDebugEnabled()) {
				log.debug("封装数据失败" + e);
			}
			throw new MarshallException("封装数据失败", e);
		}
	}
}