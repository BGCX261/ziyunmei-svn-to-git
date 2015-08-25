package com.yunmei.frame.bo.impl;

import java.util.Date;
import java.util.Map;
import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import com.yunmei.frame.bo.ILogBO;
import com.yunmei.frame.exception.BOException;
import com.yunmei.frame.model.Logs;
import com.yunmei.frame.servlet.Page;
import com.yunmei.frame.utils.Auth;
import com.yunmei.frame.utils.Forward;

@Auth(name = "日志维护")
public class LogBOImpl implements ILogBO {
	private final static Logger log = Logger.getLogger(LogBOImpl.class);

	public Page findByExample(Map map, int start, int max) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			StringBuilder hql = new StringBuilder(
					"select l.id_, l.logtime_,(case  when d.value_ is null then req_method_ else d.name_ end) as req_method_, req_params_, l.success_, l.user_id_, l.user_ip_, l.user_name_ from sys_logs l left join sys_dict_info d on l.req_method_= d.value_ where 1=1 ");
			if (map.get("fromLogTime") != null) {
				hql.append(" and l.logtime_>=:fromLogTime ");
			}
			if (map.get("toLogTime") != null) {
				hql.append(" and l.logtime_>=:toLogTime ");
			}
			if (map.get("userName") != null) {
				hql.append(" and l.user_name_ like :userName");
			}
			if (map.get("reqMethod") != null) {
				hql
						.append(" and (req_method_ like :reqMethod or d.name_ like :reqMethod )");
			}
			SQLQuery sql = session.createSQLQuery(hql.toString());
			if (map.get("fromLogTime") != null) {
				sql.setDate("fromLogTime", new Date(Long.parseLong((String) map
						.get("fromLogTime"))));
			}
			if (map.get("toLogTime") != null) {
				sql.setDate("toLogTime", new Date(Long.parseLong((String) map
						.get("toLogTime"))));
			}
			if (map.get("userName") != null) {
				sql.setString("userName", (String) map.get("userName"));
			}
			if (map.get("reqMethod") != null) {
				sql.setString("reqMethod", (String) map.get("reqMethod"));
			}
			sql.addEntity(Logs.class);
			Page page = new Page();
			page.setCount(sql.list().size());
			sql.setFirstResult(start);
			sql.setMaxResults(max);
			page.setList(sql.list());
			return page;
		} catch (Exception e) {
			e.printStackTrace();
			if (log.isDebugEnabled())
				log.error("日志查询出错:" + e);
			throw new BOException("日志查询出错");
		}
	}

	public Long insertLog(Logs log) throws BOException {
		Session session = this.sessionFactory.getCurrentSession();
		if (log.getReqParams() != null && log.getReqParams().length() > 255) {
			log.setReqParams(log.getReqParams().substring(0, 255));
		}
		return (Long) session.save(log);
	}

	@Auth(name = "日志菜单")
	public Forward enterLogs() throws BOException {
		return new Forward("com/yunmei/frame/view/log.jsp");
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	private SessionFactory sessionFactory;

}