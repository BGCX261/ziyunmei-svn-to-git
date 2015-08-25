package com.yunmei.demo.bo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.yunmei.demo.dao.OrderDAO;
import com.yunmei.demo.model.Order;
import com.yunmei.frame.bo.IWorkflowBO;
import com.yunmei.frame.exception.BOException;
import com.yunmei.frame.model.Opinion;
import com.yunmei.frame.model.User;
import com.yunmei.frame.servlet.Page;
import com.yunmei.frame.utils.Auth;
import com.yunmei.frame.utils.Forward;

/**
 * 构件名称 凡是方法前加注释的全部被系统识别为权限的控制点 根据方法的返回值及注释属性分别会被系统是别为 操作权限，数据权限，及菜单权限
 */
@Auth(name = "订单管理")
public class OrderBOImpl implements IOrderBO {
	private OrderDAO orderDAO;
	private IWorkflowBO workflowBO;
	private Map<String, String> views = new HashMap<String, String>();

	/**
	 * 方法返回值为Forward且有Auth注释的会被系统识别为菜单权限
	 */
	@Auth(name = "订单菜单")
	public Forward selectOrder() {
		return new Forward(views.get("enter"));
	}

	public Forward selectOrder(String taskId) throws BOException {
		String businessId = workflowBO.getBusinessIdByTaskId(taskId);
		return new Forward(views.get("enter"), "id", businessId);
	}

	/**
	 * 方法返回值为非Forward且有Auth注释(注释中没有fields属性)的 会被系统识别为操作权限
	 */
	@Auth(name = "订单删除")
	public void delete(Long id) throws BOException {
		try {
			orderDAO.delete(id);
		} catch (Exception e) {
			throw new BOException("订单删除失败");
		}
	}

	/**
	 * 方法返回值为非Forward且有Auth注释(注释中有fields属性)的 会被系统识别为数据权限
	 */
	@Auth(name = "订单查询", fields = "monkey_:金额,name_:订单名,state_:状态", order = 12)
	public Page find(Order order, int start, int max) throws BOException {
		try {
			return orderDAO.findByLike(order, start, max);
		} catch (Exception e) {
			throw new BOException("订单查询失败");
		}
	}

	@Auth(name = "订单更新")
	public Long save(Order order) throws BOException {
		try {
			this.orderDAO.saveOrUpate(order);
			return order.getId();
		} catch (Exception e) {
			throw new BOException("更新失败");
		}
	}

	@Auth(name = "订单提交")
	public String submit(Order order, String processDefinitionKey)
			throws BOException {
		try {
			orderDAO.saveOrUpate(order);
			Map map = new HashMap();
			map.put("monkey", order.getMonkey());
			if ("新建".equals(order.getState())) {
				workflowBO.startNext(User.getUser().getId().toString(),
						processDefinitionKey, order.getId().toString(), map);
			} else {
				workflowBO.nextByBusinessId(User.getUser().getId().toString(),
						order.getId().toString(), map);
			}
			if (order.getMonkey() <= 100)
				order.setState("财务签字");
			else
				order.setState("审批中");
			return order.getState();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("提交失败");
		}
	}

	@Auth(name = "财务签字")
	public String signed(Order order) throws BOException {
		order.setState("完成");
		orderDAO.saveOrUpate(order);
		workflowBO.nextByBusinessId(User.getUser().getId().toString(), order
				.getId().toString());
		return order.getState();
	}

	@Auth(name = "订单审批")
	public String audit(Opinion opinion) throws BOException {
		try {
			Order order = this.orderDAO.get(Long.parseLong(opinion
					.getBusinessId()));
			if (opinion.getResult().equals("不合格")) {
				order.setState("驳回");
			} else
				order.setState("财务签字");
			User user = User.getUser();
			opinion.setUserId(user.getId().toString());
			opinion.setUserName(user.getName());
			opinion.setDate(new Date());
			workflowBO.auditByBusinessId(opinion);
			return order.getState();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BOException("审批失败");
		}
	}

	public Order get(Long id) throws BOException {
		try {
			return orderDAO.get(id);
		} catch (Exception e) {
			throw new BOException("获取信息失败");
		}
	}

	public void setViews(Map views) {
		this.views = views;
	}

	public void setOrderDAO(OrderDAO orderDAO) {
		this.orderDAO = orderDAO;
	}

	public void setWorkflowBO(IWorkflowBO workflowBO) {
		this.workflowBO = workflowBO;
	}
}
