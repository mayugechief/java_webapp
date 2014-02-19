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
