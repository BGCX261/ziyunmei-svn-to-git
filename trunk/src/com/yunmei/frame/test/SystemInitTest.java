package com.yunmei.frame.test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import junit.framework.TestCase;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import com.yunmei.frame.bo.IAuthorityBO;
import com.yunmei.frame.bo.IOrganBO;
import com.yunmei.frame.exception.BOException;
import com.yunmei.frame.model.Authority;
import com.yunmei.frame.model.Dict;
import com.yunmei.frame.model.DictInfo;
import com.yunmei.frame.model.Role;
import com.yunmei.frame.model.RoleAuthority;
import com.yunmei.frame.model.User;
import com.yunmei.frame.model.UserRole;
import com.yunmei.frame.utils.MD5Tools;

public class SystemInitTest extends TestCase {

	ApplicationContext app;
	SessionFactory sessionFactory;
	IOrganBO organBO;
	IAuthorityBO authBO;

	public void setUp() throws Exception {
		app = new FileSystemXmlApplicationContext(
				"web/WEB-INF/system-config-mysql.xml");
		organBO = (IOrganBO) app.getBean("sysOrganService");
		authBO = (IAuthorityBO) app.getBean("sysAuthService");
		sessionFactory = (SessionFactory) app.getBean("sessionFactory");
	}

	public void test创建数据库() {
		Connection conn = sessionFactory.openSession().connection();
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("CREATE OR REPLACE VIEW v_sys_user_role AS "
							+ "SELECT  role_.name_, role_.remark_, null as user_id_, post_.id_ AS post_id_, role_.id_ AS role_id_ FROM sys_post_role post_role JOIN sys_post post_ ON post_role.post_id_ = post_.id_ LEFT JOIN sys_role role_ ON post_role.role_id_ = role_.id_ "
							+ "UNION ALL SELECT role_.name_, role_.remark_,  user_.id_ AS user_id_,null as post_id_, role_.id_ AS role_id_ FROM sys_user_role user_role JOIN sys_user user_ ON user_.id_ = user_role.user_id_ LEFT JOIN sys_role role_ ON role_.id_ = user_role.role_id_");
			stmt.execute(" CREATE OR REPLACE VIEW v_sys_role_auth AS SELECT  auth_.text_, auth_.parent_, auth_.left_, auth_.right_, auth_.type_, auth_.url_, auth_.remark_, role_.id_ AS role_id_, auth_.id_ AS auth_id_ FROM sys_role_auth role_auth_ JOIN sys_role role_ ON role_.id_ = role_auth_.role_id_ LEFT JOIN sys_authority auth_ ON auth_.id_ = role_auth_.auth_id_");
			stmt.execute("CREATE OR REPLACE VIEW v_sys_user_auth AS SELECT ra.auth_id_ as auth_id_,ra.text_, ra.parent_, ra.left_, ra.right_, ra.type_, ra.url_, ra.remark_, ur.user_id_ as user_id_ ,ur.post_id_ as post_id_ FROM v_sys_role_auth ra JOIN v_sys_user_role ur ON ra.role_id_ = ur.role_id_");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void test初始化基础数据() {
		Session session = sessionFactory.openSession();
		session.beginTransaction().begin();
		Connection conn = session.connection();
		String insertSQLA = "INSERT INTO sys_authority(id_, text_, left_, right_, type_)VALUES (?, ?, ?, ?, ?)";
		String insertSQLO = "INSERT INTO sys_organ_tree(id_, text_, left_, right_, type_) VALUES (?, ?, ?, ?, ?)";
		try {
			// 权限数据
			PreparedStatement pt = conn.prepareStatement(insertSQLA);
			pt.setLong(1, -1);
			pt.setString(2, "权限根");
			pt.setInt(3, 1);
			pt.setInt(4, 2);
			pt.setString(5, "S");
			pt.executeUpdate();

			PreparedStatement pt2 = conn.prepareStatement(insertSQLO);
			pt2.setLong(1, -1);
			pt2.setString(2, "所有");
			pt2.setInt(3, 1);
			pt2.setInt(4, 2);
			pt2.setString(5, "S");
			pt2.executeUpdate();

			// 组织结构数据
			User user = new User();
			user.setLogin("admin");
			user.setPassword(MD5Tools.encode("password"));
			user.setName("超级管理员");
			Role r3 = new Role();
			r3.setName("超级管理员");
			UserRole ur = new UserRole();
			ur.setUser(user);
			ur.setRole(r3);
			session.save(ur);
			
			//-----------------------------------------
			Dict dict = new Dict();
			dict.setId("success");
			dict.setName("成功");
			dict.setDesc("方法的成功与失败");
			DictInfo di = new DictInfo();
			di.setName("成功");
			di.setValue("S");
			session.save(di);
			dict.getDictInfos().add(di);
			di = new DictInfo();
			di.setName("失败");
			di.setValue("F");
			session.save(di);
			dict.getDictInfos().add(di);
			session.save(dict);
			// 性别
			dict = new Dict();
			dict.setId("sex");
			dict.setName("性别");
			dict.setDesc("不知何用");
			di = new DictInfo();
			di.setName("男");
			di.setValue("M");
			session.save(di);
			dict.getDictInfos().add(di);
			di = new DictInfo();
			di.setName("女");
			di.setValue("F");
			session.save(di);
			dict.getDictInfos().add(di);
			di = new DictInfo();
			di.setName("保密");
			di.setValue("Q");
			session.save(di);
			dict.getDictInfos().add(di);
			session.save(dict);
			// 机构类型
			dict = new Dict();
			dict.setId("organ_type");
			dict.setName("机构类型");
			dict.setDesc("不知何用");
			di = new DictInfo();
			di.setName("办局领导");
			di.setValue("B");
			session.save(di);
			dict.getDictInfos().add(di);
			di = new DictInfo();
			di.setName("中心站院");
			di.setValue("Z");
			session.save(di);
			dict.getDictInfos().add(di);
			di = new DictInfo();
			di.setName("区县机构");
			di.setValue("Q");
			session.save(di);
			dict.getDictInfos().add(di);
			session.save(dict);
			// 任务状态
			dict = new Dict();
			dict.setId("task_state");
			dict.setName("任务状态");
			dict.setDesc("不知何用");

			di = new DictInfo();
			di.setName("不启动");
			di.setValue("0");
			session.save(di);
			dict.getDictInfos().add(di);

			di = new DictInfo();
			di.setName("启动");
			di.setValue("1");
			session.save(di);
			dict.getDictInfos().add(di);
			session.save(dict);
			// 任务类型
			dict = new Dict();
			dict.setId("task_type");
			dict.setName("任务类型");
			dict.setDesc("不知何用");
			di = new DictInfo();
			di.setName("一次性任务");
			di.setValue("0");
			session.save(di);
			dict.getDictInfos().add(di);
			di = new DictInfo();
			di.setName("每天");
			di.setValue("1");
			session.save(di);
			dict.getDictInfos().add(di);
			di = new DictInfo();
			di.setName("每周");
			di.setValue("2");
			session.save(di);
			dict.getDictInfos().add(di);
			di = new DictInfo();
			di.setName("每月");
			di.setValue("3");
			dict.getDictInfos().add(di);
			session.save(di);
			session.save(dict);
			// 月修正值
			dict = new Dict();
			dict.setId("task_fixed_month");
			dict.setName("任务月修正值");
			dict.setDesc("不知何用");
			for (int i = -15; i < 28; i++) {
				if (i == 0)
					continue;
				di = new DictInfo();
				di.setName("第" + i + "天");
				di.setValue(String.valueOf(i));
				session.save(di);
				dict.getDictInfos().add(di);
			}
			session.save(dict);
			// 月修正值
			dict = new Dict();
			dict.setId("task_fixed_week");
			dict.setName("任务周修正值");
			dict.setDesc("不知何用");
			for (int i = 1; i < 8; i++) {
				di = new DictInfo();
				di.setName("周" + i);
				di.setValue(String.valueOf(i));
				session.save(di);
				dict.getDictInfos().add(di);
			}
			session.save(dict);

			dict = new Dict();
			dict.setId("task_execute_type");
			dict.setName("任务执行方式");
			dict.setDesc("不知何用");
			di = new DictInfo();
			di.setName("固定时间");
			di.setValue("0");
			session.save(di);
			dict.getDictInfos().add(di);
			di = new DictInfo();
			di.setName("循环执行");
			di.setValue("1");
			session.save(di);
			dict.getDictInfos().add(di);
			session.save(dict);
			session.beginTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void test初始化权限(){
		try {
			authBO.reload();
		} catch (BOException e) {
			e.printStackTrace();
		}
	}

	public void test设置超管权限() {
		Session session = sessionFactory.openSession();
		session.beginTransaction().begin();
		Role admin = (Role) session.createQuery(
				"from Role u where u.name='超级管理员'").list().get(0);
		Query q = session
				.createQuery("delete from RoleAuthority ra where ra.role.id=:roleId");
		q.setParameter("roleId", admin.getId());
		q.executeUpdate();
		session.flush();
		// ---------------超级管理员赋权限-------------------------------------
		List<Authority> list = session.createQuery(
				"from Authority u where u.parent is not null").list();
		for (Authority auth : list) {
			RoleAuthority ra = new RoleAuthority();
			ra.setAuthority(auth);
			ra.setRole(admin);
			session.save(ra);
		}
		session.beginTransaction().commit();
	}
}