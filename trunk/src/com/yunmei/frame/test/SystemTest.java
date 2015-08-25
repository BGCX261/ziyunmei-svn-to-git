package com.yunmei.frame.test;

import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.yunmei.demo.model.Order;
import com.yunmei.frame.bo.IAuthorityBO;
import com.yunmei.frame.exception.BOException;
import com.yunmei.frame.utils.Forward;

public class SystemTest extends TestCase {

	ApplicationContext app;
	SessionFactory sessionFactory;
	IAuthorityBO authBO;

	public void setUp() throws Exception {
		app = new FileSystemXmlApplicationContext(
				"web/WEB-INF/system-config-mysql.xml");
		authBO = (IAuthorityBO) app.getBean("sysAuthService");
		sessionFactory = (SessionFactory) app.getBean("sessionFactory");
	}

	public void testDate() throws BOException, SQLException {
		Connection conn = sessionFactory.openSession().connection();
		CallableStatement proc = null;
		try {
			proc = conn.prepareCall("{ call sys_genseq(?,?)}");
			proc.setInt(1, 10);
			proc.registerOutParameter(2, java.sql.Types.INTEGER);
			proc.execute();
			Integer testPrint = proc.getInt(2);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 单表

	public void testInsertTask() throws BOException,
			UnsupportedEncodingException {
		Session session = null;

		session = sessionFactory.openSession();
		session.beginTransaction().begin();
		Order order =new Order();
		order.setName("华来");
		session.save(order);
		Query q = session.createQuery("from Order o");

		for (Order o : (List<Order>) q.list()) {
			System.out.println(o.getName());
		}
		session.beginTransaction().commit();
	}
}
