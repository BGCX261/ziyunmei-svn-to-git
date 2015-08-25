package com.yunmei.frame.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class TimerExecute implements Job {
	Logger log = Logger.getLogger(TimerExecute.class);

	public void execute(JobExecutionContext context) {
		try {
			if (log.isDebugEnabled()) {
				log.debug("Key :" + context.getJobDetail().getKey());
				log.debug("Name :" + context.getJobDetail().getName());
				log.debug("fullName :" + context.getJobDetail().getFullName());
			}
			$.setTaskId(context.getJobDetail().getName());
			String invoke = (String) context.getMergedJobDataMap()
					.get("method");
			JSONArray params = new JSONArray((String) context
					.getMergedJobDataMap().get("params"));
			invoke(params, invoke);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void invoke(JSONArray params, String invoke) throws Exception {
		String bean = invoke.split("\\.")[0];
		String methodName = invoke.split("\\.")[1];
		Object target = SpringUtils.getBean(bean);
		Method methods[] = target.getClass().getDeclaredMethods();
		for (Method m : methods) {
			if (m.getName().equals(methodName)
					&& m.getParameterTypes().length == params.length()) {
				List val = new ArrayList();
				Class actParamCls[] = m.getParameterTypes();
				for (int i = 0; i < params.length(); i++) {
					Object paramVal = params.get(i);
					if (actParamCls[i].equals(Long.class)) {
						val.add((Integer) paramVal + 0L);
					} else if (paramVal.getClass().equals(JSONObject.class)) {
						Object instance = actParamCls[i].newInstance();
						$.copy((JSONObject) paramVal, instance);
						val.add(instance);
					} else if (paramVal.getClass().equals(JSONArray.class)) {
						Object[] ints = null;
						for (int j = 0; j < ((JSONArray) paramVal).length(); j++) {
							if (actParamCls[i].equals(String[].class)) {
								if (ints == null) {
									ints = new String[((JSONArray) paramVal)
											.length()];
								}
								ints[j] = ((JSONArray) paramVal).getString(j);
							}
							if (actParamCls[i].equals(Integer[].class)) {
								if (ints == null) {
									ints = new Integer[((JSONArray) paramVal)
											.length()];
								}
								ints[j] = ((JSONArray) paramVal).getInt(j);
							}
						}
						val.add(ints);
					}

				}
				m.invoke(target, val.toArray());
			}
		}
	}
}