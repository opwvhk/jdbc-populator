JDBC Populator
==============

Created by Oscar Westra van Holthe - Kind
Date: 2012-04-04


The JDBC populator is a library with one core interface: `net.sf.opk.populator.JDBCPopulator`,
which in turn has a single method: `populateDatabase(java.sql.Connection)`. Its purpose is to populate a
database just before it is used. As such, it can help to build an Instant Developer Experience.

In order to make this all work, the library also contains several implementations of this interface, als also
implementations of `javax.sql.DataSource` and `javax.sql.XADataSource`.


Usage
-----


To use the JDBC populator, add it as a dependency to your application server (or servlet container). Also, don't
forget to include a database. Using Maven and an embedded application server, you can do that like this:

```
<plugin>
    <groupId>net.sf.opk</groupId>
    <artifactId>embedded-glassfish-web-plugin</artifactId>
    <version>1.3</version>
    <configuration>
        <glassFishResources>${project.build.configDirectory}/glassfish-resources.xml</glassFishResources>
    </configuration>
    <dependencies>
        <dependency>
			<groupId>net.sf.opk</groupId>
			<artifactId>jdbc-populator</artifactId>
			<version>3.0</version>
        </dependency>
        <dependency>
			<groupId>org.hsqldb</groupId>
			<artifactId>hsqldb</artifactId>
			<version>2.2.8</version>
        </dependency>
    </dependencies>
</plugin>
```

The file mentioned, `glassfish-resources.xml`, has content as explained in the next section.


Data Sources; GlassFish
-----------------------

As an example says more than a long explanation, the following example is a `glassfish-resources.xml` file
that defines both an XA and a non-XA data source, both of which use a `net.sf.opk.populator.JDBCPopulator` to
fill their respective databases. All possible uses of a `net.sf.opk.populator.JDBCPopulator` are demonstrated.

The same example (except the XA part) is also available for other containers (see below).

```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE resources PUBLIC "-//GlassFish.org//DTD GlassFish Application Server 3.1 Resource Definitions//EN"
                "http://glassfish.org/dtds/glassfish-resources_1_5.dtd">
<resources>
    <!--
    Underlying Local DataSource
    -->
    <jdbc-connection-pool name="UnderlyingPool" datasource-classname="org.hsqldb.jdbc.JDBCDataSource"
                          non-transactional-connections="true">
        <property name="Url" value="jdbc:hsqldb:mem:MyApplicationDB"/>
        <property name="User" value="sa"/>
        <property name="Password" value=""/>
    </jdbc-connection-pool>
    <jdbc-resource pool-name="UnderlyingPool" jndi-name="jdbc/underlyingDataSource"/>

    <!--
    Underlying XA DataSource
    -->
    <jdbc-connection-pool name="UnderlyingPool" datasource-classname="org.hsqldb.jdbc.JDBCXADataSource"
                          non-transactional-connections="true">
        <property name="Url" value="jdbc:hsqldb:mem:MyApplicationDB"/>
        <property name="User" value="sa"/>
        <property name="Password" value=""/>
    </jdbc-connection-pool>
    <jdbc-resource pool-name="UnderlyingPool" jndi-name="jdbc/underlyingXADataSource"/>

    <!--
    JDBC Populators
    -->
	<custom-resource jndi-name="test/populator-file" res-type="net.sf.opk.populator.sql.FileSqlPopulator"
	                 factory-class="org.glassfish.resources.custom.factory.JavaBeanFactory">
		<property name="fileName" value="src/main/config/database.sql"/>
	</custom-resource>
	<custom-resource jndi-name="test/populator-dir" res-type="net.sf.opk.populator.sql.DirectorySqlPopulator"
	                 factory-class="org.glassfish.resources.custom.factory.JavaBeanFactory">
		<property name="directoryName" value="src/main/config/db-updates"/>
	</custom-resource>
	<custom-resource jndi-name="test/populator" res-type="net.sf.opk.populator.CompositeJDBCPopulator"
	                 factory-class="org.glassfish.resources.custom.factory.JavaBeanFactory">
		<property name="populatorNames" value="test/populator-file test/populator-dir"/>
	</custom-resource>

    <!--
    Local DataSource for use by your application
    -->
    <jdbc-connection-pool name="LocalPool" datasource-classname="net.sf.opk.populator.PopulatingDataSource"
                          connection-validation-method="meta-data" is-connection-validation-required="true">
        <property name="PopulatorName" value="test/populator"/>
        <property name="DelegateName" value="jdbc/underlyingDataSource"/>
    </jdbc-connection-pool>
    <jdbc-resource pool-name="LocalPool" jndi-name="jdbc/myApplicationDataSource"/>

    <!--
    XA DataSource for use by your application
    -->
    <jdbc-connection-pool name="LocalPool" datasource-classname="net.sf.opk.populator.PopulatingXADataSource"
                          connection-validation-method="meta-data" is-connection-validation-required="true">
        <property name="PopulatorName" value="test/populator"/>
        <property name="DelegateName" value="jdbc/underlyingXADataSource"/>
    </jdbc-connection-pool>
    <jdbc-resource pool-name="LocalPool" jndi-name="jdbc/myApplicationXADataSource"/>
</resources>
```

