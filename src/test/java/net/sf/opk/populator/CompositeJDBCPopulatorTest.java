/*
 * Copyright 2012 Oscar Westra van Holthe - Kind
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package net.sf.opk.populator;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.NamingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;


public class CompositeJDBCPopulatorTest
{
	private Context mockContext;
	private CompositeJDBCPopulator compositePopulator;
	private JDBCPopulator populator1;
	private JDBCPopulator populator2;
	private Connection mockConnection;
	private static final String POPULATOR1_JNDI_NAME = "POPULATOR1_JNDI_NAME";
	private static final String POPULATOR2_JNDI_NAME = "POPULATOR2_JNDI_NAME";


	@Before
	public void setUp() throws NamingException
	{
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, DummyInitialContextFactory.class.getName());
		mockContext = createMock(Context.class);
		DummyInitialContextFactory.setMockContext(mockContext);

		compositePopulator = new CompositeJDBCPopulator();
		compositePopulator.setPopulatorNames(POPULATOR1_JNDI_NAME + ' ' + POPULATOR2_JNDI_NAME);

		populator1 = createMock(JDBCPopulator.class);
		populator2 = createMock(JDBCPopulator.class);

		mockConnection = createMock(Connection.class);

		reset(mockContext, populator1, populator2, mockConnection);
	}


	@After
	public void restoreJNDI() throws Exception
	{
		System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
	}


	@Test(expected = IllegalStateException.class)
	public void testLookupFailure1() throws NamingException, IOException, SQLException
	{
		try
		{
			expect(mockContext.lookup(POPULATOR1_JNDI_NAME)).andThrow(new NamingException());
			replay(mockContext);

			compositePopulator.populateDatabase(mockConnection);
		}
		finally
		{
			verify(mockContext);
		}
	}


	@Test(expected = IllegalStateException.class)
	public void testLookupFailure2() throws NamingException, IOException, SQLException
	{
		try
		{
			expect(mockContext.lookup(POPULATOR1_JNDI_NAME)).andReturn(populator1);
			expect(mockContext.lookup(POPULATOR2_JNDI_NAME)).andReturn(new Object());
			replay(mockContext);

			compositePopulator.populateDatabase(mockConnection);
		}
		finally
		{
			verify(mockContext);
		}
	}


	@Test
	public void testPopulate1() throws NamingException, IOException, SQLException
	{
		expect(mockContext.lookup(POPULATOR1_JNDI_NAME)).andReturn(populator1).once();
		expect(mockContext.lookup(POPULATOR2_JNDI_NAME)).andReturn(populator2).once();

		populator1.populateDatabase(mockConnection);
		expectLastCall().once();
		populator2.populateDatabase(mockConnection);
		expectLastCall().once();

		replay(mockContext, populator1, populator2);

		compositePopulator.populateDatabase(mockConnection);

		verify(mockContext, populator1, populator2);
	}


	@Test
	public void testPopulate2() throws NamingException, IOException, SQLException
	{
		compositePopulator.addDelegate(populator1);
		compositePopulator.addDelegate(populator2);

		populator1.populateDatabase(mockConnection);
		populator2.populateDatabase(mockConnection);
		replay(populator1, populator2);

		compositePopulator.populateDatabase(mockConnection);

		verify(populator1, populator2);
	}
}
