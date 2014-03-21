java_webapp
===========

Objectives
----------
These are simple demos for illustrating basic Java deployment on Engine Yard. Future releases will enable generic JDBC access, more complex Spring configurations, and possibly support for JEE containers. 

Contents
--------
This repo contains the following java web applications:

- HelloServlet: A vanilla "hello world" Servlet 
- HelloJNDI: A Simple Servlet that uses a container-configured JDBC datasource (with JNDI name `EYMySQL`)
- HelloSpringMVC: A basic demo that uses Spring MVC for the web tier and and also uses Spring's `JdbcTemplate` with the `EYMySQL` datasource
- HelloPostgres: HelloJNDI, but updated to use Postgresql JNDI name `EYPostgresql`
- HelloMemcached: A basic memcached demo, showing how to configure a spymemcached client, set and get objects
- HelloRedis: A basic redis demo, showing how to configure a jedis client, set and get objects

along with an ant configuration file (build.xml) to build the sample .war files. 

Releases
--------
The [releases][8] area above contains pre-built .war files for the three demo Servlets.

Prerequisites
-------------
- You need a JDK [Java 7][1] and Apache [ant][7] to build these samples

These samples should run in any Servlet engine or web container. If you wish to deploy these samples on [Engine Yard][2], you need an account. [Sign up][5] for one!

Getting Started
---------------
1. `git clone` this repository locally
2. run `ant` (building takes a while because the ant configuration uses ivy for dependency management)
3. Target .war files are built in the deploy directory

Configuring the Database
------------------------
The HelloJNDI, HelloSpringMVC and HellPostgres Servlets connect to a container-configured JDBC datasource to retrieve a message displayed by the Servlet Response.

Your database server on Engine Yard will be installed with an empty database with the same name as your environment. The database will also have an application specific user which is granted access rights to that database.
So, if my environment is called 'javademoenv', the database will also be 'javademoenv' and the user will be 'javademoenv_user'. The below examples make use of these example values.

To configure the database in your Engine Yard environment for these examples, you need to:

1. SSH into your database server.
If you have not set up SSH, follow [these instructions][6].

2. Follow the instructions below for the specific demo you are deploying.

###HelloJNDI and HelloSpringMVC

Enter the `mysql` command to access the mysql cli:

<pre>mysql -u javademo_user -p </pre>
(Enter the database password at the prompt.)

To retrieve your database password:
 * Log into your Engine Yard account.
 * Select the relevant organization.
 * Select the environment containing the relevant database server.
 * Click the Servers tab.
 * Find the database server and click the "Show password" link.

Enter the following into the mysql command line:

<pre> mysql > use javademoenv;</pre>

