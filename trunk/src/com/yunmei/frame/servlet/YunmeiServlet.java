package com.yunmei.frame.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.JobDetail;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerUtils;
import org.quartz.ee.servlet.QuartzInitializerListener;

import com.yunmei.frame.bo.ITimerBO;
import com.yunmei.frame.model.Authority;
import com.yunmei.frame.model.DataFilter;
import com.yunmei.frame.model.User;
import com.yunmei.frame.servlet.reflect.JSONRPCBridge;
import com.yunmei.frame.servlet.reflect.JSONRPCResult;
import com.yunmei.frame.utils.$;
import com.yunmei.frame.utils.Constraint;
import com.yunmei.frame.utils.SpringUtils;
import com.yunmei.frame.utils.TimerExecute;

public class YunmeiServlet extends HttpServlet {

	private final static long serialVersionUID = 2;
	private final static Logger log = Logger.getLogger(YunmeiServlet.class);
	private final static int buf_size = 4096;
	private static SchedulerFactory schedulerFactory;
	private static int GZIP_THRESHOLD = 200;
	private static SessionFactory sessionFactory;
	private static List<String> allowedURL = new ArrayList<String>();
	static {
		allowedURL.add("sysAuthService.login");
	}

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		TimeZone.setDefault(TimeZone.getTimeZone("PRC"));
		if (log.isDebugEnabled()) {
			log.debug("设置时区为 PRC,即东八区");
		}
		schedulerFactory = (SchedulerFactory) this.getServletContext()
				.getAttribute(QuartzInitializerListener.QUARTZ_FACTORY_KEY);
		if (schedulerFactory != null) {
			$.setSchedulerFactory(schedulerFactory);
			// 每天0点执行
			sinoTask();
		} else if (log.isDebugEnabled())
			log.error("定时器初始化失败");

