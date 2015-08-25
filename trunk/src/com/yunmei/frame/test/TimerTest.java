package com.yunmei.frame.test;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.yunmei.frame.bo.ITimerBO;
import com.yunmei.frame.model.TimerTask;
import com.yunmei.frame.utils.$;

public class TimerTest extends TestCase {

	ApplicationContext app;
	ITimerBO timeService;

	public void setUp() throws Exception {

	}

	public void testDate() throws Exception {
		JSONArray params = new JSONArray("[1,2,{id:2},['Hello','World']]");
		String abc[] = { "zhang", "lisi" };

		Method methods[] = TimerTest.class.getDeclaredMethods();
		String methodName = "testTimer";
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
						List ints = new ArrayList();
						for (int j = 0; i < ((JSONArray) paramVal).length(); j++) {
							ints.add(((JSONArray) paramVal).getInt(i));
						}
						
					} else
						val.add(paramVal);
				}
				m.invoke(this, val.toArray());
			}
		}
	}

	public void testA(){
		List ints = new ArrayList();
		ints.add("String");
		String[] a =(String[]) ints.toArray(new String[ints.size()]);
	}
	
	public void testTimer(Integer i, Long j, TimerTask t, String k[]) {

	}
}
