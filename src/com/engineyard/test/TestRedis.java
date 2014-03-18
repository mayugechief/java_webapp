package com.engineyard.test;

import java.io.IOException;
import java.net.URI;
import java.util.Random;

import javax.servlet.http.*;
import javax.servlet.ServletException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;


public class TestRedis extends HttpServlet {

	private static final long serialVersionUID = 1L;

  private static JedisPool pool;
  private Context ctx = null;
  private String eyredishost = null;

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    int digit = (new Random()).nextInt(30);

    Result r = testRedis(digit);
    StringBuilder sb = new StringBuilder();

    // static html...
    sb.append(
        "<html>\n" +
          "<head>\n" +
            "<title>Simple Redis Test</title>\n" +
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
            "<div class='page-header'><h1>Simple Redis Test</h1></div>\n" +
             "<p class='lead'>This script picks a random digit and attempts to cache it in Redis," +
             "then read it back out.</p>\n");

    // actual cache / result values...
    sb.append("<p class='lead'>Digit: <span class='text-info'>" + digit + "</span></p>\n");
    sb.append("<p class='lead'>Cache: <span class='text-info'>" + r.val + "</span></p>\n");
    sb.append("<p class='lead'>Success? <span class='text-info'>" + r.result + "</span></p>\n");

    // static html again...
    sb.append("<script src='//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.2/js/bootstrap.min.js'></script>");
    sb.append("</body></html>");

    response.getWriter().print(sb.toString());
  }

  public void init() {
    try {
        ctx = new InitialContext();
        eyredishost = (String)ctx.lookup("java:comp/env/EYRedisHost");
        String host = eyredishost.split(":")[0];
        int port = Integer.parseInt(eyredishost.split(":")[1]);
        pool = new JedisPool(new JedisPoolConfig(), host, port);
        System.out.println("Got a Redis Pool, connecting to: " + eyredishost);
    } catch (NamingException ne) {
        System.err.println(
          "Failed creating initial context and looking up java:comp/env/EYRedisHost\nNamingException "
          + ne.getMessage());
    }catch (Exception e) {
        System.err.println(
          "Couldn't create a JedisPool, bailing out:\nException " + e.getMessage());
         e.printStackTrace();
    }
  }

  public void destroy() {
    pool.destroy();
  }

  private static class Result {
    public int val;
    public String result;

    public Result(int val, String result) {
      this.val = val;
      this.result = result;
    }
  }


  private static Result testRedis(int n) {

    cacheInRedis(n);

    Jedis jedis = null;
    try {
      jedis = pool.getResource();

      String inCache = jedis.get("" + n);
      if (inCache == null) {
        return new Result(0, "Nope, we didn't cache successfully.");
      } else {
        return new Result(Integer.parseInt(inCache), "Yep, our result came from Redis!");
      }
    } catch (JedisConnectionException e) {
      // returnBrokenResource when the state of the object is unrecoverable
      String message = "Couldn't get a Jedis instance from the pool\nException " + e.getMessage();
    	System.err.println(message);
    	if (null != jedis) {
          pool.returnBrokenResource(jedis);
          jedis = null;
      }
      return new Result(0, message);
    } finally {
      /// ... it's important to return the Jedis instance to the pool once you've finished using it
      if (null != jedis) {
        pool.returnResource(jedis);
      }
    }
  }

  private static void cacheInRedis(int n) {
    Jedis jedis = null;
    try {
      jedis = pool.getResource();
      jedis.set("" + n, Integer.toString(n));
    } catch (JedisConnectionException e) {
    	System.err.println(
  	          "Couldn't get a Jedis instance from the pool\nException " + e.getMessage());
    	// returnBrokenResource when the state of the object is unrecoverable
      if (null != jedis) {
          pool.returnBrokenResource(jedis);
          jedis = null;
      }
    } finally {
      /// ... it's important to return the Jedis instance to the pool once you've finished using it
      if (null != jedis) {
        pool.returnResource(jedis);
      }
    }
  }

}

