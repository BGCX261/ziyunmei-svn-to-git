package com.yunmei.frame.bo.impl;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.json.JSONArray;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;

import com.yunmei.frame.bo.ITimerBO;
import com.yunmei.frame.exception.BOException;
import com.yunmei.frame.model.TimerTask;
import com.yunmei.frame.servlet.Page;
import com.yunmei.frame.utils.$;
import com.yunmei.frame.utils.Auth;
import com.yunmei.frame.utils.Forward;
import com.yunmei.frame.utils.TimerExecute;

@Auth(name = "任务管理")
public class TimerBOImpl implements ITimerBO {

	@Auth(name = "定时任务")
	public Forward enterTimer() {
		return new Forward("com/yunmei/frame/view/timer.jsp");
	}

	public void insertTask2Scheduler(TimerTask task) throws BOException {
		try {
			if (!task.isValidExecuteDateTime()) {
				if (log.isDebugEnabled())
					log.info("当前任务已过期" + task.getId() + ",当前时间：" + new Date()
							+ ",执行时间:" + task.getExecuteDateTime());
				return;
			}
			JobDetail job = new JobDetail(task.getId().toString(), null,
					TimerExecute.class);
			job.getJobDataMap().put("method", task.getInvoke());
			job.getJobDataMap().put(
					"params",
					task.getParams() == null ? "[]" : "[" + task.getParams()
							+ "]");
			SimpleTrigger trigger = new SimpleTrigger(task.getId().toString());
			trigger.setStartTime(task.getExecuteDateTime());
			if (task.getExecuteType().equals("1")) {
				trigger.setRepeatCount(task.getRepeatCount());
				trigger.setRepeatInterval(task.getRepeatInterval() * 1000);
			}
			Scheduler sche = $.getSchedulerFactory().getScheduler();
			if (sche.getJobDetail(task.getId().toString(), null) == null) {
				sche.scheduleJob(job, trigger);
				if (log.isDebugEnabled()) {
					log.info("当前任务ID:" + task.getId() + ",任务名："
							+ task.getName() + ",执行时间:"
							+ task.getExecuteDateTime() + "被成功加入....");
				}
			} else if (log.isDebugEnabled()) {
				log.info("当前任务ID:" + task.getId() + ",任务名：" + task.getName()
						+ ",执行时间:" + task.getExecuteDateTime() + "已经存在....");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<TimerTask> findTodoTasks() throws BOException {
		if (log.isDebugEnabled())
			log.debug("taskId:" + $.getTaskId());
		Session session = sessionFactory.getCurrentSession();
		Date begin = new Date();
		Date end = new Date(begin.getTime() + 24 * 60 * 60 * 1000L);
		// TODO 这个地方有问题，呵呵
		SQLQuery sql = session
				.createSQLQuery("select * from sys_timer where type_=0 and state_=1 and execute_date_>=:begin and execute_date_<:end  "
						+ "union all select * from sys_timer where type_=1 and state_=1  and start_date_<=:begin and end_date_>:begin  "
						+ "union all select * from sys_timer where type_=2 and state_=1  and start_date_<=:begin and end_date_>:begin   and fixed_=:weekFix  "
						+ "union all select * from sys_timer where type_=3 and state_=1  and start_date_<=:begin and end_date_>:begin  and (fixed_=:monthFix or fixed_=:monthFix2)");
		Calendar c = Calendar.getInstance();
		sql.setParameter("begin", begin);
		sql.setParameter("end", end);
		sql.setParameter("weekFix", c.get(Calendar.DAY_OF_WEEK));
		sql.setParameter("monthFix", c.get(Calendar.DAY_OF_MONTH));
		sql.setParameter("monthFix2", c.get(Calendar.DAY_OF_MONTH)
				- c.getActualMaximum(Calendar.DAY_OF_MONTH) - 1);
		sql.addEntity(TimerTask.class);
		return sql.list();
	}

	public Serializable insertTask(TimerTask task) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(task);
		if ("1".equals(task.getState())) {
			insertTask2Scheduler(task);
		}
		if ("0".equals(task.getType())) {
			task.setFixed(null);
			task.setStartDate(null);
			task.setEndDate(null);

		} else if ("1".equals(task.getType())) {
			task.setFixed(null);
			task.setExecuteDate(null);
		} else {
			task.setExecuteDate(null);
		}
		if ("0".equals(task.getExecuteType())) {
			task.setExecuteTimeEnd(null);
			task.setRepeatInterval(null);
		}
		return task.getId();
	}

	public Page find(TimerTask task, int start, int max)
			throws BOException {
		Session session = sessionFactory.getCurrentSession();
		Page page = new Page();
		Criteria crit = session.createCriteria(TimerTask.class);
		Example example = Example.create(task);
		example.enableLike();
		crit.add(example);
		page.setCount(crit.list().size());
		crit.setFirstResult(start);
		crit.setMaxResults(max);
		page.setList(crit.list());
		return page;
	}

	public void loadTimerTask() throws BOException {
		List<TimerTask> list = this.findTodoTasks();
		if (log.isDebugEnabled())
			log.debug("有 " + list.size() + "被加入定时任务列表");
		for (TimerTask timer : list) {
			insertTask2Scheduler(timer);
		}
	}

	public void invokeNow(TimerTask task) throws BOException {
		try {
			TimerExecute t = new TimerExecute();
			String invoke = task.getInvoke();
			$.setTaskId(task.getId().toString());
			JSONArray params = new JSONArray(task.getParams() == null ? "[]"
					: "[" + task.getParams() + "]");
			t.invoke(params, invoke);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("执行失败");
		}
	}

	public void delete(Long id) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		Query q = session.createQuery("delete from TimerTask t where t.id=:id");
		q.setParameter("id", id);
		q.executeUpdate();
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	Logger log = Logger.getLogger(TimerBOImpl.class);
	private SessionFactory sessionFactory;
}
