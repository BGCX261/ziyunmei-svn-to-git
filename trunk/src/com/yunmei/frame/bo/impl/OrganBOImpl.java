package com.yunmei.frame.bo.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.json.JSONArray;
import org.json.JSONObject;

import com.yunmei.frame.bo.IOrganBO;
import com.yunmei.frame.exception.BOException;
import com.yunmei.frame.model.Organ;
import com.yunmei.frame.model.OrganTree;
import com.yunmei.frame.model.Post;
import com.yunmei.frame.model.PostRole;
import com.yunmei.frame.model.User;
import com.yunmei.frame.servlet.Page;
import com.yunmei.frame.utils.$;
import com.yunmei.frame.utils.Auth;
import com.yunmei.frame.utils.Constraint;
import com.yunmei.frame.utils.Forward;
import com.yunmei.frame.utils.MD5Tools;

@Auth(name = "组织机构")
@SuppressWarnings("unchecked")
public class OrganBOImpl implements IOrganBO {

	private static Logger log = Logger.getLogger(OrganBOImpl.class);
	private SessionFactory sessionFactory;

	@Auth(name = "组织结构")
	public Forward enterOrgan() {
		return new Forward("com/yunmei/frame/view/organ.jsp");
	}

	@Auth(name = "删除节点")
	public void deleteNode(Long nodeId) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			OrganTree node = (OrganTree) session.load(OrganTree.class, nodeId);
			if(node.getChildren().size()>0)
				throw new BOException("删除节点失败,该节点还有子节点");
			if (node.getType() == Constraint.USER) {
				if (!check(node.getUser().getId()))
					node.getUser().setDefaultPost(null);
				node.setUser(null);
			}
			session.delete(node);
			Integer value = node.getRight() - node.getLeft() + 1;
			Query q = session
					.createQuery("update OrganTree n set n.left=n.left-?,n.right=n.right-? where  n.left>?");
			q.setInteger(0, value).setInteger(1, value).setInteger(2,
					node.getRight());
			q.executeUpdate();
			q = session
					.createQuery("update OrganTree n set n.right=n.right-? where  n.right>? and n.left<?");
			q.setInteger(0, value).setInteger(1, node.getRight()).setInteger(2,
					node.getLeft());
			q.executeUpdate();
		} catch (Exception e) {
			if (log.isDebugEnabled())
				log.error("删除节点失败" + e);
			throw new BOException("删除节点失败");
		}
	}

	public String findOrganTree(Long id) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			Query q = session
					.createQuery("from OrganTree tree where tree.parent=:parent order by tree.left");
			q.setParameter("parent", id);
			Iterator iter = q.iterate();
			JSONArray array = new JSONArray();
			while (iter.hasNext()) {
				JSONObject json = $.objectToJSON(iter.next());
				if (json.has("left") && json.has("right")) {
					json.put("leaf",
							json.getInt("right") - json.getInt("left") == 1);
				} else {
					json.put("leaf", "true".equals(json.getString("leaf")));
				}
				switch (json.getString("type").charAt(0)) {
				case Constraint.ORGAN:
					json.put("icon", "images/comp.gif");
					break;
				case Constraint.POST:
					json.put("icon", "images/post.gif");
					break;
				case Constraint.USER:
					json.put("icon", "images/user.gif");
					break;
				}
				array.put(json);
			}
			return array.toString();
		} catch (Exception e) {
			if (log.isDebugEnabled())
				log.error("查询组织结构树失败" + e);
			throw new BOException("查询组织结构树失败");
		}
	}

	public List<OrganTree> findSuperNodes(Long userId, Character type)
			throws BOException {
		Session session = sessionFactory.getCurrentSession();
		Query q = session
				.createQuery("select g from OrganTree g,OrganTree o where g.left<o.left and g.right>o.right and o.user.id=:userId and g.type=:type");
		q.setParameter("type", type).setParameter("userId", userId);
		return q.list();
	}

	public Object getNode(Long nodeId) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		OrganTree node = (OrganTree) session.load(OrganTree.class, nodeId);
		switch (node.getType()) {
		case Constraint.ORGAN: {
			return node.getOrgan();
		}
		case Constraint.POST:
			return node.getPost();
		case Constraint.USER:
			return node.getUser();
		default:
			throw new BOException("参数有问题");
		}
	}

	public List findSubNodes(Long id, String type) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			OrganTree root = (OrganTree) session.load(OrganTree.class, id);
			String hql = null;
			switch (type.charAt(0)) {
			case Constraint.ORGAN:
				hql = "select u from Organ u,OrganTree tree where tree.type='C' and tree.left>? and tree.right<? and u.id=tree.organ";
				break;
			case Constraint.POST:
				hql = "select u from Post u,  OrganTree tree where tree.type='P' and tree.left>? and tree.right<? and u.id=tree.post";
				break;
			case Constraint.USER:
				hql = "from User u where exists( select 1 from OrganTree tree where tree.type='U' and tree.left>? and tree.right<? and u.id=tree.user.id)";
				break;
			}
			System.out.println(root.getLeft() + "," + root.getRight());
			Query q = session.createQuery(hql);
			q.setParameter(0, root.getLeft()).setParameter(1, root.getRight());
			return q.list();
		} catch (Exception e) {
			log.error(e);
			throw new BOException("查询组织结构树失败");
		}
	}

	public Map insertOrgan(Organ organ, Long parent) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			OrganTree parentNode = (OrganTree) session.load(OrganTree.class,
					parent);
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

			if (organ.getAdmin() != null) {
				OrganTree user = (OrganTree) session.load(OrganTree.class,
						organ.getAdmin().getId());
				organ.setAdmin(user.getUser());
			}
			OrganTree tree = new OrganTree();
			tree.setOrgan(organ);
			tree.setText(organ.getName());
			tree.setType(Constraint.ORGAN);
			tree.setParent(parent);
			tree.setLeft(parentNode.getRight());
			tree.setRight(parentNode.getRight() + 1);
			Map map = new HashMap();
			map.put("treeId", session.save(tree));
			map.put("objId", organ.getId());
			return map;
		} catch (Throwable e) {
			if (log.isDebugEnabled())
				log.error("添加科室失败" + e);
			throw new BOException("添加科室失败");
		}
	}

	public Map insertPost(Post post, Long parent) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			OrganTree parentNode = (OrganTree) session.load(OrganTree.class,
					parent);
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
			tree.setPost(post);
			tree.setText(post.getName());
			tree.setType(Constraint.POST);
			tree.setParent(parent);
			tree.setLeft(parentNode.getRight());
			tree.setRight(parentNode.getRight() + 1);
			Map map = new HashMap();
			map.put("treeId", session.save(tree));
			map.put("objId", post.getId());
			return map;
		} catch (Exception e) {
			if (log.isDebugEnabled())
				log.error("添加岗位失败" + e);
			throw new BOException("添加岗位失败");
		}
	}

	@Auth(name = "增加节点")
	public Map insertUser(User user, Long parent) throws BOException {
		try {
			String md5Password = MD5Tools.encode(user.getPassword());
			user.setPassword(md5Password); // 对新添加人员的密码进行MD5加密 sun 09-11-10
			Session session = sessionFactory.getCurrentSession();
			OrganTree parentNode = (OrganTree) session.load(OrganTree.class,
					parent);
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
			if (user.getDefaultPost() == null) {
				user.setDefaultPost(parentNode.getPost().getId());
			}
			tree.setUser(user);
			tree.setText(user.getName());
			tree.setType(Constraint.USER);
			tree.setParent(parent);
			tree.setLeft(parentNode.getRight());
			tree.setRight(parentNode.getRight() + 1);
			Map map = new HashMap();
			map.put("treeId", session.save(tree));
			map.put("objId", user.getId());
			return map;
		} catch (Exception e) {
			if (log.isDebugEnabled())
				log.error(e);
			throw new BOException("添加职员失败");
		}
	}

	public void deletePostRole(PostRole pr) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			Query q = session
					.createQuery("delete from PostRole pr where pr.post=:postId and pr.role=:roleId");
			q.setLong("postId", pr.getPost().getId());
			q.setLong("roleId", pr.getRole().getId());
			q.executeUpdate();
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.error("删除岗位角色失败" + e);
			}
			throw new BOException("除岗位角色失败");
		}
	}

	@Auth(name = "岗位角色更新")
	public void savePostRole(PostRole pr) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			session.save(pr);
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.error("添加岗位角色失败" + e);
			}
			throw new BOException("添加岗位角色失败");
		}
	}

	public Page findUsers(User user, int start, int maxResults)
			throws BOException {
		Session session = sessionFactory.getCurrentSession();
		Page page = new Page();
		Criteria crit = session.createCriteria(User.class);
		Example example = Example.create(user);
		example.enableLike();
		crit.add(example);
		page.setCount(crit.list().size());
		crit.setFirstResult(start);
		crit.setMaxResults(maxResults);
		page.setList(crit.list());
		return page;
	}

	public Serializable updateOrgan(Organ organ) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			if (organ.getAdmin() != null) {
				User user = (User) session.load(User.class, organ.getAdmin()
						.getId());
				organ.setAdmin(user);
			}
			session.merge(organ);
			Query q = session
					.createQuery("from OrganTree o where o.organ=:organId");
			q.setLong("organId", organ.getId());
			OrganTree ot = (OrganTree) q.uniqueResult();
			ot.setText(organ.getName());
			return ot.getId();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("更新居室节点失败");
		}
	}

	public Serializable updatePost(Post post) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		session.merge(post);
		Query q = session.createQuery("from OrganTree o where o.post=:postId");
		q.setLong("postId", post.getId());
		OrganTree ot = (OrganTree) q.uniqueResult();
		ot.setText(post.getName());
		return ot.getId();
	}

	@Auth(name = "更新节点")
	public List<Serializable> updateUser(User user) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		User p = (User) session.load(User.class, user.getId());
		$.copy(user, p);
		Query q = session.createQuery("from OrganTree o where o.user=:userId");
		q.setLong("userId", user.getId());
		List<OrganTree> list = q.list();
		List<Serializable> ids = new ArrayList<Serializable>();
		for (OrganTree t : list) {
			t.setText(user.getName());
			ids.add(t.getId());
		}
		return ids;
	}

	public String findUserTree(Long id) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			List list = session
					.createQuery(
							"from OrganTree tree where tree.parent=:parent and (tree.type='U' or exists(select 1 from OrganTree up where up.left>tree.left and up.right<tree.right and up.type='U')) order by tree.left")
					.setLong("parent", id).list();
			JSONArray array = new JSONArray();
			for (int i = 0; i < list.size(); i++) {
				JSONObject json = $.objectToJSON(list.get(i));
				json.put("leaf", json.getString("type").equals("U"));
				switch (json.getString("type").charAt(0)) {
				case Constraint.ORGAN:
					json.put("icon", "images/comp.gif");
					break;
				case Constraint.POST:
					json.put("icon", "images/post.gif");
					break;
				case Constraint.USER:
					json.put("icon", "images/user.gif");
					break;
				}
				array.put(json);
			}
			return array.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("用户树得到失败");
		}
	}

	public String findPostTree(Long id) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			List list = session
					.createQuery(
							"from OrganTree tree where tree.parent=:parent and (tree.type='P' or exists(select 1 from OrganTree up where up.left>tree.left and up.right<tree.right and up.type='P')) order by tree.left")
					.setLong("parent", id).list();
			JSONArray array = new JSONArray();
			for (int i = 0; i < list.size(); i++) {
				JSONObject json = $.objectToJSON(list.get(i));
				json.put("leaf", json.getString("type").equals("P"));
				array.put(json);
				switch (json.getString("type").charAt(0)) {
				case Constraint.ORGAN:
					json.put("icon", "images/comp.gif");
					break;
				case Constraint.POST:
					json.put("icon", "images/post.gif");
					break;
				}
			}
			return array.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("用户树得到失败");
		}
	}

	public User getUser(Long userId) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		return (User) session.get(User.class, userId);
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	/**
	 * 修改用户密码 sun 09-11-10
	 */
	@Auth(name = "密码修改")
	public String alterPassword(Long userid, String password) {
		// TODO Auto-generated method stub
		Session session = sessionFactory.getCurrentSession();
		User user = (User) session.load(User.class, userid);
		String md5password = MD5Tools.encode(password);
		user.setPassword(md5password);
		return null;
	}

	public String alterPassword(Long userid, String oldpassword, String password)
			throws BOException {
		Session session = sessionFactory.getCurrentSession();
		User user = (User) session.load(User.class, userid);
		String md5OldPassword = MD5Tools.encode(oldpassword);
		String md5password = MD5Tools.encode(password);
		if (md5OldPassword.equals(user.getPassword())) {
			user.setPassword(md5password);
			return "success";
		}
		return "failure";
	}

	public void changePost(Long userTreeId, Long postTreeId) throws BOException {

		Session session = sessionFactory.getCurrentSession();
		OrganTree son = (OrganTree) session.load(OrganTree.class, userTreeId);
		OrganTree father = (OrganTree) session
				.load(OrganTree.class, postTreeId);
		if (check(son.getUser().getId(), postTreeId)) {
			throw new BOException("该用户已存在");
		}
		try {
			if (son.getRight() < father.getRight())
				moveDown(son, father);
			else
				moveUp(son, father);
			son.getUser().setDefaultPost(father.getPost().getId());
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("调岗失败");
		}
	}

	private void moveDown(OrganTree son, OrganTree father) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		int value = son.getRight() - son.getLeft() + 1;
		int selfValue = father.getRight() - 1 - son.getRight();
		String updateBoth = "update OrganTree tree set tree.left=tree.left-:value,tree.right=tree.right-:value"
				+ " where tree.left>:sLeft and tree.right>:sRight and tree.right<:fRight";
		String updateLeft = "update OrganTree tree set tree.right=tree.right-:value"
				+ " where tree.left<:sLeft and tree.right>:sRight and tree.right<:fRight";
		String updateRight = "update OrganTree tree set tree.left=tree.left-:value "
				+ " where tree.left<=:fLeft and tree.right>=:fRight and tree.left>:sLeft";
		String updateSelf = "from OrganTree tree"
				+ " where tree.left>=:sLeft and tree.right<=:sRight";
		List<OrganTree> self = session.createQuery(updateSelf).setInteger(
				"sLeft", son.getLeft()).setInteger("sRight", son.getRight())
				.list();
		Query upateLeftQ = session.createQuery(updateLeft).setParameter(
				"value", value).setParameter("sLeft", son.getLeft())
				.setParameter("sRight", son.getRight()).setParameter("fRight",
						father.getRight());
		Query updateRightQ = session.createQuery(updateRight).setInteger(
				"value", value).setInteger("sLeft", son.getLeft()).setInteger(
				"fLeft", father.getLeft()).setInteger("fRight",
				father.getRight());
		Query updateBothQ = session.createQuery(updateBoth).setInteger("value",
				value).setInteger("sLeft", son.getLeft()).setInteger("sRight",
				son.getRight()).setInteger("fRight", father.getRight());
		upateLeftQ.executeUpdate();
		updateRightQ.executeUpdate();
		updateBothQ.executeUpdate();
		for (OrganTree auth : self) {
			auth.setLeft(auth.getLeft() + selfValue);
			auth.setRight(auth.getRight() + selfValue);
		}
		son.setParent(father.getId());
	}

	private void moveUp(OrganTree son, OrganTree father) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		int value = son.getLeft() - son.getRight() - 1;
		int selfValue = father.getRight() - 1 - value - son.getRight();
		String updateLeft = "update OrganTree tree set tree.right=tree.right-:value"
				+ " where tree.left<=:fLeft and tree.right>=:fRight and tree.right<:sRight";
		String updateRight = "update OrganTree tree set tree.left=tree.left-:value "
				+ " where tree.left<:sLeft and tree.right>:sRight and tree.left>:fLeft";
		String updateSelf = "from OrganTree tree"
				+ " where tree.left>=:sLeft and tree.right<=:sRight";
		String updateBoth = "update OrganTree tree set tree.left=tree.left-:value,tree.right=tree.right-:value"
				+ " where tree.left>:fLeft and tree.right>:fRight and tree.right<:sLeft";
		List<OrganTree> self = session.createQuery(updateSelf).setInteger(
				"sLeft", son.getLeft()).setInteger("sRight", son.getRight())
				.list();
		Query upateLeftQ = session.createQuery(updateLeft).setParameter(
				"value", value).setParameter("fLeft", father.getLeft())
				.setParameter("fRight", father.getRight()).setParameter(
						"sRight", son.getRight());
		Query updateRightQ = session.createQuery(updateRight).setInteger(
				"value", value).setInteger("sLeft", son.getLeft()).setInteger(
				"sRight", son.getRight()).setInteger("fLeft", father.getLeft());
		Query updateBothQ = session.createQuery(updateBoth).setInteger("value",
				value).setInteger("fLeft", father.getLeft()).setInteger(
				"fRight", father.getRight()).setInteger("sLeft", son.getLeft());
		upateLeftQ.executeUpdate();
		updateRightQ.executeUpdate();
		updateBothQ.executeUpdate();
		for (OrganTree auth : self) {
			auth.setLeft(auth.getLeft() + selfValue);
			auth.setRight(auth.getRight() + selfValue);
		}
		son.setParent(father.getId());
	}

	private boolean check(Long userId, Long postTreeId) {
		Session session = this.sessionFactory.getCurrentSession();
		Query q = session
				.createQuery("from OrganTree t where t.user.id=:userId and exists( select 1 from OrganTree s where s.id=:postId and t.left>s.left and t.right<s.right )");
		q.setParameter("userId", userId);
		q.setParameter("postId", postTreeId);
		return q.iterate().hasNext();
	}

	private boolean check(Long userId) {
		Session session = this.sessionFactory.getCurrentSession();
		Query q = session
				.createQuery("from OrganTree t where t.user.id=:userId");
		q.setParameter("userId", userId);
		return q.list().size() > 1;
	}

	public Serializable insertUser2Post(Long userTreeId, Long postTreeId)
			throws BOException {
		Session session = sessionFactory.getCurrentSession();
		OrganTree userNode = (OrganTree) session.load(OrganTree.class,
				userTreeId);
		OrganTree postNode = (OrganTree) session.load(OrganTree.class,
				postTreeId);
		if (check(userNode.getUser().getId(), postTreeId)) {
			throw new BOException("该用户已存在");
		}
		try {
			Query update = session
					.createQuery("update OrganTree n set n.left=n.left+?,n.right=n.right+? where n.right>? and n.left>?");
			update.setParameter(0, 2).setParameter(1, 2).setParameter(2,
					postNode.getRight() - 1).setParameter(3,
					postNode.getRight() - 1);
			update.executeUpdate();
			update = session
					.createQuery("update OrganTree n set n.right=n.right+? where  n.right>? and n.left<?");
			update.setParameter(0, 2).setParameter(1, postNode.getRight() - 1)
					.setParameter(2, postNode.getRight());
			update.executeUpdate();

			OrganTree tree = new OrganTree();
			tree.setUser(userNode.getUser());
			tree.setText(userNode.getUser().getName());
			tree.setType(Constraint.USER);
			tree.setParent(postTreeId);
			tree.setLeft(postNode.getRight());
			tree.setRight(postNode.getRight() + 1);
			return session.save(tree);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("兼职失败");
		}
	}
}
