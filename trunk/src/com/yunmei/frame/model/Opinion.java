package com.yunmei.frame.model;

import java.util.Date;

public class Opinion {

	private Long id;
	/**
	 * 业务ID
	 */
	private String businessId;
	
	private String taskId;


	/**
	 * 任务名
	 */
	private String taskName;
	/**
	 * 参与者名称
	 */
	private String userName;
	/**
	 * 参与者ID
	 */
	private String userId;
	/**
	 * 处理时间
	 */
	private Date date;
	/**
	 * 意见
	 */
	private String opinion;
	/**
	 * 判断节点的结果
	 */
	private String key;

	private String result;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getBusinessId() {
		return businessId;
	}

	public void setBusinessId(String businessId) {
		this.businessId = businessId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getOpinion() {
		return opinion;
	}

	public void setOpinion(String opinion) {
		this.opinion = opinion;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

}
