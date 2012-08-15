package net.sf.opk.populator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertEquals;


/**
 * Case class for tests to a database.
 *
 * @author <a href="mailto:oscar@westravanholthe.nl">Oscar Westra van Holthe - Kind</a>
 */
public class DatabaseTestBase
{
	private Connection connectionForTest;
	private Connection connectionToVerify;


	@Before
	public void setUpDatabase() throws ClassNotFoundException, SQLException
	{
		connectionForTest = openDbConnection();
		connectionToVerify = openDbConnection();

		Statement statement = connectionToVerify.createStatement();
		statement.execute("create table Record (id integer primary key, name varchar(32) not null)");
		statement.close();

		connectionToVerify.commit();
	}


	protected Connection openDbConnection() throws ClassNotFoundException, SQLException
	{
		Class.forName("org.hsqldb.jdbc.JDBCDriver");
		return DriverManager.getConnection("jdbc:hsqldb:mem:testDb", "sa", "");
	}


	@After
	public void tearDownDatabase() throws Exception
	{
		Statement statement = connectionToVerify.createStatement();
		statement.execute("drop table Record");
		statement.close();

		connectionToVerify.close();
		connectionForTest.close();
	}


	protected void checkRecordCount(long recordCount) throws SQLException
	{
		Statement statement = connectionToVerify.createStatement();
		ResultSet resultSet = statement.executeQuery("select count(*) from Record");
		resultSet.next();
		try
		{
			assertEquals(recordCount, resultSet.getLong(1));
		}
		finally
		{
			resultSet.close();
			statement.close();
		}
	}


	protected Connection getConnectionForTest()
	{
		return connectionForTest;
	}
}
