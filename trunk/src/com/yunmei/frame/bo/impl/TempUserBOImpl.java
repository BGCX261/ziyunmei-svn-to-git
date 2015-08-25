package com.yunmei.frame.bo.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Restrictions;

import com.yunmei.frame.bo.ITempUserBO;
import com.yunmei.frame.exception.BOException;
import com.yunmei.frame.model.OrganTree;
import com.yunmei.frame.model.User;
import com.yunmei.frame.servlet.Page;
import com.yunmei.frame.utils.$;
import com.yunmei.frame.utils.Auth;
import com.yunmei.frame.utils.Constraint;
import com.yunmei.frame.utils.Forward;
import com.yunmei.frame.utils.MD5Tools;

@Auth(name = "临时人员管理")
public class TempUserBOImpl implements ITempUserBO {

	public static Logger log = Logger.getLogger(TempUserBOImpl.class);

	@Auth(name = "临时人员")
	public Forward enter() {
		return new Forward("com/yunmei/frame/view/tempUser.jsp");
	}

	public Page find(User user, int start, int maxResults) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		Page page = new Page();
		List list = null;
		if (user.getId() != null) {
			list = new ArrayList();
			User u = (User) session.get(User.class, user.getId());
			if (u != null && u.getDefaultPost() == null) {
				list.add(u);
			}
			page.setCount(1);
			page.setList(list);
			return page;
		}
		Criteria crit = session.createCriteria(User.class);
		Example example = Example.create(user);
		example.enableLike();
		crit.add(example);
		crit.add(Restrictions.isNull("defaultPost"));
		//crit.add(Restrictions.);
		page.setCount(crit.list().size());
		crit.setFirstResult(start);
		crit.setMaxResults(maxResults);
		page.setList(crit.list());
		return page;
	}

	@Auth(name = "临时人员更新")
	public Serializable insert(User user) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			user.setPassword(MD5Tools.encode(user.getPassword()));
			return session.save(user);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("增加临时人员失败");
		}
	}

	@Auth(name = "临时人员转正")
	public void turnNormal(Long userId, Long treeId) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			OrganTree parentNode = (OrganTree) session.load(OrganTree.class,
					treeId);
			User user = (User) session.load(User.class, userId);
			Query update = session
					.createQuery("update OrganTree n set n.left=n.left+?,n.right=n.right+? where n.right>? and n.left>?");
			update.setParameter(0, 2).setParameter(1, 2).setParameter(2,
					parentNode.getRight() - 1).setParameter(3,
					parentNode.getRight() - 1);
			update.executeUpdate();
			update = session
					.createQuery("update OrganTree n set n.right=n.right+? where  n.right>? and n.left<?");
			update.setParameter(0, 2)
					.setParameter(1, parentNode.getRight() - 1).setParameter(2,
							parentNode.getRight());
			update.executeUpdate();

			OrganTree tree = new OrganTree();
			user.setDefaultPost(parentNode.getPost().getId());
			tree.setUser(user);
			tree.setText(user.getName());
			tree.setType(Constraint.USER);
			tree.setParent(treeId);
			tree.setLeft(parentNode.getRight());
			tree.setRight(parentNode.getRight() + 1);
			session.save(tree);
		} catch (Exception e) {
			if (log.isDebugEnabled())
				log.error(e);
			throw new BOException("转正失败");
		}

	}

	public void delete(Long[] userIds) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		for (Long userId : userIds)
			session.delete(session.load(User.class, userId));
	}

	public void update(User user) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		User p = (User) session.load(User.class, user.getId());
		$.copy(user, p);
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	private SessionFactory sessionFactory;
}
