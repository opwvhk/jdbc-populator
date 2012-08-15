/*
 * Copyright (c) 2012 Oscar Westra van Holthe - Kind
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package net.sf.opk.populator;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;


public class PopulatingXADataSourceTest
{
	private Context mockContext;
	private PopulatingXADataSource datasource;
	private JDBCPopulator populator;
	private XADataSource delegate;
	private static final String POPULATOR_JNDI_NAME = "POPULATOR_JNDI_NAME";
	private static final String DELEGATE_JNDI_NAME = "DELEGATE_JNDI_NAME";


	@Before
	public void setUp() throws NamingException
	{
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, DummyInitialContextFactory.class.getName());
		mockContext = createMock(Context.class);
		DummyInitialContextFactory.setMockContext(mockContext);

		datasource = new PopulatingXADataSource();
		datasource.setDelegate(DELEGATE_JNDI_NAME);
		datasource.setPopulator(POPULATOR_JNDI_NAME);

		populator = createMock(JDBCPopulator.class);

		delegate = createMock(XADataSource.class);

		reset(mockContext, populator, delegate);
	}


	@After
	public void restoreJNDI() throws Exception
	{
		System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
	}


	@Test(expected = IllegalStateException.class)
	public void testSetUpFailure1() throws NamingException, SQLException
	{
		try
		{
			expect(mockContext.lookup(DELEGATE_JNDI_NAME)).andReturn(new Object());
			replay(mockContext);
			datasource.getLoginTimeout();
		}
		finally
		{
			verify(mockContext);
		}
	}


	@Test(expected = IllegalStateException.class)
	public void testSetUpFailure2() throws NamingException, SQLException
	{
		datasource.setDelegate(delegate);

		XAConnection xaConnection = createMock(XAConnection.class);
		Connection connection = createMock(Connection.class);
		try
		{
			expect(delegate.getXAConnection()).andReturn(xaConnection);
			expect(xaConnection.getConnection()).andReturn(connection);

			expect(connection.getAutoCommit()).andReturn(false);
			connection.rollback();
			expectLastCall();

			expect(mockContext.lookup(POPULATOR_JNDI_NAME)).andReturn(new Object());

			replay(xaConnection, connection, mockContext, populator, delegate);
			datasource.setPopulator(POPULATOR_JNDI_NAME);
			datasource.getXAConnection();
		}
		finally
		{
			verify(xaConnection, connection, mockContext, populator, delegate);
		}
		try
		{
			expect(mockContext.lookup(POPULATOR_JNDI_NAME)).andReturn(new Object());
			replay(mockContext);

			datasource.setPopulator(POPULATOR_JNDI_NAME);
		}
		finally
		{
			verify(mockContext);
		}
	}


	@Test(expected = SQLException.class)
	public void testGetConnection1a() throws SQLException
	{
		datasource.setDelegate(delegate);

		XAConnection xaConnection = createMock(XAConnection.class);
		Connection connection = createMock(Connection.class);
		try
		{
			expect(delegate.getXAConnection()).andReturn(xaConnection);
			expect(xaConnection.getConnection()).andReturn(connection);
			expect(connection.getAutoCommit()).andThrow(new SQLException());

			replay(xaConnection, connection, mockContext, populator, delegate);
			datasource.getXAConnection();
		}
		finally
		{
			verify(xaConnection, connection, mockContext, populator, delegate);
		}
	}


	@Test(expected = SQLException.class)
	public void testGetConnection2a() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		// Use lookup here so it's tested. Set a populator directly further on for readability.
		expect(mockContext.lookup(POPULATOR_JNDI_NAME)).andReturn(populator);

		XAConnection xaConnection = createMock(XAConnection.class);
		Connection connection = createMock(Connection.class);
		try
		{
			expect(delegate.getXAConnection()).andReturn(xaConnection);
			expect(xaConnection.getConnection()).andReturn(connection);

			expect(connection.getAutoCommit()).andReturn(false);
			connection.rollback();
			expectLastCall();

			populator.populateDatabase(connection);
			expectLastCall().andThrow(new IOException());

			replay(xaConnection, connection, mockContext, populator, delegate);
			datasource.setPopulator(POPULATOR_JNDI_NAME);
			datasource.getXAConnection();
		}
		finally
		{
			verify(xaConnection, connection, mockContext, populator, delegate);
		}
	}


	@Test(expected = SQLException.class)
	public void testGetConnection3a() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		datasource.setPopulator(populator);

		XAConnection xaConnection = createMock(XAConnection.class);
		Connection connection = createMock(Connection.class);
		try
		{
			expect(delegate.getXAConnection()).andReturn(xaConnection);
			expect(xaConnection.getConnection()).andReturn(connection);

			expect(connection.getAutoCommit()).andReturn(false);
			connection.rollback();
			expectLastCall();

			populator.populateDatabase(connection);
			expectLastCall().andThrow(new SQLException());

			replay(xaConnection, connection, mockContext, populator, delegate);
			datasource.setPopulator(POPULATOR_JNDI_NAME);
			datasource.getXAConnection();
		}
		finally
		{
			verify(xaConnection, connection, mockContext, populator, delegate);
		}
	}


	@Test
	public void testGetConnection4a() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		datasource.setPopulator(populator);

		XAConnection xaConnection = createMock(XAConnection.class);
		Connection connection = createMock(Connection.class);

		expect(delegate.getXAConnection()).andReturn(xaConnection);
		expect(xaConnection.getConnection()).andReturn(connection);

		expect(connection.getAutoCommit()).andReturn(false);
		connection.commit();
		expectLastCall();

		populator.populateDatabase(connection);
		expectLastCall();

		replay(xaConnection, connection, mockContext, populator, delegate);
		datasource.setPopulator(POPULATOR_JNDI_NAME);
		assertSame(xaConnection, datasource.getXAConnection());
		verify(xaConnection, connection, mockContext, populator, delegate);
	}


	@Test
	public void testGetConnection5a() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		datasource.setPopulator(populator);

		XAConnection xaConnection = createMock(XAConnection.class);
		Connection connection = createMock(Connection.class);

		expect(delegate.getXAConnection()).andReturn(xaConnection).times(2);
		expect(xaConnection.getConnection()).andReturn(connection);

		expect(connection.getAutoCommit()).andReturn(false);
		connection.commit();
		expectLastCall();

		populator.populateDatabase(connection);
		expectLastCall();

		replay(xaConnection, connection, mockContext, populator, delegate);
		datasource.setPopulator(POPULATOR_JNDI_NAME);
		assertSame(xaConnection, datasource.getXAConnection());
		assertSame(xaConnection, datasource.getXAConnection());
		verify(xaConnection, connection, mockContext, populator, delegate);
	}


	@Test
	public void testGetConnection6a() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		datasource.setPopulator(populator);

		XAConnection xaConnection = createMock(XAConnection.class);
		Connection connection = createMock(Connection.class);

		expect(delegate.getXAConnection()).andReturn(xaConnection);
		expect(xaConnection.getConnection()).andReturn(connection);

		expect(connection.getAutoCommit()).andReturn(true);
		connection.setAutoCommit(false);
		expectLastCall();
		connection.commit();
		expectLastCall();
		connection.setAutoCommit(true);
		expectLastCall();

		populator.populateDatabase(connection);
		expectLastCall();

		replay(xaConnection, connection, mockContext, populator, delegate);
		datasource.setPopulator(POPULATOR_JNDI_NAME);
		assertSame(xaConnection, datasource.getXAConnection());
		verify(xaConnection, connection, mockContext, populator, delegate);
	}


	@Test(expected = SQLException.class)
	public void testGetConnection1b() throws SQLException, NamingException
	{
		datasource.setDelegate(delegate);

		final String user = "username";
		final String pass = "password";

		XAConnection xaConnection = createMock(XAConnection.class);
		Connection connection = createMock(Connection.class);
		try
		{
			expect(delegate.getXAConnection(user, pass)).andReturn(xaConnection);
			expect(xaConnection.getConnection()).andReturn(connection);
			expect(connection.getAutoCommit()).andThrow(new SQLException());

			replay(xaConnection, connection, mockContext, populator, delegate);
			datasource.getXAConnection(user, pass);
		}
		finally
		{
			verify(xaConnection, connection, mockContext, populator, delegate);
		}
	}


	@Test(expected = SQLException.class)
	public void testGetConnection2b() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		datasource.setPopulator(populator);

		final String user = "username";
		final String pass = "password";

		XAConnection xaConnection = createMock(XAConnection.class);
		Connection connection = createMock(Connection.class);
		try
		{
			expect(delegate.getXAConnection(user, pass)).andReturn(xaConnection);
			expect(xaConnection.getConnection()).andReturn(connection);

			expect(connection.getAutoCommit()).andReturn(false);
			connection.rollback();
			expectLastCall();

			populator.populateDatabase(connection);
			expectLastCall().andThrow(new IOException());

			replay(xaConnection, connection, mockContext, populator, delegate);
			datasource.setPopulator(POPULATOR_JNDI_NAME);
			datasource.getXAConnection(user, pass);
		}
		finally
		{
			verify(xaConnection, connection, mockContext, populator, delegate);
		}
	}


	@Test(expected = SQLException.class)
	public void testGetConnection3b() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		datasource.setPopulator(populator);

		final String user = "username";
		final String pass = "password";

		XAConnection xaConnection = createMock(XAConnection.class);
		Connection connection = createMock(Connection.class);
		try
		{
			expect(delegate.getXAConnection(user, pass)).andReturn(xaConnection);
			expect(xaConnection.getConnection()).andReturn(connection);

			expect(connection.getAutoCommit()).andReturn(false);
			connection.rollback();
			expectLastCall();

			populator.populateDatabase(connection);
			expectLastCall().andThrow(new SQLException());

			replay(xaConnection, connection, mockContext, populator, delegate);
			datasource.setPopulator(POPULATOR_JNDI_NAME);
			datasource.getXAConnection(user, pass);
		}
		finally
		{
			verify(xaConnection, connection, mockContext, populator, delegate);
		}
	}


	@Test
	public void testGetConnection4b() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		datasource.setPopulator(populator);

		final String user = "username";
		final String pass = "password";

		XAConnection xaConnection = createMock(XAConnection.class);
		Connection connection = createMock(Connection.class);

		expect(delegate.getXAConnection(user, pass)).andReturn(xaConnection);
		expect(xaConnection.getConnection()).andReturn(connection);

		expect(connection.getAutoCommit()).andReturn(false);
		connection.commit();
		expectLastCall();

		populator.populateDatabase(connection);
		expectLastCall();

		replay(xaConnection, connection, mockContext, populator, delegate);
		datasource.setPopulator(POPULATOR_JNDI_NAME);
		assertSame(xaConnection, datasource.getXAConnection(user, pass));
		verify(xaConnection, connection, mockContext, populator, delegate);
	}


	@Test
	public void testGetConnection5b() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		datasource.setPopulator(populator);

		final String user = "username";
		final String pass = "password";

		XAConnection xaConnection = createMock(XAConnection.class);
		Connection connection = createMock(Connection.class);

		expect(delegate.getXAConnection(user, pass)).andReturn(xaConnection).times(2);
		expect(xaConnection.getConnection()).andReturn(connection);

		expect(connection.getAutoCommit()).andReturn(false);
		connection.commit();
		expectLastCall();

		populator.populateDatabase(connection);
		expectLastCall();

		replay(xaConnection, connection, mockContext, populator, delegate);
		datasource.setPopulator(POPULATOR_JNDI_NAME);
		assertSame(xaConnection, datasource.getXAConnection(user, pass));
		assertSame(xaConnection, datasource.getXAConnection(user, pass));
		verify(xaConnection, connection, mockContext, populator, delegate);
	}


	@Test
	public void testGetConnection6b() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		datasource.setPopulator(populator);

		final String user = "username";
		final String pass = "password";

		XAConnection xaConnection = createMock(XAConnection.class);
		Connection connection = createMock(Connection.class);

		expect(delegate.getXAConnection(user, pass)).andReturn(xaConnection);
		expect(xaConnection.getConnection()).andReturn(connection);

		expect(connection.getAutoCommit()).andReturn(true);
		connection.setAutoCommit(false);
		expectLastCall();
		connection.commit();
		expectLastCall();
		connection.setAutoCommit(true);
		expectLastCall();

		populator.populateDatabase(connection);
		expectLastCall();

		replay(xaConnection, connection, mockContext, populator, delegate);
		datasource.setPopulator(POPULATOR_JNDI_NAME);
		assertSame(xaConnection, datasource.getXAConnection(user, pass));
		verify(xaConnection, connection, mockContext, populator, delegate);
	}


	@Test
	public void testGetLoginTimeout() throws SQLException
	{
		datasource.setDelegate(delegate);

		int number = 42;
		expect(delegate.getLoginTimeout()).andReturn(number);

		replay(mockContext, populator, delegate);
		assertEquals(number, datasource.getLoginTimeout());
		verify(mockContext, populator, delegate);
	}


	@Test
	public void testSetLoginTimeout() throws SQLException
	{
		datasource.setDelegate(delegate);

		int number = 42;
		delegate.setLoginTimeout(number);
		expectLastCall();

		replay(mockContext, populator, delegate);
		datasource.setLoginTimeout(number);
		verify(mockContext, populator, delegate);
	}


	@Test
	public void testGetLogWriter() throws SQLException
	{
		datasource.setDelegate(delegate);

		@SuppressWarnings("UseOfSystemOutOrSystemErr")
		PrintWriter writer = new PrintWriter(System.out);
		expect(delegate.getLogWriter()).andReturn(writer);

		replay(mockContext, populator, delegate);
		assertSame(writer, datasource.getLogWriter());
		verify(mockContext, populator, delegate);
	}


	@Test
	public void testSetLogWriter() throws SQLException
	{
		datasource.setDelegate(delegate);

		@SuppressWarnings("UseOfSystemOutOrSystemErr")
		PrintWriter writer = new PrintWriter(System.out);
		delegate.setLogWriter(writer);
		expectLastCall();

		replay(mockContext, populator, delegate);
		datasource.setLogWriter(writer);
		verify(mockContext, populator, delegate);
	}


	@Test
	public void testGetParentLogger() throws SQLException
	{
		datasource.setDelegate(delegate);

		Logger logger = Logger.getAnonymousLogger();
		expect(delegate.getParentLogger()).andReturn(logger);

		replay(mockContext, populator, delegate);
		assertSame(logger, datasource.getParentLogger());
		verify(mockContext, populator, delegate);
	}
}
