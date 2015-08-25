package com.yunmei.frame.bo;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.task.Task;

import com.yunmei.frame.model.Opinion;
import com.yunmei.frame.model.TaskUsers;
import com.yunmei.frame.utils.Forward;

public interface IWorkflowBO {
	/**
	 * 待办任务列表
	 * 
	 * @return
	 */
	Forward todoList();

	/**
	 * 结案任务列表
	 * 
	 * @return
	 */
	Forward doneList();

	/**
	 * 流程管理
	 * 
	 * @return
	 */
	Forward manager();

	/**
	 *待办人物列表
	 */
	List<Task> findTodos(String userId);

	/**
	 *开启流程
	 */
	String start(String processDefinitonKey, String businessId);

	/**
	 *开启流程
	 */
	String start(String processDefinitonKey, String businessId, Map varis);

	/**
	 *开启流程并跳转
	 */
	void startNext(String userId, String processDefinitonKey,
			String businessId, Map varis);

	/**
	 *开启流程并跳转
	 */
	void startNext(String userId, String processDefinitonKey, String businessId);

	/**
	 *流程继续流转
	 */
	void next(String userId, String taskId, Map varis);

	/**
	 *流程继续流转
	 */
	void next(String userId, String taskId);
	
	/**
	 *流程继续流转
	 */
	void nextByBusinessId(String userId, String businessId, Map varis);

	/**
	 *流程继续流转
	 */
	void nextByBusinessId(String userId, String businessId);

	/**
	 *审批
	 */
	void audit(Opinion opinion);
	/**
	 *审批
	 */
	void auditByBusinessId(Opinion opinion);

	/**
	 * 删除流程实例
	 */
	void deleteProcessInstance(String processInstanceId);

	/**
	 * 查找流程定义
	 */
	List<ProcessDefinition> findProcessDefinitions();

	/**
	 * 查找流程实例
	 */
	List<ProcessInstance> findProcessInstances(String processDefinitionId);

	List<Opinion> findOpinions(String businessId);

	/**
	 * 分配流程参与者
	 */
	void assigned(TaskUsers tu);

	/**
	 * 部署流程
	 */
	String deploy(InputStream input, String remark);

	/**
	 * 查询流程任务节点
	 */
	List<Map<String, String>> findTaskNodes(String workflow);

	/**
	 * 按部署ID删除流程
	 */
	void delete(String deploymentId);

	String getBusinessIdByTaskId(String taskId);
}