<pre> mysql >
DROP TABLE IF EXISTS `javademo`;
CREATE TABLE `javademo` (
`ID` varchar(4) NOT NULL,
`message` varchar(128) NOT NULL,
PRIMARY KEY  (`ID`),
UNIQUE KEY `ID_UNIQUE` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
INSERT INTO `javademo` VALUES ('jndi','Hello from the JNDI servlet!'), ('spf','Hello from the Spring servlet!');
</pre>

###HelloPostgres

First, su to the postges user:

<pre>su postgres</pre>

Then, login to the database for your application:

<pre>psql javademoenv </pre>

Paste the following CREATE statement into the postgresql cli:

<pre>javademoenv=>
CREATE TABLE javademo (
ID varchar(4) PRIMARY KEY,
message varchar(128) NOT NULL
);
</pre>

Finally, insert the following initial rows into the new table.

<pre>
INSERT INTO javademo VALUES ('jndi','Hello from the JNDI servlet!'), ('spf','Hello from the Spring servlet!');
</pre>

Using a Cache With your Java App on Engine Yard
-----------------------------------------------

The 3 primary use cases for Caches (Key/value stores) and Java apps are:

1. as a shared/replication session store
2. as a database query resultset cache (e.g., see this)
3. as a general purpose object cache

These examples focus on the 3rd use case, configuring a general object cache.

Using Memcached
---------------

We've configured Engine Yard to use the spymemcached[11] client lib simply because it seems to be the most commonly used Java ibrary for memcached.

The important thing about using this library is getting the configuration into the client code needed to create a memcached client and connections. Because the firewall rules in your Engine Yard environment are configured so that only the application servers can connect to the cache servers, we don't use authentication (yet) to the memcached server, so the only thing we need to get into the client code is the list of servers. The spymemcached client uses it this way:

<pre>
// Get a memcached client connected to several servers
MemcachedClient c=new MemcachedClient(
        AddrUtil.getAddresses("server1:11211 server2:11211"));
</pre>

The ivy build script in the demo code pulls down the spymemcached lib from Maven.  You will need to include the spymemcached jar file in any war file of code you want to deploy on Engine Yard.

Memcachier is a common memcached-aaS. We've reused their [demo code][9].  The demo code gets the list of memcached servers to configure the MemcachedClient object using Environment Entries. (This is a configuration mechanism in the Java Servlet specification. These entries are arbitrary types and values that an app server can configure and make available to Servlet code.)

So, for Jetty and Tomcat app servers, when Engine Yard updates servers in the environment, the Chef cookbooks include the right <env-entries>, and values for the memcache servers, using the JNDI name "EYMCHosts". The EYMCHosts entry is a java.lang.String with the syntax "server1:port1 server2:port2". This app server configuration is handled with a different syntax in Jetty and Tomcat configs, 

The jetty webapps/root.xml file in an environment with a memcached server contains an entry like this (the hostname will be different of course):

```xml
  <New id="EYMCHosts" class="org.eclipse.jetty.plus.jndi.EnvEntry">
  <Arg></Arg>
  <Arg>EYMCHosts</Arg>
  <Arg type="java.lang.String">ldomU-12-31-39-0B-31-82.compute-1.internal:11211</Arg>
  <Arg type="boolean">true</Arg>
  </New>
```

The tomcat conf/Catalina/localhost/ROOT.xml file in an environment with a memcached server contains an entry like this:
```xml
   <Environment name="EYMCHosts" value="domU-12-31-39-0B-31-82.compute-1.internal:11211"
         type="java.lang.String" override="false"/>
```

but in either case, the client code is the same (isn't Servlet portability great :\ ): 

<pre>
  private void getServersFromContext() throws NamingException {
      ctx = new InitialContext();
      eymchosts = (String)ctx.lookup("java:comp/env/EYMCHosts");
  }
</pre>

Using the Redis example
-----------------------
Engine Yard integrates with the jedis[10] redis client library for Java. And again, the integration with Engine Yard application servers works by making the hostname and port of the Redis server available to Java code using an environment entry called 'EYRedisHost'. 

An excerpt from the HelloRedis demo code (without exception handling, for clarity) that shows how to configure a pool of Jedis client objects
<pre>
        ctx = new InitialContext();
        eyredishost = (String)ctx.lookup("java:comp/env/EYRedisHost");
        String host = eyredishost.split(":")[0];
        int port = Integer.parseInt(eyredishost.split(":")[1]);
        pool = new JedisPool(new JedisPoolConfig(), host, port);
 </pre>


The jetty webapps/root.xml file in an environment with a redis server contains an entry like this:

```xml
<New id="EYRedisHost" class="org.eclipse.jetty.plus.jndi.EnvEntry">
  <Arg></Arg>
  <Arg>EYRedisHost</Arg>
  <Arg type="java.lang.String">ip-10-5-58-200.ec2.internal:6379</Arg>
  <Arg type="boolean">true</Arg>
</New>
```

The tomcat conf/Catalina/localhost/ROOT.xml file in an environment with a redis server contains an entry like this:
```xml
    <Environment name="EYRedisHost" value="ip-10-5-58-200.ec2.internal:6379"
         type="java.lang.String" override="false"/>
```


Deploying the Applications
--------------------------
The war files built locally by ant can be deployed with Engine Yard using the [UI][3] or [CLI][4].


[1]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
[2]: http://ui.engineyard.com
[3]: https://support.cloud.engineyard.com/entries/26483236-User-Interface-for-Deploying-your-Java-Application-on-Engine-Yard
[4]: https://support.cloud.engineyard.com/entries/27042383-CLI-for-Deploying-your-Java-Application-on-Engine-Yard
[5]: https://support.cloud.engineyard.com/entries/27322283-Sign-up-for-an-Engine-Yard-Account
[6]: https://support.cloud.engineyard.com/entries/27519756-Set-up-SSH
[7]: http://ant.apache.org/
[8]: https://github.com/engineyard/java_webapp/releases
[9]: https://github.com/memcachier/examples-java
[10]: https://github.com/xetorthio/jedis
[11]: https://code.google.com/p/spymemcached
