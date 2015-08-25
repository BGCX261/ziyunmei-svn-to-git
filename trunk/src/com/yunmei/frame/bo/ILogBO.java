package com.yunmei.frame.bo;

import java.util.Map;

import com.yunmei.frame.exception.BOException;
import com.yunmei.frame.model.Logs;
import com.yunmei.frame.servlet.Page;
import com.yunmei.frame.utils.Forward;

public interface ILogBO {
	public Forward enterLogs() throws BOException;

	public Page findByExample(Map map, int start, int max) throws BOException;

	public Long insertLog(Logs log) throws BOException;
	
}
