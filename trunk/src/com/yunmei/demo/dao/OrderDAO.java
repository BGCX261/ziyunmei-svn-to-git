package com.yunmei.demo.dao;

import com.yunmei.demo.model.Order;
import com.yunmei.frame.utils.AbstractBaseDAO;

public class OrderDAO extends AbstractBaseDAO<Order> {
	public void saveOrUpate(Order order) {
		this.getHibernateTemplate().saveOrUpdate(order);
	}
}