The `JDBCPopulator` is called upon first use of each data source.

This enables the creation of an _Instant Developer Experience_, as a datasource for an in-memory database can be
populated upon first use. This means that if you start the application, your application will have data available.

Additionally, the `JDBCPopulator` can also populate our EJB component tests. Using JavaEE 6, you can use:

```
@DataSourceDefinition(name="java:global/MyApp/MyDataSource", ...)
public MyCodeTest
{
    @Resource(name = "java:global/MyApp/MyDataSource")
    DataSource datasource;

    @Before
    public void populateDatabase() throws SQLException, IOException
    {
        try (Connection connection = datasource.getConnection())
        {
            JDBCPopulator populator = new net.sf.opk.populator.sql.FileSqlPopulator("/path/to/file.sql");
            populator.populateDatabase(datasource.getConnection());
        }
    }
}
```

Other JNDI Containers
---------------------

In addition to GlassFish, JDBC Populator also supports other JNDI implementations. It has also been tested on
Tomcat 7.0 and Jetty 8.1. The JDBC Populator should also work (but has _not_ been tested) on JBoss, WebLogic,
WebSphere and Resin.

The only containers that are specifically not supported are those that cannot create generic JavaBean resources in
JNDI without the help of a custom JNDI `ObjectFactory`.

Jetty
-----

For the example below, you also need a database connection pool implementation. The example below uses Commons DBCP.

Example `jetty-env.xml`:

```
<?xml version="1.0"  encoding="ISO-8859-1"?>
<!DOCTYPE Configure PUBLIC "-//Mort Bay Consulting//DTD Configure//EN" "http://www.eclipse.org/jetty/configure.dtd">
<Configure class="org.eclipse.jetty.webapp.WebAppContext">
	<New id="underlyingLocalDataSource" class="org.apache.commons.dbcp.BasicDataSource">
		<Set name="driverClassName">org.hsqldb.jdbcDriver</Set>
		<Set name="url">jdbc:hsqldb:mem:TestDB</Set>
		<Set name="username">sa</Set>
		<Set name="password"/>
		<Set name="maxActive">5</Set>
		<Set name="maxIdle">5</Set>
		<Set name="minIdle">5</Set>
	</New>

	<New id="filePopulator" class="net.sf.opk.populator.sql.FileSqlPopulator">
		<Arg>src/main/config/database.sql</Arg>
	</New>
	<New id="directoryPopulator" class="net.sf.opk.populator.sql.DirectorySqlPopulator">
		<Arg>src/main/config/db-updates</Arg>
	</New>
	<New id="populator" class="net.sf.opk.populator.CompositeJDBCPopulator">
		<Call name="addPopulator">
			<Arg><Ref id="filePopulator"/></Arg>
		</Call>
		<Call name="addPopulator">
			<Arg><Ref id="directoryPopulator"/></Arg>
		</Call>
	</New>

	<New class="org.eclipse.jetty.plus.jndi.Resource">
		<Arg>jdbc/applicationDataSource</Arg>
		<Arg>
			<New class="net.sf.opk.populator.PopulatingDataSource">
				<Call name="setPopulator">
					<Arg><Ref id="populator"/></Arg>
				</Call>
				<Call name="setDelegate">
					<Arg><Ref id="underlyingDataSource"/></Arg>
				</Call>
			</New>
		</Arg>
	</New>
</Configure>
```


Tomcat
------

Example `context.xml`:

```
<?xml version="1.0" encoding="UTF-8"?>
<Context>
	<Resource name="jdbc/underlyingDataSource"
	          type="javax.sql.DataSource" auth="Container" driverClassName="org.hsqldb.jdbcDriver"
	          url="jdbc:hsqldb:mem:TestDB" username="sa" password=""/>

    <Resource name="test/populator-file" type="net.sf.opk.populator.sql.FileSqlPopulator" auth="Container"
              factory="org.apache.naming.factory.BeanFactory" fileName="src/main/config/database.sql"/>
    <Resource name="test/populator-dir" type="net.sf.opk.populator.sql.DirectorySqlPopulator" auth="Container"
              factory="org.apache.naming.factory.BeanFactory" directoryName="src/main/config/db-updates"/>
    <Resource name="test/populator" type="net.sf.opk.populator.CompositeJDBCPopulator" auth="Container"
              factory="org.apache.naming.factory.BeanFactory" populatorNames="test/populator-file test/populator-dir"/>

	<!-- NOTE: Tomcat 7.0.x does NOT support the JavaEE 6 JNDI namespaces java:global, java:app and java:module ! -->
    <Resource name="jdbc/applicationDataSource" type="net.sf.opk.populator.PopulatingDataSource" auth="Container"
              factory="org.apache.naming.factory.BeanFactory"
              delegateName="java:comp/env/jdbc/underlyingDataSource" populatorName="java:comp/env/test/populator"/>
</Context>
```


Conclusion
----------

The `JDBCPopulator` is a valuable asset during development, both for trying out an application and to test
components contacting a database.

