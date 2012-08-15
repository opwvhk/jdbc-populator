package net.sf.opk.populator;

import java.io.IOException;
import java.sql.SQLException;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.CommonDataSource;
import javax.sql.XADataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class XADataSourceFactoryTest
{
	private static final String POPULATOR_NAME = "populatorJNDIName";
	private static final String DELEGATE_NAME = "delegateJNDIName";
	private XADataSource delegate;


	@Before
	public void initializeJNDI() throws NamingException
	{
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, DummyInitialContextFactory.class.getName());
		Context mockContext = createMock(Context.class);
		DummyInitialContextFactory.setMockContext(mockContext);

		JDBCPopulator populator = createMock(JDBCPopulator.class);
		expect(mockContext.lookup(POPULATOR_NAME)).andStubReturn(populator);

		delegate = createMock(XADataSource.class);
		expect(mockContext.lookup(DELEGATE_NAME)).andStubReturn(delegate);

		replay(mockContext);
	}


	@After
	public void restoreJNDI() throws Exception
	{
		System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
	}


	@Test(expected = IllegalStateException.class)
	public void testPropertyError1() throws SQLException, NamingException
	{
		XADataSourceFactory factory = new XADataSourceFactory();
		factory.getObjectInstance(null, null, null, null);
	}


	@Test(expected = IllegalStateException.class)
	public void testPropertyError2() throws SQLException, NamingException
	{
		XADataSourceFactory factory = new XADataSourceFactory();
		factory.setDelegate(DELEGATE_NAME);
		factory.getObjectInstance(null, null, null, null);
	}


	@Test(expected = IllegalStateException.class)
	public void testPropertyError3() throws SQLException, NamingException
	{
		XADataSourceFactory factory = new XADataSourceFactory();
		factory.setPopulator(POPULATOR_NAME);
		factory.getObjectInstance(null, null, null, null);
	}


	@Test
	public void testPropertyValues() throws SQLException
	{
		XADataSourceFactory factory = new XADataSourceFactory();
		factory.setDelegate(DELEGATE_NAME);
		factory.setPopulator(POPULATOR_NAME);

		assertEquals(DELEGATE_NAME, factory.getDelegate());
		assertEquals(POPULATOR_NAME, factory.getPopulator());
	}


	@Test
	public void testCreateObject() throws SQLException, IOException, NamingException
	{
		XADataSourceFactory factory = new XADataSourceFactory();
		factory.setDelegate(DELEGATE_NAME);
		factory.setPopulator(POPULATOR_NAME);

		expect(delegate.getLoginTimeout()).andReturn(1);

		replay(delegate);

		Object dataSource = factory.getObjectInstance(null, null, null, null);
		assertTrue(dataSource instanceof PopulatingXADataSource);
		assertEquals(1, ((CommonDataSource)dataSource).getLoginTimeout());
	}
}
