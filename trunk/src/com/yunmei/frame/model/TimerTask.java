package com.yunmei.frame.model;

import java.util.Date;

public class TimerTask {

	private Long id;
	/**
	 * 任务名
	 */
	private String name;
	/**
	 * 启动，不启动
	 */
	private String state;
	/**
	 * 每天，每月，每年
	 */
	private String type;

	private Integer fixed;
	/**
	 * 开始日期
	 */
	private Date startDate;
	/**
	 * 失效日期
	 */
	private Date endDate;
	/**
	 * 间隔时间
	 */
	private Integer repeatInterval;

	private Date executeDate;

	private String executeTime;

	private String executeTimeEnd;

	private String executeType;

	/**
	 * 被调用业务逻辑
	 */
	private String invoke;
	/**
	 * 被调用参数
	 */
	private String params;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Integer getRepeatCount() {
		Integer repeatCount;
		String ends[] = this.executeTimeEnd.split(":");
		String starts[] = this.executeTime.split(":");
		Integer count = (Integer.parseInt(ends[0]) - Integer
				.parseInt(starts[0]))
				* 60
				* 60
				+ (Integer.parseInt(ends[1]) - Integer.parseInt(starts[1]))
				* 60
				+ (Integer.parseInt(ends[2]) - Integer.parseInt(starts[2]));
		if (repeatInterval == null) {
			repeatCount = count;
		} else
			repeatCount = count / repeatInterval;
		return repeatCount;
	}

	public Date getExecuteDateTime() {
		String starts[] = this.executeTime.split(":");
		Integer allSec = Integer.parseInt(starts[0]) * 60 * 60
				+ Integer.parseInt(starts[1]) * 60
				+ Integer.parseInt(starts[2]);
		Date nextTime = null;
		if ("0".equals(this.type)) {
			nextTime = new Date(this.executeDate.getTime() + allSec * 1000);
		} else {
			nextTime = new Date();
			nextTime.setHours(Integer.parseInt(starts[0]));
			nextTime.setMinutes(Integer.parseInt(starts[1]));
			nextTime.setSeconds(Integer.parseInt(starts[2]));
		}
		return nextTime;
	}

	public Integer getRepeatInterval() {
		return repeatInterval;
	}

	public void setRepeatInterval(Integer repeatInterval) {
		this.repeatInterval = repeatInterval;
	}

	public String getInvoke() {
		return invoke;
	}

	public void setInvoke(String invoke) {
		this.invoke = invoke;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public Integer getFixed() {
		return fixed;
	}

	public void setFixed(Integer fixed) {
		this.fixed = fixed;
	}

	public Date getExecuteDate() {
		return executeDate;
	}

	public void setExecuteDate(Date executeDate) {
		this.executeDate = executeDate;
	}

	public void setExecuteTime(String executeTime) {
		this.executeTime = executeTime;
	}

	public String getExecuteTime() {
		return executeTime;
	}

	public boolean isValidExecuteDateTime() {
		return new Date().getTime() <= this.getExecuteDateTime().getTime()
				&& this.getExecuteDateTime().getTime() < (new Date().getTime() + 24 * 60 * 60 * 1000L);
	}

	public String getExecuteTimeEnd() {
		return executeTimeEnd;
	}

	public void setExecuteTimeEnd(String executeTimeEnd) {
		this.executeTimeEnd = executeTimeEnd;
	}

	public String getExecuteType() {
		return executeType;
	}

	public void setExecuteType(String executeType) {
		this.executeType = executeType;
	}
}
