package com.yunmei.frame.bo.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import com.yunmei.frame.bo.IDictBO;
import com.yunmei.frame.exception.BOException;
import com.yunmei.frame.model.Dict;
import com.yunmei.frame.model.DictInfo;
import com.yunmei.frame.servlet.Page;
import com.yunmei.frame.utils.Auth;
import com.yunmei.frame.utils.Forward;

@Auth(name = "字典维护")
public class DictBOImpl implements IDictBO {
	private static Logger log = Logger.getLogger(DictBOImpl.class);

	@Auth(name = "字典菜单")
	public Forward enterDict() {
		return new Forward("com/yunmei/frame/view/dict.jsp");
	}

	public List findDictInfoByDictId(String dictId) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		Query q = session
				.createQuery("from DictInfo info where info.dictId=:dictId");
		q.setParameter("dictId", dictId);
		return q.list();
	}

	public Page findDictInfoByDictId(String dictId, Integer start, Integer max)
			throws BOException {
		Session session = sessionFactory.getCurrentSession();
		Page page = new Page();
		Query q = session
				.createQuery("from DictInfo info where info.dictId=:dictId");
		q.setParameter("dictId", dictId);
		page.setCount(q.list().size());
		q.setFirstResult(start);
		q.setMaxResults(max);
		page.setList(q.list());
		return page;
	}

	public Page findDicts(Dict dict, Integer start, Integer max)
			throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			Page page = new Page();
			if (dict.getId() != null) {
				Object obj = session.get(Dict.class, dict.getId());
				if (obj == null) {
					page.setList(new ArrayList());
				} else {
					List list = new ArrayList();
					list.add(obj);
					page.setList(list);
				}
				return page;
			}

			Criteria crit = session.createCriteria(Dict.class);
			Example example = Example.create(dict);
			example.enableLike();
			crit.add(example);
			page.setCount(crit.list().size());
			crit.setFirstResult(start);
			crit.setMaxResults(max);
			page.setList(crit.list());
			return page;
		} catch (Exception e) {
			if (log.isDebugEnabled())
				log.error("字典查询失败" + e);
			throw new BOException("字典查询失败");
		}
	}

	@Auth(name = "字典明细更新")
	public List<Map> saveDictInfo(List<DictInfo> insert, List<DictInfo> update)
			throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			List<Map> result = new ArrayList<Map>();
			for (DictInfo info : insert) {
				Long oldId = info.getId();
				Map map = new HashMap();
				map.put(oldId, session.save(info));
				result.add(map);
			}
			for (DictInfo info : update) {
				session.update(info);
				Map map = new HashMap();
				map.put(info.getId(), info.getId());
				result.add(map);
			}
			return result;
		} catch (Exception e) {
			if (log.isDebugEnabled())
				log.error("字典明细更新失败" + e);
			throw new BOException("字典明细更新失败");
		}
	}

	public void deleteDict(Long id) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		session.delete(session.load(Dict.class, id));
	}

	public void deleteDictInfo(Long dictInfoId) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		Query q = session
				.createQuery("delete from DictInfo info where info.id=:dictInfoId");
		q.setParameter("dictInfoId", dictInfoId);
		q.executeUpdate();
	}

	@Auth(name = "字典更新")
	public List<Map> saveDict(List<Dict> insert, List<Dict> update)
			throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			List<Map> result = new ArrayList<Map>();
			for (Dict info : insert) {
				String oldId = info.getId();
				System.out.println("name:" + info.getName());
				Map map = new HashMap();
				map.put(oldId, session.save(info));
				result.add(map);
			}
			for (Dict info : update) {
				session.update(info);
				Map map = new HashMap();
				map.put(info.getId(), info.getId());
				result.add(map);
			}
			return result;
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.error("字典更新失败" + e);
			}
			throw new BOException("字典更新失败");
		}
	}

	@Auth(name = "字典删除")
	public void deleteDict(String id) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		session.delete(session.load(Dict.class, id));
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	private SessionFactory sessionFactory;
}
