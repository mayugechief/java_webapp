package com.engineyard.javademo;

import java.util.Date;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HelloServlet extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8447571599556573671L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("<title>Java on Engine Yard</title></head>");
		response.getWriter().println("<h1>I'm running Java on "
			+ getServletContext().getServerInfo() 
			+ " on Engine Yard, at " 
			+ new Date().toString() + "</h1>");
		response.getWriter().println(
			"<p><img src=\"http://s3.amazonaws.com/engineyard.com/media_files/files/49/original/ey-java.jpg\" />");
		response.getWriter().println("<h2>Hello Servlet</h2>");
		response.getWriter().println("session=".concat(request.getSession(true).getId()));
	}

}
