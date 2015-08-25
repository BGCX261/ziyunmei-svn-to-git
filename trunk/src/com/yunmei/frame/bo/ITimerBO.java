package com.yunmei.frame.bo;

import java.io.Serializable;
import java.util.List;

import com.yunmei.frame.exception.BOException;
import com.yunmei.frame.model.TimerTask;
import com.yunmei.frame.servlet.Page;
import com.yunmei.frame.utils.Forward;

public interface ITimerBO {

	public Forward enterTimer();

	public void insertTask2Scheduler(TimerTask task) throws BOException;

	public Serializable insertTask(TimerTask task) throws BOException;

	public List<TimerTask> findTodoTasks() throws BOException;

	public Page find(TimerTask task, int start, int max)
			throws BOException;

	public void invokeNow(TimerTask task) throws BOException;

	public void loadTimerTask() throws BOException;
	public void delete(Long id) throws BOException;
}
