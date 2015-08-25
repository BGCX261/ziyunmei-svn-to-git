package com.yunmei.frame.bo.impl;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.json.JSONArray;
import org.json.JSONObject;

import com.yunmei.frame.bo.IAuthorityBO;
import com.yunmei.frame.exception.BOException;
import com.yunmei.frame.model.Authority;
import com.yunmei.frame.model.DataFilter;
import com.yunmei.frame.model.OrganTree;
import com.yunmei.frame.model.Role;
import com.yunmei.frame.model.RoleAuthority;
import com.yunmei.frame.model.User;
import com.yunmei.frame.model.UserRole;
import com.yunmei.frame.servlet.Page;
import com.yunmei.frame.utils.$;
import com.yunmei.frame.utils.Auth;
import com.yunmei.frame.utils.Constraint;
import com.yunmei.frame.utils.Forward;
import com.yunmei.frame.utils.MD5Tools;
import com.yunmei.frame.utils.SpringUtils;

@Auth(name = "权限管理")
public class AuthorityBOImpl implements IAuthorityBO {
	private SessionFactory sessionFactory;
	private static Logger log = Logger.getLogger(AuthorityBOImpl.class);
	private final static String superOrgan = "select g from OrganTree g,OrganTree o where g.left<o.left and g.right>o.right and o.user.id=:userId";

	@Auth(name = "角色权限配置")
	public Forward enterAuth() {
		return new Forward("com/yunmei/frame/view/auth.jsp");
	}

	public List findRolesByUserId(Long id) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		Connection conn = session.connection();
		List list = new ArrayList();
		PreparedStatement pt = null;
		try {
			pt = conn
					.prepareStatement("SELECT * FROM v_sys_user_role v where v.user_id_=? or exists(select 1 from sys_organ_tree p,sys_organ_tree u where u.user_id_=? and u.parent_=p.id_ and v.post_id_=p.post_id_)");
			pt.setLong(1, id);
			pt.setLong(2, id);
			ResultSet rs = pt.executeQuery();
			while (rs.next()) {
				Map map = new HashMap();
				String roleId = rs.getString("role_id_");
				String userId = rs.getString("user_id_");
				map.put("id", userId == null ? null : roleId);
				map.put("name", rs.getString("name_"));
				map.put("remark", rs.getString("remark_"));
				map.put("type", userId == null ? "岗位角色" : "用户角色");
				list.add(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				pt.close();
			} catch (SQLException e) {
			}
		}
		return list;
	}

	@Auth(name = "数据过滤配置")
	public Forward enterDataAuth() {
		return new Forward("com/yunmei/frame/view/data.jsp");
	}

