package com.yunmei.frame.servlet.tablib;

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.DynamicAttributes;

import com.yunmei.frame.model.Authority;
import com.yunmei.frame.model.Post;
import com.yunmei.frame.model.User;

public class YunmeiTag extends BodyTagSupport implements DynamicAttributes {

	private String ids;
	private static String responseText = "";

	public String getIds() {
		return ids;
	}

	public void setIds(String ids) {
		this.ids = ids;
	}

	@Override
	public int doStartTag() throws JspException {
		JspWriter writer = pageContext.getOut();
		StringBuilder sb = null;
		if (responseText.length() == 0) {
			sb = new StringBuilder();
			sb
					.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n");
			sb.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"")
					.append("css/ext-all.css\"/>\n");
			sb.append("<script type=\"text/javascript\" src=\"").append(
					"script/ext/ext-base.js\"></script>\n");
			sb.append("<script type=\"text/javascript\" src=\"").append(
					"script/ext/ext-all.js\"></script>\n");
			sb.append("<script type=\"text/javascript\" src=\"").append(
			"script/ext/ux/QueryField.js\"></script>\n");
			sb.append("<script type=\"text/javascript\" src=\"").append(
					"script/ext/ext-lang-zh_CN.js\"></script>\n");
			sb.append("<script type=\"text/javascript\" src=\"").append(
					"script/yunmei.js\"></script>\n");
			sb.append("<script type=\"text/javascript\">");
			sb.append("$.serverURL=\"").append("login\";");
			sb.append("Ext.BLANK_IMAGE_URL='images/default/tree/s.gif'");
			sb.append("</script>\n");
			responseText = sb.toString();
		}
		sb = new StringBuilder();
		if ("header".equals(ids)) {
			User user = User.getUser();
			if (user != null) {
				sb.append("<script type=\"text/javascript\">");
				sb.append("var user = {id:");
				sb.append(user.getId()).append(",defaultPost:");
				sb.append(user.getDefaultPost());
				sb.append(",name:'").append(user.getName()).append("'");
				sb.append(",posts:[");
				if (user.getPosts().size() != 0) {
					for (Post node : user.getPosts()) {
						sb.append("{id:").append(node.getId());
						sb.append(",name:'").append(node.getName());
						sb.append("'},");
					}
				}
				if (sb.charAt(sb.length() - 1) == '[')
					sb.append("]");
				else
					sb.replace(sb.length() - 1, sb.length(), "]");
				List<Authority> list = user.getAuths();
				sb.append(",opers:[");
				for (Authority auth : list) {
					if (auth.getType() == 'O') {
						sb.append("{id:'").append(auth.getId());
						sb.append("',url:'").append(auth.getUrl());
						sb.append("'},");
					}
				}
				if (sb.charAt(sb.length() - 1) == '[')
					sb.append("]");
				else
					sb.replace(sb.length() - 1, sb.length(), "]");
				sb.append("};</script>");
			}
		}
		try {
			writer.println(responseText + sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return EVAL_PAGE;
	}

	public void setDynamicAttribute(String arg0, String arg1, Object arg2)
			throws JspException {
	}
}
