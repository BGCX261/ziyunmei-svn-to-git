package com.yunmei.frame.servlet;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.RepositoryService;
import org.jbpm.api.model.ActivityCoordinates;

import com.yunmei.frame.bo.IWorkflowBO;
import com.yunmei.frame.utils.SpringUtils;

/**
 * Servlet implementation class ImageServlet
 */
public class JbpmServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	RepositoryService repositoryService = null;
	ExecutionService executionService = null;
	Integer memorySize = 1024 * 1024;
	String tempDirectory = "/home/ghost/";
	Integer maxSize = 1024 * 1024 * 10;

	public void init(ServletConfig config) throws ServletException {
		ProcessEngine processEngine = (ProcessEngine) SpringUtils
				.getBean("processEngine");
		repositoryService = processEngine.getRepositoryService();
		executionService = processEngine.getExecutionService();
	}

	public JbpmServlet() {

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	private void processImage(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String processDefinitionId = null;
		String executionId = request.getParameter("executionId");
		if (executionId != null) {
			processDefinitionId = executionService.findExecutionById(
					executionId).getProcessDefinitionId();
		} else
			processDefinitionId = request.getParameter("processDefinitionId");
		String activityName = request.getParameter("activityName");
		ProcessDefinition processDefinition = repositoryService
				.createProcessDefinitionQuery().processDefinitionId(
						processDefinitionId).uniqueResult();
		InputStream io = repositoryService.getResourceAsStream(
				processDefinition.getDeploymentId(), processDefinition
						.getImageResourceName());
		BufferedImage image = ImageIO.read(io);
		if (activityName != null) {
			activityName=new String(activityName.getBytes("iso8859-1"));
			ActivityCoordinates ac = repositoryService.getActivityCoordinates(
					processDefinitionId, activityName);
			Graphics g = image.getGraphics();
			g.setColor(Color.RED);
			g.drawRect(ac.getX()+4, ac.getY()+4, ac.getWidth()-8, ac.getHeight()-8);
			g.dispose();
		}
		ImageIO.write(image, "png", response.getOutputStream());
		response.getOutputStream().close();
	}

	private void processFileUpload(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			FileUploadException {
		IWorkflowBO workflowBO = (IWorkflowBO) SpringUtils
				.getBean("sysWorkflowService");
		DiskFileItemFactory factory = new DiskFileItemFactory(memorySize,
				new File(tempDirectory));
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setSizeMax(maxSize);
		List<FileItem> items = upload.parseRequest(request);
		InputStream input = null;
		String remark = null;
		for (FileItem item : items) {
			if (item.isFormField()) {
				remark = item.getString();
			} else {
				input = item.getInputStream();
			}
		}
		workflowBO.deploy(input, remark);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (isMultipart)
			try {
				processFileUpload(request, response);
			} catch (FileUploadException e) {
				e.printStackTrace();
			}
		else
			processImage(request, response);
	}
}
