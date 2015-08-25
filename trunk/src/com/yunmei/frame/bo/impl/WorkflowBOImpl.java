package com.yunmei.frame.bo.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jbpm.api.Execution;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.RepositoryService;
import org.jbpm.api.TaskService;
import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.task.TaskImpl;

import com.yunmei.frame.bo.IWorkflowBO;
import com.yunmei.frame.model.Opinion;
import com.yunmei.frame.model.TaskUsers;
import com.yunmei.frame.utils.Auth;
import com.yunmei.frame.utils.Forward;

@Auth(name = "流程管理")
public class WorkflowBOImpl implements IWorkflowBO {
	Logger log = Logger.getLogger(WorkflowBOImpl.class);
	private ProcessEngine processEngine;
	private SessionFactory sessionFactory;

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public String deploy(InputStream input, String remark) {
		String deploymentId = processEngine.getRepositoryService()
				.createDeployment().addResourcesFromZipInputStream(
						new ZipInputStream(input)).deploy();
		if (log.isDebugEnabled()) {
			log.debug("deploymentId:" + deploymentId + ",remark:" + remark);
		}
		return deploymentId;
	}

	public void setProcessEngine(ProcessEngine processEngine) {
		this.processEngine = processEngine;
	}

	// 流程id+"_"+任务名
	public void assigned(TaskUsers tu) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(tu);
	}

	@Auth(name = "结案任务")
	public Forward doneList() {
		return new Forward("com/yunmei/frame/view/done.jsp");
	}

	@Auth(name = "流程管理")
	public Forward manager() {
		return new Forward("com/yunmei/frame/view/manager.jsp");
	}

	@Auth(name = "待办任务")
	public Forward todoList() {
		return new Forward("com/yunmei/frame/view/todo.jsp");
	}

	public String start(String processDefinitonKey, String businessId) {
		ProcessInstance processInstance = processEngine.getExecutionService()
				.startProcessInstanceByKey(processDefinitonKey, businessId);
		return processInstance.getId();
	}

	public String start(String processDefinitonKey, String businessId, Map varis) {
		ProcessInstance processInstance = processEngine.getExecutionService()
				.startProcessInstanceByKey(processDefinitonKey, varis,
						businessId);
		return processInstance.getId();
	}

	public void startNext(String userId, String processDefinitonKey,
			String businessId) {
		startNext(userId, processDefinitonKey, businessId, new HashMap());
	}

	public void startNext(String userId, String processDefinitonKey,
			String businessId, Map varis) {
		start(processDefinitonKey, businessId);
		// 要不然数据在缓存中,下面的查询查不到
		sessionFactory.getCurrentSession().flush();
		Task task = (Task) sessionFactory
				.getCurrentSession()
				.createQuery(
						"select t from org.jbpm.pvm.internal.task.TaskImpl t,ExecutionImpl e where e.key=:key and t.processInstance=e.processInstance")
				.setParameter("key", businessId).uniqueResult();
		next(userId, task.getId(), varis);
	}

	public void next(String userId, String taskId) {
		next(userId, taskId, new HashMap());
	}

	public void next(String userId, String taskId, Map varis) {
		Task task = processEngine.getTaskService().getTask(taskId);
		if (task.getAssignee() == null) {
			processEngine.getTaskService().takeTask(taskId, userId);
			varis.put("!" + task.getActivityName(), userId);
		}
		processEngine.getTaskService().completeTask(taskId, varis);
	}

	public List<Task> findTodos(String userId) {
		TaskService taskService = processEngine.getTaskService();
		List<Task> list = taskService.createTaskQuery().candidate(userId)
				.list();
		list.addAll(taskService.findPersonalTasks(userId));
		return list;
	}

	public void delete(String deploymentId) {
		ProcessDefinition processDefinition = processEngine
				.getRepositoryService().createProcessDefinitionQuery()
				.deploymentId(deploymentId).uniqueResult();
		processEngine.getRepositoryService().deleteDeploymentCascade(
				processDefinition.getDeploymentId());

		Session session = sessionFactory.getCurrentSession();
		session.createQuery("delete from TaskUsers tu where tu.id like :id")
				.setParameter("id", processDefinition.getId() + "%")
				.executeUpdate();
	}

	public void deleteProcessInstance(String processInstanceId) {
		processEngine.getExecutionService().deleteProcessInstanceCascade(
				processInstanceId);
	}

	public List<ProcessDefinition> findProcessDefinitions() {
		return processEngine.getRepositoryService()
				.createProcessDefinitionQuery().list();
	}

	public List<ProcessInstance> findProcessInstances(String processDefinitionId) {
		return processEngine.getExecutionService().createProcessInstanceQuery()
				.processDefinitionId(processDefinitionId).list();
	}

	public List<Map<String, String>> findTaskNodes(String processDefinitionId) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		RepositoryService repositoryService = processEngine
				.getRepositoryService();
		ProcessDefinition processDefinition = repositoryService
				.createProcessDefinitionQuery().processDefinitionId(
						processDefinitionId).uniqueResult();
		InputStream input = repositoryService.getResourceAsStream(
				processDefinition.getDeploymentId(), processDefinition.getKey()
						+ ".jpdl.xml");
		try {
			Document doc = new SAXReader().read(input);
			for (Iterator i = doc.getRootElement().elementIterator("task"); i
					.hasNext();) {
				Element foo = (Element) i.next();
				String act = foo.attributeValue("name");
				Map<String, String> rec = new HashMap<String, String>();
				TaskUsers taskUsers = (TaskUsers) sessionFactory
						.getCurrentSession().get(TaskUsers.class,
								processDefinitionId + "_" + act);
				rec.put("name", act);
				if (taskUsers != null) {
					rec.put("type", taskUsers.getType());
					rec.put("value", taskUsers.getEntityId());
					rec.put("text", taskUsers.getEntityName());
				}
				list.add(rec);
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<Opinion> findOpinions(String businessId) {
		Session session = sessionFactory.getCurrentSession();
		return session
				.createQuery(
						"from Opinion p where p.businessId=:businessId order by p.date")
				.setParameter("businessId", businessId).list();
	}

	public String getBusinessIdByTaskId(String taskId) {
		Task task = processEngine.getTaskService().getTask(taskId);
		Execution execution = processEngine.getExecutionService()
				.findExecutionById(task.getExecutionId());
		return execution.getKey();
	}

	public void auditByBusinessId(Opinion opinion) {
		Task e = (Task) sessionFactory
				.openSession()
				.createQuery(
						"select t from org.jbpm.pvm.internal.task.TaskImpl t,ExecutionImpl e where e.key=:key and t.processInstance=e.processInstance")
				.setParameter("key", opinion.getBusinessId()).uniqueResult();
		opinion.setTaskId(e.getId());
		opinion.setTaskName(e.getName());
		audit(opinion);
	}

	public void audit(Opinion opinion) {
		Map varis = new HashMap();
		varis.put(opinion.getKey(), opinion.getResult());
		sessionFactory.getCurrentSession().save(opinion);
		next(opinion.getUserId(), opinion.getTaskId(), varis);
	}

	public void nextByBusinessId(String userId, String businessId, Map varis) {
		Task e = (Task) sessionFactory
				.openSession()
				.createQuery(
						"select t from org.jbpm.pvm.internal.task.TaskImpl t,ExecutionImpl e where e.key=:key and t.processInstance=e.processInstance")
				.setParameter("key", businessId).uniqueResult();
		next(userId, e.getId(), varis);
	}

	public void nextByBusinessId(String userId, String businessId) {
		nextByBusinessId(userId, businessId, new HashMap());
	}
}
