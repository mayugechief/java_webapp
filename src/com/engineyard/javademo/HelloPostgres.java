package com.engineyard.javademo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

public class HelloPostgres extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3349334178242798897L;
	private Context ctx = null;
	private DataSource ds = null;
	private String message = null;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		try {
			initializeDB();
			getMessage("jndi");
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			message = e.toString();
		}

		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().println("<title>Java on Engine Yard</title></head>");
		response.getWriter().println("<h1>I'm running Java on Engine Yard</h1>");
		response.getWriter().println(
			"<p><img src=\"http://s3.amazonaws.com/engineyard.com/media_files/files/49/original/ey-java.jpg\" />");
		response.getWriter().println("<h2>" + message );
		response.getWriter().println(
				"session=".concat(request.getSession(true).getId()) + "</h2>");
	}

	private void initializeDB() throws NamingException {
		ctx = new InitialContext();
		ds = (DataSource) ctx.lookup("java:comp/env/jdbc/EYPostgresql");
	}

	private void getMessage(String string) {
		Connection con = null;
		PreparedStatement statement = null;
		String query = "select message from javademo where id = ?";
		ResultSet resultSet = null;
		try {
			con = ds.getConnection();
			statement = con.prepareStatement(query);
			statement.setString(1, string);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				message = resultSet.getString(1);
			}

		} catch (SQLException e) {
			message = "Query failed: " + e;
		} finally {
			if (con != null)
				try {
					con.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					// I DON'T CARE
				}
		}
	}

}