	public List findNotRolesByPostId(Long id) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		Query q = session
				.createQuery("select role from Role role where not exists(select 1 from PostRole pr where pr.post=:postId and role.id=pr.role.id)");
		q.setLong("postId", id);
		return q.list();
	}

	public List findRolesByPostId(Long id) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		Query q = session
				.createQuery("select role from PostRole pr inner join pr.role role left join pr.post post where post.id=:postId");
		q.setLong("postId", id);
		return q.list();
	}

	public Page findInvokeMethod(Map map1, Integer start, Integer end)
			throws BOException {
		String beanNames[] = SpringUtils.getBeans();
		List<Map> list = new ArrayList();
		for (String bean : beanNames) {
			if (bean.endsWith("Service")) {
				Object proxy = SpringUtils.getBean(bean);
				Class clazz = $.getTarget(proxy);
				Auth isHave = (Auth) clazz.getAnnotation(Auth.class);
				if (isHave != null) {
					for (Method m : clazz.getDeclaredMethods()) {
						String methodName = m.getName();
						String nameArg = (String) map1.get("name");
						if (methodName.startsWith("set"))
							continue;
						if (nameArg != null
								&& !(bean + methodName).contains(nameArg)) {
							continue;
						}
						Map map = new HashMap();
						map.put("id", bean + "." + methodName);
						map.put("class", isHave.name());
						list.add(map);
						Class paramsClass[] = m.getParameterTypes();
						if (paramsClass.length > 2)
							continue;
						for (int i = 0; i < paramsClass.length; i++) {
							map
									.put("param" + i, paramsClass[i]
											.getSimpleName());
						}
					}
				}
			}
		}
		Page page = new Page();
		page.setCount(list.size());
		int e = list.size() < (start + end) ? list.size() : start + end;
		page.setList(list.subList(start, e));
		return page;
	}

	@Auth(name = "加载新构件")
	public void reload() throws BOException {
		try {
			String beanNames[] = SpringUtils.getBeans();
			for (String bean : beanNames) {
				Object proxy = SpringUtils.getBean(bean);
				Class clazz = $.getTarget(proxy);
				Auth isHave = (Auth) clazz.getAnnotation(Auth.class);
				if (isHave != null) {
					Authority root = new Authority();
					root.setText(isHave.name());
					root.setUrl(bean);
					root.setRemark("这是根");
					root.setType(Constraint.ROOT);
					// 创建根
					create(root);
					// 更新菜单
					for (Method m : clazz.getDeclaredMethods()) {
						Auth annotation = m.getAnnotation(Auth.class);
						if (annotation != null) {
							Authority auth = new Authority();
							auth.setUrl(bean + "." + m.getName());
							auth.setText(annotation.name());
							if (m.getReturnType().equals(Forward.class)) {
								auth.setType(Constraint.MEMU);
							} else {
								if (annotation.fields().length() == 0)
									auth.setType(Constraint.OPER);
								else
									auth.setType(Constraint.DATA);
							}
							create(auth);
						}
					}
				}
			}
		} catch (BOException e) {
			e.printStackTrace();
			throw new BOException("重新加载菜单失败");
		}
	}

	private void create(Authority auth) throws BOException {
		Session session = sessionFactory.openSession();
		session.beginTransaction().begin();
		Query q = session.createQuery("from Authority a where a.url=:url");
		q.setString("url", auth.getUrl());
		if (q.list().size() > 0)
			return;
		// 如果当前节点是根
		if (auth.getType() == Constraint.ROOT) {
			Authority root = (Authority) session.load(Authority.class, -1L);
			root.setRight(root.getRight() + 2);
			auth.setLeft(root.getRight() - 2);
			auth.setRight(root.getRight() - 1);
			auth.setParent(-1L);
			session.save(auth);
		} else {
			Authority parent = (Authority) session.createQuery(
					"from Authority a where a.url=?").setString(0,
					auth.getUrl().split("\\.")[0]).uniqueResult();
			if (parent != null) {
				Query update = session
						.createQuery("update Authority n set n.left=n.left+?,n.right=n.right+? where n.right>? and n.left>?");
				update.setParameter(0, 2).setParameter(1, 2).setParameter(2,
						parent.getRight() - 1).setParameter(3,
						parent.getRight() - 1);
				update.executeUpdate();
				update = session
						.createQuery("update Authority n set n.right=n.right+? where  n.right>? and n.left<?");
				update.setParameter(0, 2)
						.setParameter(1, parent.getRight() - 1).setParameter(2,
								parent.getRight());
				update.executeUpdate();
				auth.setParent(parent.getId());
				auth.setLeft(parent.getRight());
				auth.setRight(parent.getRight() + 1);
				session.save(auth);
			} else
				System.out.println("有错误呀,你个傻叉");
		}
		session.beginTransaction().commit();
		session.close();
	}

	public Forward login(String login, String password) throws BOException {
		try {
			String url = "index.jsp";
			Session session = sessionFactory.openSession();
			Query q = session
					.createQuery("from User u where u.login=? and u.password=?");
			q.setString(0, login).setString(1, MD5Tools.encode(password));
			User user = (User) q.uniqueResult();

			if (user != null) {
				User.setUser(user);
				loadUserInfo(user);
				loadAuths(user.getId(), user.getDefaultPost());
				url = "com/yunmei/frame/view/main.jsp";
			} else {
				user = new User();
				user.setId(-1L);
				user.setName(login);
				User.setUser(user);
			}
			user.setIp($.getRequest().getRemoteHost());
			return new Forward(url);
		} catch (BOException e) {
			e.printStackTrace();
			throw new BOException("登录失败");
		}
	}

	private void loadAuths(Long userId, Long postId) throws BOException {
		try {
			List<Long> list = new ArrayList<Long>();
			Session session = sessionFactory.getCurrentSession();
			SQLQuery sql = session
					.createSQLQuery("SELECT distinct auth_id_ as id_,text_,left_,right_,type_,url_,parent_,remark_ "
							+ " FROM v_sys_user_auth where user_id_=:userId or post_id_=:postId order by left_");
			sql.setLong("userId", userId);
			sql.setLong("postId", postId == null ? -1 : postId);
			sql.addEntity(Authority.class);
			User.getUser().setAuths(sql.list());
			User.getUser().setDefaultPost(postId);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("加载数据权限失败");
		}
	}

	private void loadUserInfo(User user) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			Query q = session.createQuery(superOrgan);
			q.setParameter("userId", user.getId());
			List<OrganTree> list = q.list();
			user.getPosts().clear();
			for (OrganTree g : list) {
				if (g.getType() == Constraint.POST)
					user.getPosts().add(g.getPost());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("加载用户信息失败");
		}
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public List findFilters() throws BOException {
		Session session = sessionFactory.getCurrentSession();
		List list = session.createQuery("from Authority a where a.parent=-1")
				.list();
		Authority e = new Authority();
		e.setId(-1L);
		e.setText("所有");
		list.add(e);
		return list;
	}

	public String findAuths(Long parentId) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			Authority parentNode = (Authority) session.load(Authority.class,
					parentId);
			Query q = session
					.createQuery("from Authority a where a.left>=:left and a.right<=:right and a.type<>'D' and a.parent is not null order by a.left");
			q.setInteger("left", parentNode.getLeft());
			q.setInteger("right", parentNode.getRight());
			Iterator iter = q.iterate();
			Stack<JSONObject> stack = new Stack<JSONObject>();
			JSONArray array = new JSONArray();
			while (iter.hasNext()) {
				Authority a = (Authority) iter.next();
				JSONObject json = $.objectToJSON(a, "id,text");
				json.put("leaf", a.getRight() - a.getLeft() == 1);
				json.put("checked", false);
				if (a.getType() == Constraint.MEMU)
					json.put("icon", "images/menu.gif");
				else if (a.getType() == Constraint.OPER)
					json.put("icon", "images/oper.gif");
				while (!stack.empty()) {
					JSONObject parent = stack.pop();
					if (parent.getString("id").equals(a.getParent().toString())) {
						if (!parent.has("children")) {
							JSONArray pares = new JSONArray();
							parent.put("children", pares);
						}
						parent.getJSONArray("children").put(json);
						stack.push(parent);
						stack.push(json);
						break;
					}
				}
				if (stack.empty()) {
					array.put(json);
					stack.push(json);
				}
			}
			return array.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("加载数据权限失败");
		}
	}

	public Page findRoles(Role role, int start, int maxResults)
			throws BOException {
		try {
			Page page = new Page();
			Session session = sessionFactory.getCurrentSession();
			Criteria crit = session.createCriteria(Role.class);
			Example example = Example.create(role);
			example.enableLike();
			crit.add(example);
			page.setCount(crit.list().size());
			crit.setFirstResult(start);
			crit.setMaxResults(maxResults);
			page.setList(crit.list());
			return page;
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("角色查询失败");
		}
	}

	public List<Long> getAuthsByRoleId(Long roleId) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			return session
					.createSQLQuery(
							"select auth_id_ from (select auth_id_,left_ from v_sys_role_auth where parent_ is not null and role_id_=:roleId order by left_) p")
					.addScalar("auth_id_", Hibernate.LONG).setLong("roleId",
							roleId).list();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("得到权限列表失败");
		}
	}

	@Auth(name = "角色权限管理")
	public void insertAuthToRole(Long roleId, Long[] array) throws BOException {
		try {
			if (array == null || array.length == 0)
				return;
			Session session = sessionFactory.getCurrentSession();
			Query sql = session
					.createQuery("delete  from RoleAuthority a where a.role=:roleId");
			sql.setLong("roleId", roleId);
			sql.executeUpdate();
			for (Long authId : array) {
				Role role = (Role) session.load(Role.class, roleId);
				Authority authority = (Authority) session.load(Authority.class,
						authId);
				RoleAuthority ra = new RoleAuthority();
				ra.setRole(role);
				ra.setAuthority(authority);
				session.save(ra);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("得到权限列表失败");
		}
	}

	@Auth(name = "菜单维护管理")
	public Forward enterMenu() {
		return new Forward("com/yunmei/frame/view/menu.jsp");
	}

	@Auth(name = "用户角色管理")
	public Forward enterUserRole() {
		return new Forward("com/yunmei/frame/view/userRole.jsp");
	}

	public String findGobalMenus(Long parent) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			Iterator iter = session
					.createQuery(
							"from Authority menu where menu.parent=:parent and menu.type!='D' order by menu.left")
					.setLong("parent", parent).iterate();
			JSONArray array = new JSONArray();
			while (iter.hasNext()) {
				JSONObject json = $.objectToJSON(iter.next());
				if (json.has("left") && json.has("right")) {
					json.put("leaf",
							json.getInt("right") - json.getInt("left") == 1);
					if (json.getString("type").charAt(0) == Constraint.MEMU)
						json.put("icon", "images/menu.gif");
					else if (json.getString("type").charAt(0) == Constraint.OPER)
						json.put("icon", "images/oper.gif");
				} else {
					json.put("leaf", "true".equals(json.getString("leaf")));
				}
				array.put(json);
			}
			return array.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("查找全局菜单失败");
		}
	}

	public String findDataMenus(Long parent) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			Iterator iter = session
					.createQuery(
							"from Authority menu where menu.parent=:parent and (menu.type='D' or (menu.type='R' and exists(from Authority ss where ss.type='D' and ss.left>menu.left and ss.right<menu.right))) order by menu.left")
					.setLong("parent", parent).iterate();
			return $.list2AsyncTree(iter).toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("查找全局菜单失败");
		}
	}

	public void updateMenu(Long menuId, String text) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		Authority auth = (Authority) session.load(Authority.class, menuId);
		auth.setText(text);
	}

	public void deleteMenu(Long nodeId) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			Authority node = (Authority) session.load(Authority.class, nodeId);
			session.delete(node);
			Integer value = node.getRight() - node.getLeft() + 1;
			Query q = session
					.createQuery("update Authority n set n.left=n.left-?,n.right=n.right-? where  n.left>?");
			q.setInteger(0, value).setInteger(1, value).setInteger(2,
					node.getRight());
			q.executeUpdate();
			q = session
					.createQuery("update Authority n set n.right=n.right-? where n.right>? and n.left<?");
			q.setInteger(0, value).setInteger(1, node.getRight()).setInteger(2,
					node.getLeft());
			q.executeUpdate();
		} catch (Exception e) {
			throw new BOException("删除节点失败");
		}
	}

	@Auth(name = "菜单更新")
	public Long saveMenu(Authority auth) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		Authority parent = (Authority) session.load(Authority.class, auth
				.getParent());
		Query update = session
				.createQuery("update Authority n set n.left=n.left+?,n.right=n.right+? where n.right>? and n.left>?");
		update.setParameter(0, 2).setParameter(1, 2).setParameter(2,
				parent.getRight() - 1).setParameter(3, parent.getRight() - 1);
		update.executeUpdate();
		update = session
				.createQuery("update Authority n set n.right=n.right+? where  n.right>? and n.left<?");
		update.setParameter(0, 2).setParameter(1, parent.getRight() - 1)
				.setParameter(2, parent.getRight());
		update.executeUpdate();
		auth.setParent(parent.getId());
		auth.setLeft(parent.getRight());
		auth.setRight(parent.getRight() + 1);
		return (Long) session.save(auth);
	}

	public void moveMenu(Long sonId, Long parentId) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		Authority son = (Authority) session.load(Authority.class, sonId);
		Authority father = (Authority) session.load(Authority.class, parentId);
		Query q = null;
		if (father.getParent() == -1L) {
			q = session
					.createQuery("select ra.role.id from RoleAuthority ra where ra.authority=:sonId");
			q.setLong("sonId", sonId);
		} else {
			q = session
					.createQuery("select ra.role.id from RoleAuthority ra where ra.authority=:sonId or ra.authority=:parentId group by ra.role.id having count(*)>1");
			q.setLong("sonId", sonId);
			q.setLong("parentId", father.getParent());
		}
		List<Long> list = q.list();
		for (Long id : list) {
			RoleAuthority ra = new RoleAuthority();
			ra.setAuthority(father);
			ra.setRole((Role) session.load(Role.class, id));
			session.save(ra);
		}
		if (son.getRight() < father.getRight())
			moveMenuDown(son, father);
		else
			moveMenuUp(son, father);
	}

	private void moveMenuDown(Authority son, Authority father)
			throws BOException {
		Session session = sessionFactory.getCurrentSession();
		int value = son.getRight() - son.getLeft() + 1;
		int selfValue = father.getRight() - 1 - son.getRight();
		String updateBoth = "update Authority tree set tree.left=tree.left-:value,tree.right=tree.right-:value"
				+ " where tree.left>:sLeft and tree.right>:sRight and tree.right<:fRight";
		String updateLeft = "update Authority tree set tree.right=tree.right-:value"
				+ " where tree.left<:sLeft and tree.right>:sRight and tree.right<:fRight";
		String updateRight = "update Authority tree set tree.left=tree.left-:value "
				+ " where tree.left<=:fLeft and tree.right>=:fRight and tree.left>:sLeft";
		String updateSelf = "from Authority tree"
				+ " where tree.left>=:sLeft and tree.right<=:sRight";
		List<Authority> self = session.createQuery(updateSelf).setInteger(
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
		for (Authority auth : self) {
			auth.setLeft(auth.getLeft() + selfValue);
			auth.setRight(auth.getRight() + selfValue);
		}
		son.setParent(father.getId());
	}

	private void moveMenuUp(Authority son, Authority father) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		int value = son.getLeft() - son.getRight() - 1;
		int selfValue = father.getRight() - 1 - value - son.getRight();
		String updateLeft = "update Authority tree set tree.right=tree.right-:value"
				+ " where tree.left<=:fLeft and tree.right>=:fRight and tree.right<:sRight";

		String updateRight = "update Authority tree set tree.left=tree.left-:value "
				+ " where tree.left<:sLeft and tree.right>:sRight and tree.left>:fLeft";
		String updateSelf = "from Authority tree"
				+ " where tree.left>=:sLeft and tree.right<=:sRight";
		String updateBoth = "update Authority tree set tree.left=tree.left-:value,tree.right=tree.right-:value"
				+ " where tree.left>:fLeft and tree.right>:fRight and tree.right<:sLeft";
		List<Authority> self = session.createQuery(updateSelf).setInteger(
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
		for (Authority auth : self) {
			auth.setLeft(auth.getLeft() + selfValue);
			auth.setRight(auth.getRight() + selfValue);
		}
		son.setParent(father.getId());
	}

	public String findMenus(Long id) throws BOException {
		try {
			JSONArray array = new JSONArray();
			List<Authority> list = User.getUser().getAuths();
			for (Authority auth : list) {
				if (auth.getParent().longValue() == id
						&& (auth.getType() == Constraint.MEMU || auth.getType() == Constraint.ROOT)) {
					JSONObject json = new JSONObject();
					json.put("id", auth.getId());
					json.put("text", auth.getText());
					if (auth.getType() == Constraint.MEMU) {
						json.put("url", auth.getUrl());
						json.put("icon", "images/menu.gif");
						json.put("leaf", true);
					} else {
						json.put("leaf", false);
					}
					array.put(json);
				}
			}
			return array.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("加载用户菜单");
		}
	}

	public List<Map> findDataFields(Long id) throws BOException {
		Session session = sessionFactory.getCurrentSession();
		Authority auth = (Authority) session.load(Authority.class, id);
		String url = auth.getUrl();
		Object obj = SpringUtils.getBean(url.split("\\.")[0]);
		Class target = $.getTarget(obj);
		List<Map> list = new ArrayList<Map>();
		for (Method ms : target.getDeclaredMethods()) {
			if (ms.getName().equals(url.split("\\.")[1])) {
				Auth annotation = ms.getAnnotation(Auth.class);
				if (annotation != null) {
					String fields[] = annotation.fields().split(",");
					for (String field : fields) {
						Map map = new HashMap();
						String entity[] = field.split(":");
						map.put("field", entity[0]);
						map.put("value", entity[1]);
						map.put("order", annotation.order());
						list.add(map);
					}
				}
			}
		}
		return list;
	}

	@Auth(name = "角色更新")
	public List<Map> saveRoles(List<Role> insert, List<Role> update)
			throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			List<Map> result = new ArrayList<Map>();
			for (Role info : insert) {
				Long oldId = info.getId();
				Map map = new HashMap();
				map.put(oldId, session.save(info));
				result.add(map);
			}
			for (Role info : update) {
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

	@Auth(name = "用户角色更新")
	public void insertRole2User(Long[] roleIds, Long userId) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			for (Long roleId : roleIds) {
				UserRole ur = new UserRole();
				ur.setUser((User) session.load(User.class, userId));
				ur.setRole((Role) session.load(Role.class, roleId));
				session.save(ur);
			}
		} catch (HibernateException e) {
			if (log.isDebugEnabled())
				log.error("用户角色添加失败" + e);
			throw new BOException("用户角色添加失败");
		}
	}

	public Page findDirectNotRoles(Long userId, int start, int maxResult)
			throws BOException {
		try {
			Page page = new Page();
			Session session = sessionFactory.getCurrentSession();
			Query q = session
					.createQuery("select role from Role role where not exists (select role1 from UserRole ur inner join ur.role as role1 "
							+ "where ur.user=:userId and role=role1)");
			q.setLong("userId", userId);
			page.setCount(q.list().size());
			q.setFirstResult(start);
			q.setMaxResults(maxResult);
			page.setList(q.list());
			return page;
		} catch (HibernateException e) {
			if (log.isDebugEnabled())
				log.error("查询失败" + e);
			throw new BOException("查询失败");
		}
	}

	public List<DataFilter> findFiltersByRoleId(Long roleId) {
		Session session = sessionFactory.getCurrentSession();
		session
				.createSQLQuery("select  from DataFilter df,Authority au where df.roleId=:roleId and df.authId=au.id ");
		return null;
	}

	public void deleteDirectRoles(Long userId, Long[] roleId)
			throws BOException {
		Session session = sessionFactory.getCurrentSession();
		Query q = session
				.createQuery("delete from UserRole ur where ur.user=:userId and ur.role.id in(:roleId)");
		q.setLong("userId", userId);
		q.setParameterList("roleId", roleId);
		q.executeUpdate();
	}

	public Serializable insertFilter2Role(DataFilter df) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			Query q = session
					.createQuery("delete from DataFilter df where df.roleId=:roleId and df.authId=:authId");
			q.setLong("roleId", df.getRoleId());
			q.setLong("authId", df.getAuthId());
			RoleAuthority ra = new RoleAuthority();
			Authority a = (Authority) session.load(Authority.class, df
					.getAuthId());
			Role role = (Role) session.load(Role.class, df.getRoleId());
			ra.setAuthority(a);
			ra.setRole(role);
			session.save(ra);
			q.executeUpdate();
			session.flush();
			return session.save(df);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("角色添加数据权限失败");
		}
	}

	public List<DataFilter> findDataAuthsByRoleId(Long roleId)
			throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			return session.createQuery(
					"from DataFilter df where df.roleId=:roleId").setLong(
					"roleId", roleId).list();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("查询角色数据权限失败");
		}
	}

	@Auth(name = "过滤条件更新")
	public void deleteDataFromRole(Long id) throws BOException {
		try {
			Session session = sessionFactory.getCurrentSession();
			session.createQuery("delete from DataFilter df where df.id=:id")
					.setLong("id", id).executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("数据权限删除失败");
		}
	}

	public Forward logout() {
		User.setUser(null);
		return new Forward("index.jsp");
	}

	/**
	 */
	@Auth(name = "个人资料")
	public Forward enterInfo() {
		return new Forward("com/yunmei/frame/view/info.jsp");
	}

	public void deleteRole(Long roleId) throws BOException {
		Session session = this.sessionFactory.getCurrentSession();
		session.delete(session.load(Role.class, roleId));
	}

	public Forward changePost(Long userId, Long postId) throws BOException {
		loadAuths(userId, postId);
		return new Forward("com/yunmei/frame/view/main.jsp");
	}

	public List<Long> findUsersByPostId(Long postId) {
		Session session = this.sessionFactory.getCurrentSession();
		List<Long> list = (List<Long>) session
				.createQuery(
						"select ot.user.id from OrganTree ot where exists(select 1 from OrganTree ot2 where ot2.post.id=:postId and ot2.left<ot.left and ot2.right>ot.right)")
				.setParameter("postId", postId).list();
		return list;
	}

	public List<Long> findUsersByRoleId(Long roleId) {

		Session session = sessionFactory.getCurrentSession();
		List<Long> list = (List<Long>) session.createQuery(
				"select ur.user.id from UserRole ur where ur.role.id=:roleId")
				.setParameter("roleId", roleId).list();
		return list;
	}
}