		String beans[] = SpringUtils.getBeans();
		for (String beanId : beans) {
			if (beanId.endsWith("Service")) {
				JSONRPCBridge.getGlobalBridge().registerObject(beanId,
						SpringUtils.getBean(beanId));
				if (log.isDebugEnabled())
					log.info("发布的服务:" + beanId);
			}
		}
		sessionFactory = (SessionFactory) SpringUtils.getBean("sessionFactory");
		// JSON压缩处理 －1 不处理,0 全压缩 0>0选择压缩
		String zipSize = config.getInitParameter("zipSize");
		if (zipSize != null && zipSize.length() > 0) {
			try {
				YunmeiServlet.GZIP_THRESHOLD = Integer.parseInt(zipSize);
			} catch (NumberFormatException n) {
				YunmeiServlet.GZIP_THRESHOLD = -1;
			}
		}
		log.debug("压缩参数为: " + YunmeiServlet.GZIP_THRESHOLD
				+ "－1 不处理,0 全压缩  >0选择压缩");
	}

	public void service(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String charset = request.getCharacterEncoding();
		if (charset == null) {
			charset = "UTF-8";
		}
		Map requestInfo = new HashMap();
		$.clear();
		String receiveString = request.getParameter("!params");
		String requestType = request.getParameter("!type");
		$.setRequest(request);
		$.setSession(request.getSession());
		$.setRequestInfo(requestInfo);
		if (schedulerFactory == null)
			schedulerFactory = (SchedulerFactory) this.getServletContext()
					.getAttribute(QuartzInitializerListener.QUARTZ_FACTORY_KEY);
		$.setSchedulerFactory(schedulerFactory);
		$.setRequestType(requestType == null ? Constraint.REQUEST_NORMAL
				: requestType.charAt(0));
		JSONRPCBridge json_bridge = findBridge(request);

		BufferedReader in = new BufferedReader(new InputStreamReader(request
				.getInputStream(), charset));
		if (receiveString == null || receiveString.length() == 0) {
			CharArrayWriter data = new CharArrayWriter();
			char buf[] = new char[buf_size];
			int ret;
			while ((ret = in.read(buf, 0, buf_size)) != -1) {
				data.write(buf, 0, ret);
			}
			receiveString = data.toString();
		}
		receiveString = java.net.URLDecoder.decode(receiveString, charset);
		JSONObject json_req = null;
		JSONRPCResult json_res = null;
		String method = null;
		try {
			json_req = new JSONObject(receiveString);
			method = json_req.getString("method");
			String jsonParams = request.getParameter("!jsonparams");
			String query = request.getParameter("query");
			if (jsonParams != null && jsonParams.length() > 0) {
				if (query == null) {
					json_req.put("params", new JSONArray(jsonParams));
				}
			}
			requestInfo.put("method", method);
			requestInfo.put("args", json_req.toString());
			requestInfo.put("ip", request.getRemoteHost());
			if (log.isDebugEnabled()) {
				log.debug("请求参数:" + json_req.toString());
				log.debug("请求类型:" + $.getRequestType());
			}
		} catch (Exception e) {
			json_res = new JSONRPCResult(JSONRPCResult.CODE_ERR_UNMARSHALL, -1,
					"解析参数失败");
		}
		if (json_res != null) {
			processError(request, response, json_res);
			return;
		}
		if (!verifyAuth(method)) {
			processError(request, response, new JSONRPCResult(
					JSONRPCResult.CODE_ERR_NOAUTH, -1, "没有相应权限"));
			return;
		}
		json_res = json_bridge.call(new Object[] { request, response },
				json_req);
		if (json_res.getErrorCode() != JSONRPCResult.CODE_SUCCESS) {
			processError(request, response, json_res);
			return;
		}
		if ($.getRequestType() == Constraint.REQUEST_FORWARD) {
			try {
				JSONObject retval = ((JSONObject) json_res.getResult());
				String url = retval.getString("url");
				if (retval.has("key")) {
					String key = retval.getString("key");
					String val = retval.getString("value");
					request.setAttribute(key, val);
				}
				getServletContext()
						.getRequestDispatcher(
								(url.startsWith(Constraint.FORWORDPATH) ? Constraint.CONTEXTPATH
										: Constraint.CONTEXTROOT)
										+ url).forward(request, response);
			} catch (Exception e) {
				if (log.isDebugEnabled())
					log.error("异常信息" + e);
				processError(request, response,
						new JSONRPCResult(JSONRPCResult.CODE_ERR_NOAUTH,
								json_res.getId(), "跳转异常"));
			}
			return;
		}
		String sendString = null;
		sendString = json_res.getResult().toString();
		if (log.isDebugEnabled())
			log.debug("返回字符串:" + sendString);
		byte[] bout = sendString.getBytes("UTF-8");
		if (YunmeiServlet.GZIP_THRESHOLD != -1) {
			if (acceptsGzip(request)) {
				if (bout.length > YunmeiServlet.GZIP_THRESHOLD) {
					byte[] gzippedOut = gzip(bout);
					log.debug("gzipping! original size =  " + bout.length
							+ "  gzipped size = " + gzippedOut.length);
					if (bout.length <= gzippedOut.length) {
						log
								.warn("gzipping resulted in a larger output size! original size = "
										+ bout.length
										+ " gzipped size = "
										+ gzippedOut.length);
					} else {
						bout = gzippedOut;
						response.addHeader("Content-Encoding", "gzip");
					}
				} else {
					log.debug("not gzipping because size is " + bout.length
							+ " (less than the GZIP_THRESHOLD of "
							+ YunmeiServlet.GZIP_THRESHOLD + " bytes)");
				}
			} else {
				log
						.debug("not gzipping because user agent doesn't accept gzip encoding...");
			}
		}
		response.setContentType("application/json;charset=utf-8");
		OutputStream out = response.getOutputStream();
		response.setIntHeader("Content-Length", bout.length);
		out.write(bout);
		out.flush();
		out.close();

	}

	protected JSONRPCBridge findBridge(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		JSONRPCBridge json_bridge = null;
		if (session != null) {
			json_bridge = (JSONRPCBridge) session.getAttribute("JSONRPCBridge");
		}
		if (json_bridge == null) {
			json_bridge = JSONRPCBridge.getGlobalBridge();
		}
		return json_bridge;
	}

	private boolean acceptsGzip(HttpServletRequest request) {
		// can browser accept gzip encoding?
		String ae = request.getHeader("accept-encoding");
		return ae != null && ae.indexOf("gzip") != -1;
	}

	private byte[] gzip(byte[] in) {
		if (in != null && in.length > 0) {
			long tstart = System.currentTimeMillis();
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			try {
				GZIPOutputStream gout = new GZIPOutputStream(bout);
				gout.write(in);
				gout.flush();
				gout.close();
				if (log.isDebugEnabled()) {
					log.debug("gzipping took "
							+ (System.currentTimeMillis() - tstart) + " msec");
				}
				return bout.toByteArray();
			} catch (IOException io) {
				log.error("io exception gzipping byte array", io);
			}
		}
		return new byte[0];
	}

	private void processError(HttpServletRequest request,
			HttpServletResponse response, JSONRPCResult result) {
		if (log.isDebugEnabled()) {
			log.error("错误信息:" + result);
		}
		try {
			if (Constraint.REQUEST_FORWARD == $.getRequestType()) {
				request.setAttribute(Constraint.ERROR, result.getResult()
						.toString());
				getServletContext().getRequestDispatcher("/error.jsp").forward(
						request, response);
			} else {
				byte[] bout = result.toString().getBytes("UTF-8");
				response.setContentType("application/json;charset=utf-8");
				OutputStream out = response.getOutputStream();
				response.setIntHeader("Content-Length", bout.length);
				out.write(bout);
				out.flush();
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean verifyAuth(String url) {
		boolean pass = true;
		Session session = sessionFactory.openSession();
		Query q = session.createQuery("from Authority a where a.url=:url")
				.setString("url", url);
		q.setCacheable(true);
		Authority auth = (Authority) q.uniqueResult();
		if (auth == null) {
			if (log.isDebugEnabled())
				log.debug("此地址不再权限控制范围内：" + url);
		} else {
			if (auth.getType() == 'D') {
				SQLQuery sql = session
						.createSQLQuery("select data.* FROM v_sys_user_role ur,sys_data_filter data where data.role_id_=ur.role_id_ and data.auth_id_=:authId and (ur.user_id_=:userId or post_id_=:postId)");
				sql.addEntity(DataFilter.class);
				sql.setLong("authId", auth.getId());
				sql.setLong("userId", User.getUser().getId());
				sql.setLong("postId",
						User.getUser().getDefaultPost() == null ? -1 : User
								.getUser().getDefaultPost());
				List<DataFilter> list = sql.list();
				if (list.size() > 0) {
					DataFilter df = list.get(0);
					if (log.isDebugEnabled()) {
						log.debug("过滤信息：" + df.getFilterSQL() + ","
								+ df.getOrder());
					}
					if (list.size() > 1) {
						if (log.isDebugEnabled())
							log.error("该用户过滤条件多于一个,请重新配置");
					}
					$.setAuthData(df.getFilterSQL(), df.getOrder());
				}
			} else {
				pass = User.getUser().getAuths().contains(auth);
			}
		}
		session.close();
		return pass;
	}

	private void sinoTask() {
		try {
			JobDetail job = new JobDetail("yunmeiTask", null,
					TimerExecute.class);
			job.getJobDataMap().put("method", "sysTimerService.loadTimerTask");
			job.getJobDataMap().put("params", "[]");
			Trigger trigger = TriggerUtils.makeDailyTrigger(0, 0);
			trigger.setName("yunmeiTrigger");
			ITimerBO timer = (ITimerBO) SpringUtils.getBean("sysTimerService");
			timer.loadTimerTask();
			SchedulerFactory sf = (SchedulerFactory) getServletContext()
					.getAttribute(QuartzInitializerListener.QUARTZ_FACTORY_KEY);
			sf.getScheduler().scheduleJob(job, trigger);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}