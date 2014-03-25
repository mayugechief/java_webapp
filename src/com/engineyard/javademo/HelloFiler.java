
package com.engineyard.javademo;

import java.nio.file.*;
import java.util.Date;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HelloFiler extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8447571599556573671L;

	private String checkPath(String path) {
		Path p5 = Paths.get(path);
		if (Files.isWritable(p5)) {
			return path + " is writable<p>";
		} else {
			return path + " is not writable<p>";			
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("<title>Java 7 on Engine Yard</title></head>");

		String userHome = System.getProperty("user.home");
		response.getWriter().println("user.home is " + userHome  + "<p>");
		response.getWriter().println(checkPath(userHome));

		userHome = System.getProperty("java.io.tmpdir");
		response.getWriter().println("java.io.tmpdir is " + userHome + "<p>");
		response.getWriter().println(checkPath(userHome));
	}

}
