package com.yunmei.demo.bo;

import com.yunmei.demo.model.Order;
import com.yunmei.frame.exception.BOException;
import com.yunmei.frame.model.Opinion;
import com.yunmei.frame.servlet.Page;
import com.yunmei.frame.utils.Forward;

public interface IOrderBO {

	/**
	 * 订单菜单
	 */
	public Forward selectOrder() throws BOException;

	/**
	 * 代办跳转
	 */
	public Forward selectOrder(String id) throws BOException;

	/**
	 * 得到床单 
	 */
	public Order get(Long id) throws BOException;

	/**
	 * 保存床单
	 */
	public Long save(Order order) throws BOException;

	/**
	 * 删除床单
	 */
	public void delete(Long id) throws BOException;

	/**
	 *订单查询 
	 */
	public Page find(Order order, int start, int max) throws BOException;

	/**
	 * 订单提交
	 */
	public String submit(Order order, String workflow) throws BOException;

	/**
	 * 订单审批
	 */
	public String audit(Opinion opinion) throws BOException;
	
	/**
	 * 财务签字
	 */
	public String signed(Order order) throws BOException;
}
