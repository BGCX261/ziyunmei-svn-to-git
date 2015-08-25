package com.yunmei.frame.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.yunmei.frame.utils.Constraint;

public class ResourceServlet extends HttpServlet {
	/**
	 * 
	 */
	private final static Logger log = Logger.getLogger(ResourceServlet.class);

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.doPost(req, resp);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String uri = req.getRequestURI();
		String path = uri.substring(req.getContextPath().length() + 1);
		/*
		 * if (path.endsWith(".jsp") || path.endsWith(".html") ||
		 * path.endsWith(".htm")) {
		 * getServletContext().getRequestDispatcher("/error.jsp").forward(req,
		 * resp); } else {
		 */
		StringBuffer dispatcherPath = new StringBuffer();
		dispatcherPath.append(Constraint.CONTEXTPATH).append(path);
		getServletContext().getRequestDispatcher(dispatcherPath.toString())
				.forward(req, resp);
		// }
	}
}
