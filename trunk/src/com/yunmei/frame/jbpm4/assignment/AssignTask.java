package com.yunmei.frame.jbpm4.assignment;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.api.task.Assignable;
import org.jbpm.api.task.AssignmentHandler;
import com.yunmei.frame.bo.IAuthorityBO;
import com.yunmei.frame.exception.BOException;
import com.yunmei.frame.model.TaskUsers;
import com.yunmei.frame.utils.SpringUtils;

public class AssignTask implements AssignmentHandler {
	private static IAuthorityBO authBO;
	private static SessionFactory sessionFactory;
	private static Logger logger = Logger.getLogger(AssignTask.class);

	private static SessionFactory getSessionFactory() {
		if (sessionFactory == null)
			sessionFactory = (SessionFactory) SpringUtils
					.getBean("sessionFactory");
		return sessionFactory;
	}

	private static IAuthorityBO getAuthBO() {
		if (authBO == null)
			authBO = (IAuthorityBO) SpringUtils.getBean("sysAuthService");
		return authBO;
	}

	public void assign(Assignable assignable, OpenExecution e) throws Exception {
		String activityName = e.findActiveActivityNames().iterator().next();
		String userId = (String) e.getVariable("!"+activityName);
		if (userId == null) {
			SessionFactory sessionFactory = getSessionFactory();
			IAuthorityBO authBO = getAuthBO();
			Session session = sessionFactory.getCurrentSession();
			String processDefinitionId = e.getProcessDefinitionId();
			if (logger.isDebugEnabled()) {
				logger.debug("executionId :" + e.getId());
				logger.debug("processInstanceId :"
						+ e.getProcessInstance().getId());
				logger.debug("buessiessId :" + e.getProcessInstance().getKey());
			}
			TaskUsers tu = (TaskUsers) session.get(TaskUsers.class,
					processDefinitionId + "_" + activityName);
			if (tu == null) {
				throw new BOException("请先分配流程参与者");
			}
			List<Long> list = null;
			if (logger.isDebugEnabled()) {
				logger.debug("分配类型 :" + tu.getType());
			}
			if ("P".equals(tu.getType())) {
				list = authBO.findUsersByPostId(Long
						.parseLong(tu.getEntityId()));
			} else if ("R".equals(tu.getType())) {
				list = authBO.findUsersByRoleId(Long
						.parseLong(tu.getEntityId()));
			} else {
				list = new ArrayList<Long>();
				list.add(Long.parseLong(tu.getEntityId()));
				assignable.addCandidateUser(tu.getEntityId());
			}
			for (Long id : list) {
				if (logger.isDebugEnabled()) {
					logger.debug("actor :" + id);
				}
				assignable.addCandidateUser(id.toString());
			}
		} else {
			logger.debug("再次进入,参与者 :" + userId);
			assignable.setAssignee(userId);
		}
	}
}
