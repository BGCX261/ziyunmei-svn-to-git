package com.yunmei.frame.jbpm4.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.hibernate.SessionFactory;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.RepositoryService;
import org.jbpm.api.TaskService;
import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.task.TaskImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.yunmei.frame.bo.IWorkflowBO;

/**
 * @author Tom Baeyens
 */
public class JbpmTest extends TestCase {

	String deploymentId;
	RepositoryService repositoryService;
	ExecutionService executionService;
	TaskService taskService;
	ProcessEngine processEngine;
	IWorkflowBO workflowBO;
	SessionFactory sessionFactory;

	protected void setUp() throws Exception {
		ApplicationContext app = new FileSystemXmlApplicationContext(
				"web/WEB-INF/system-config-mysql.xml");
		processEngine = (ProcessEngine) app.getBean("processEngine");
		repositoryService = processEngine.getRepositoryService();
		executionService = processEngine.getExecutionService();
		workflowBO = (IWorkflowBO) app.getBean("sysWorkflowService");
		taskService = processEngine.getTaskService();
		sessionFactory = (SessionFactory) app.getBean("sessionFactory");
	}

	public void testGetTask() throws Exception {
		Task e = (TaskImpl) sessionFactory.openSession()
				.createQuery("select  t from org.jbpm.pvm.internal.task.TaskImpl t ,ExecutionImpl e where e.key=:key and t.processInstance=e.processInstance")
				.setParameter("key", "10001").uniqueResult();
		System.out.println(e.getId());
	}

	public void testGetProcinst() {
		ProcessInstance pi = workflowBO.findProcessInstances("workflow-1").get(
				0);
		System.out.println(pi.getId());
	}

	public void testSelectTodoList() {
		List<Task> list = workflowBO.findTodos("5");
		for (Task t : list) {
			System.out.println("id:" + t.getId() + ",task:"
					+ t.getActivityName());
		}

		System.out.println("*************************");
		list = workflowBO.findTodos("6");
		for (Task t : list) {
			System.out.println("id:" + t.getId() + ",task:"
					+ t.getActivityName());
		}

		System.out.println("*************************");
		list = workflowBO.findTodos("7");
		for (Task t : list) {
			System.out.println("id:" + t.getId() + ",task:"
					+ t.getActivityName());
		}
	}

	public void testSubmit() {
		System.out.println("start submiting");
		Map map = new HashMap();
		map.put("monkey", 1001);
		workflowBO.next("6", "570002", map);
		// workflowBO.startNext("5", "workflow","1001", map);
		System.out.println("end submiting");
	}

	public void testReject() {
		Map map = new HashMap();
		map.put("pass", "不合格");
		workflowBO.next("7", "590004", map);
		// workflowBO.audit("7", "ghost", "", opinion);
	}

	public void testFindTasks() {
		// taskService.takeTask("300002", "6");
		List<Task> list = taskService.findPersonalTasks("6");
		for (Task t : list)
			System.out.println(t.getActivityName());
	}

	public void testStart() {
		workflowBO.start("workflow", System.currentTimeMillis() + "");
	}

	public void testDeploy() {
		deploymentId = repositoryService.createDeployment()
				.addResourceFromClasspath(
						"com/yunmei/frame/jbpm4/test/process.jpdl.xml")
				.deploy();
		System.out.println("deploymentId:" + deploymentId);
	}

	public void testDeleteDeploy() {
		repositoryService.deleteDeploymentCascade("1");
	}

}