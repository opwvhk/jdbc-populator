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
import javax.sql.DataSource;

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;


public class PopulatingDataSourceTest
{
	private Context mockContext;
	private PopulatingDataSource datasource;
	private JDBCPopulator populator;
	private DataSource delegate;
	private static final String POPULATOR_JNDI_NAME = "POPULATOR_JNDI_NAME";
	private static final String DELEGATE_JNDI_NAME = "DELEGATE_JNDI_NAME";


	@Before
	public void setUp() throws NamingException
	{
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, DummyInitialContextFactory.class.getName());
		mockContext = createMock(Context.class);
		DummyInitialContextFactory.setMockContext(mockContext);

		datasource = new PopulatingDataSource();
		datasource.setDelegate(DELEGATE_JNDI_NAME);
		datasource.setPopulator(POPULATOR_JNDI_NAME);

		populator = createMock(JDBCPopulator.class);

		delegate = createMock(DataSource.class);

		reset(mockContext, populator, delegate);
	}


	@After
	public void restoreJNDI() throws Exception
	{
		System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
	}


	@Test(expected = IllegalStateException.class)
	public void testLookupFailure1() throws NamingException, SQLException
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
	public void testLookupFailure2() throws SQLException, NamingException
	{
		datasource.setDelegate(delegate);

		Connection connection = createMock(Connection.class);
		try
		{
			expect(delegate.getConnection()).andReturn(connection);
			expect(connection.getAutoCommit()).andReturn(false);
			connection.rollback();
			expectLastCall();

			expect(mockContext.lookup(POPULATOR_JNDI_NAME)).andReturn(new Object());

			replay(connection, mockContext, populator, delegate);
			assertSame(connection, datasource.getConnection());
		}
		finally
		{
			verify(connection, mockContext, populator, delegate);
		}
	}


	@Test(expected = SQLException.class)
	public void testGetConnection1a() throws SQLException, NamingException
	{
		datasource.setDelegate(delegate);

		Connection connection = createMock(Connection.class);
		try
		{
			expect(delegate.getConnection()).andReturn(connection);
			expect(connection.getAutoCommit()).andThrow(new SQLException());

			replay(connection, mockContext, populator, delegate);
			datasource.getConnection();
		}
		finally
		{
			verify(connection, mockContext, populator, delegate);
		}
	}


	@Test(expected = SQLException.class)
	public void testGetConnection2a() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		// Use lookup here so it's tested. Set a populator directly further on for readability.
		expect(mockContext.lookup(POPULATOR_JNDI_NAME)).andReturn(populator);

		Connection connection = createMock(Connection.class);
		try
		{
			expect(delegate.getConnection()).andReturn(connection);
			expect(connection.getAutoCommit()).andReturn(false);
			connection.rollback();
			expectLastCall();

			populator.populateDatabase(connection);
			expectLastCall().andThrow(new IOException());

			replay(connection, mockContext, populator, delegate);
			assertSame(connection, datasource.getConnection());
		}
		finally
		{
			verify(connection, mockContext, populator, delegate);
		}
	}


	@Test(expected = SQLException.class)
	public void testGetConnection3a() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		datasource.setPopulator(populator);

		Connection connection = createMock(Connection.class);
		try
		{
			expect(delegate.getConnection()).andReturn(connection);
			expect(connection.getAutoCommit()).andReturn(false);
			connection.rollback();
			expectLastCall();

			populator.populateDatabase(connection);
			expectLastCall().andThrow(new SQLException());

			replay(connection, mockContext, populator, delegate);
			assertSame(connection, datasource.getConnection());
		}
		finally
		{
			verify(connection, mockContext, populator, delegate);
		}
	}


	@Test
	public void testGetConnection4a() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		datasource.setPopulator(populator);

		Connection connection = createMock(Connection.class);
		expect(delegate.getConnection()).andReturn(connection);
		expect(connection.getAutoCommit()).andReturn(false);
		connection.commit();
		expectLastCall();

		populator.populateDatabase(connection);
		expectLastCall();

		replay(connection, mockContext, populator, delegate);
		assertSame(connection, datasource.getConnection());
		verify(connection, mockContext, populator, delegate);
	}


	@Test
	public void testGetConnection5a() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		datasource.setPopulator(populator);

		Connection connection = createMock(Connection.class);
		expect(delegate.getConnection()).andReturn(connection).times(2);
		expect(connection.getAutoCommit()).andReturn(false);
		connection.commit();
		expectLastCall();

		populator.populateDatabase(connection);
		expectLastCall();

		replay(connection, mockContext, populator, delegate);
		assertSame(connection, datasource.getConnection());
		assertSame(connection, datasource.getConnection());
		verify(connection, mockContext, populator, delegate);
	}


	@Test
	public void testGetConnection6a() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		datasource.setPopulator(populator);

		Connection connection = createMock(Connection.class);
		expect(delegate.getConnection()).andReturn(connection);
		expect(connection.getAutoCommit()).andReturn(true);
		connection.setAutoCommit(false);
		expectLastCall();
		connection.commit();
		expectLastCall();
		connection.setAutoCommit(true);
		expectLastCall();

		populator.populateDatabase(connection);
		expectLastCall();

		replay(connection, mockContext, populator, delegate);
		assertSame(connection, datasource.getConnection());
		verify(connection, mockContext, populator, delegate);
	}


	@Test(expected = SQLException.class)
	public void testGetConnection1b() throws SQLException, NamingException
	{
		datasource.setDelegate(delegate);

		final String user = "username";
		final String pass = "password";

		Connection connection = createMock(Connection.class);
		try
		{
			expect(delegate.getConnection(user, pass)).andReturn(connection);
			expect(connection.getAutoCommit()).andThrow(new SQLException());

			replay(connection, mockContext, populator, delegate);
			datasource.getConnection(user, pass);
		}
		finally
		{
			verify(connection, mockContext, populator, delegate);
		}
	}


	@Test(expected = SQLException.class)
	public void testGetConnection2b() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		datasource.setPopulator(populator);

		final String user = "username";
		final String pass = "password";

		Connection connection = createMock(Connection.class);
		try
		{
			expect(delegate.getConnection(user, pass)).andReturn(connection);
			expect(connection.getAutoCommit()).andReturn(false);
			connection.rollback();
			expectLastCall();

			populator.populateDatabase(connection);
			expectLastCall().andThrow(new IOException());

			replay(connection, mockContext, populator, delegate);
			assertSame(connection, datasource.getConnection(user, pass));
		}
		finally
		{
			verify(connection, mockContext, populator, delegate);
		}
	}


	@Test(expected = SQLException.class)
	public void testGetConnection3b() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		datasource.setPopulator(populator);

		final String user = "username";
		final String pass = "password";

		Connection connection = createMock(Connection.class);
		try
		{
			expect(delegate.getConnection(user, pass)).andReturn(connection);
			expect(connection.getAutoCommit()).andReturn(false);
			connection.rollback();
			expectLastCall();

			populator.populateDatabase(connection);
			expectLastCall().andThrow(new SQLException());

			replay(connection, mockContext, populator, delegate);
			assertSame(connection, datasource.getConnection(user, pass));
		}
		finally
		{
			verify(connection, mockContext, populator, delegate);
		}
	}


	@Test
	public void testGetConnection4b() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		datasource.setPopulator(populator);

		final String user = "username";
		final String pass = "password";

		Connection connection = createMock(Connection.class);
		expect(delegate.getConnection(user, pass)).andReturn(connection);
		expect(connection.getAutoCommit()).andReturn(false);
		connection.commit();
		expectLastCall();

		populator.populateDatabase(connection);
		expectLastCall();

		replay(connection, mockContext, populator, delegate);
		assertSame(connection, datasource.getConnection(user, pass));
		verify(connection, mockContext, populator, delegate);
	}


	@Test
	public void testGetConnection5b() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		datasource.setPopulator(populator);

		final String user = "username";
		final String pass = "password";

		Connection connection = createMock(Connection.class);
		expect(delegate.getConnection(user, pass)).andReturn(connection).times(2);
		expect(connection.getAutoCommit()).andReturn(false);
		connection.commit();
		expectLastCall();

		populator.populateDatabase(connection);
		expectLastCall();

		replay(connection, mockContext, populator, delegate);
		assertSame(connection, datasource.getConnection(user, pass));
		assertSame(connection, datasource.getConnection(user, pass));
		verify(connection, mockContext, populator, delegate);
	}


	@Test
	public void testGetConnection6b() throws SQLException, IOException, NamingException
	{
		datasource.setDelegate(delegate);
		datasource.setPopulator(populator);

		final String user = "username";
		final String pass = "password";

		Connection connection = createMock(Connection.class);
		expect(delegate.getConnection(user, pass)).andReturn(connection);
		expect(connection.getAutoCommit()).andReturn(true);
		connection.setAutoCommit(false);
		expectLastCall();
		connection.commit();
		expectLastCall();
		connection.setAutoCommit(true);
		expectLastCall();

		populator.populateDatabase(connection);
		expectLastCall();

		replay(connection, mockContext, populator, delegate);
		assertSame(connection, datasource.getConnection(user, pass));
		verify(connection, mockContext, populator, delegate);
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


	@Test
	public void testIsWrapperFor() throws SQLException
	{
		datasource.setDelegate(delegate);

		expect(delegate.isWrapperFor(Object.class)).andReturn(false);
		expect(delegate.isWrapperFor(Object.class)).andReturn(true);

		replay(mockContext, populator, delegate);
		assertFalse(datasource.isWrapperFor(Object.class));
		assertTrue(datasource.isWrapperFor(Object.class));
		verify(mockContext, populator, delegate);
	}


	@Test
	public void testUnwrap() throws SQLException
	{
		datasource.setDelegate(delegate);

		Object object = new Object();
		expect(delegate.unwrap(Object.class)).andReturn(object);

		replay(mockContext, populator, delegate);
		assertSame(object, datasource.unwrap(Object.class));
		verify(mockContext, populator, delegate);
	}
}
