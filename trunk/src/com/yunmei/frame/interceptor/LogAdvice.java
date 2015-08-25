package com.yunmei.frame.interceptor;

import java.util.Date;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;

import com.yunmei.frame.bo.ILogBO;
import com.yunmei.frame.model.Logs;
import com.yunmei.frame.model.User;
import com.yunmei.frame.utils.$;

public class LogAdvice implements MethodInterceptor {
	Logger log = Logger.getLogger(LogAdvice.class);

	public Object invoke(MethodInvocation invoke) throws Throwable {
		Object retVal = null;
		Logs logs = new Logs();
		try {
			retVal = invoke.proceed();
			logs.setIsSuccess('S');
		} catch (Exception e) {
			logs.setIsSuccess('F');
			if (log.isDebugEnabled()) {
				log.error(" 日志操作出现异常：" + e);
			}
		}
		Map requestInfo = $.getRequestInfo();
		logs.setLogTime(new Date());
		logs.setReqMethod((String) requestInfo.get("method"));
		logs.setReqParams((String) requestInfo.get("args"));
		logs.setUserIp((String) requestInfo.get("ip"));
		logs.setUserId(User.getUser().getId());
		logs.setUserName(User.getUser().getName());
		sysLogService.insertLog(logs);
		return retVal;
	}

	public void setSysLogService(ILogBO sysLogService) {
		this.sysLogService = sysLogService;
	}

	private ILogBO sysLogService;
}
