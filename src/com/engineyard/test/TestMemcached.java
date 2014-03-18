package com.engineyard.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Console;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;
import javax.servlet.http.*;
import javax.servlet.ServletException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;

public class TestMemcached extends HttpServlet {

  /**
   *
   */
  private static final long serialVersionUID = 8447571599556573671L;

  private static MemcachedClient mc;
  private Context ctx = null;
  private String eymchosts = null;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    int digit = (new Random()).nextInt(30);

    Result r = test_memcached(digit);
    StringBuilder sb = new StringBuilder();

    // static html...
    sb.append(
        "<html>\n" +
          "<head>\n" +
            "<title>Simple MemCachier Test</title>\n" +
            "<style type='text/css'>\n" +
              "html, body { height: 100%; }\n " +
              "#wrap { min-height: 100%; height: auto !important; height: 100%; margin: 0 auto -60px; }\n" +
              "#push, #footer { height: 60px; }\n" +
              "#footer { background-color: #f5f5f5; }\n" +
              "@media (max-width: 767px) { #footer { margin-left: -20px; margin-right: -20px; padding-left: 20px; padding-right: 20px; }}\n" +
              ".container { width: auto; max-width: 680px; }\n" +
              ".container .credit { margin: 20px 0; height: 1px; }\n" +
            "</style>" +
            "<link href='//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/" +
                       "css/bootstrap-combined.min.css' rel='stylesheet'>\n" +
            "<link rel='shortcut icon' href='https://www.memcachier.com/wp-content/uploads/2013/06/favicon.ico'>\n" +
          "</head>\n" +
          "<body>\n" +
            "<div id='wrap'><div class='container'>\n" +
            "<div class='page-header'><h1>Simple MemCachier Test</h1></div>\n" +
             "<p class='lead'>This script picks a random digit and attempts to cache it in Memcached," +
             "then read it back out.</p>\n");

    // actual cache / result values...
    sb.append("<p class='lead'>Digit: <span class='text-info'>" + digit + "</span></p>\n");
    sb.append("<p class='lead'>Cache: <span class='text-info'>" + r.val + "</span></p>\n");
    sb.append("<p class='lead'>Awesome? <span class='text-info'>" + r.result + "</span></p>\n");
    sb.append("<p class='lead'>Memcached Servers: <span class='text-info'>" + eymchosts + "</span></p>\n");

    // static html again...
    sb.append("<script src='//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/js/bootstrap.min.js'></script>");
    sb.append("</body></html>");

    response.getWriter().print(sb.toString());
  }

  public void init() {
    createMemCacheClient();
  }

  private static class Result {
    public int val;
    public String result;

    public Result(int val, String result) {
      this.val = val;
      this.result = result;
    }
  }

  private void getServersFromContext() throws NamingException {
      ctx = new InitialContext();
      eymchosts = (String)ctx.lookup("java:comp/env/EYMCHosts");
  }

  private void createMemCacheClient() {

    try {
      getServersFromContext();
      ConnectionFactory c;
      c = new ConnectionFactoryBuilder().setProtocol(
                  ConnectionFactoryBuilder.Protocol.BINARY).build();
      mc = new MemcachedClient(c,
          AddrUtil.getAddresses(eymchosts));
    } catch (Exception ex) {
      System.err.println(
        "Couldn't create a connection, bailing out:\nIOException "
        + ex.getMessage());
    }
  }

  private static Result test_memcached(int n) {
    try {
      mc.set("" + n, 3600, n);
      Object inCache = mc.get("" + n);
      if (inCache == null) {
        return new Result(0, "Memcached didn't cache for us.");
      } else {
        return new Result((Integer) inCache, "I just used Memcached and it was AWESOME");
      }
    } catch (Exception e) {
      // if any exception we simply shutdown the existing one an reconnect.
      // XXX: This is a very hacky way of dealing with the problem, thought
      // needs to be put in to your application to handle failures gracefully.
      if (mc != null)
        mc.shutdown();

      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      return new Result(0, "It looks like the Memcached Client probably bailed: " + e + sw.toString());
    }
  }

}

