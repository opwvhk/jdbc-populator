package net.sf.opk.populator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.CommonDataSource;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.enumeration;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class DataSourceFactoryTest
{
	private static final String POPULATOR_NAME = "populatorJNDIName";
	private static final RefAddr POPULATOR_PROPERTY = new StringRefAddr("populatorName", "populatorJNDIName");
	private static final String DELEGATE_NAME = "delegateJNDIName";
	private static final RefAddr DELEGATE_PROPERTY = new StringRefAddr("delegateName", "delegateJNDIName");
	private static final RefAddr UNKNOWN_PROPERTY = new StringRefAddr("foo", "bar");
	private DataSource delegate;


	@Before
	public void initializeJNDI() throws NamingException
	{
		System.setProperty(Context.INITIAL_CONTEXT_FACTORY, DummyInitialContextFactory.class.getName());
		Context mockContext = createMock(Context.class);
		DummyInitialContextFactory.setMockContext(mockContext);

		JDBCPopulator populator = createMock(JDBCPopulator.class);
		expect(mockContext.lookup(POPULATOR_NAME)).andStubReturn(populator);

		delegate = createMock(DataSource.class);
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
		DataSourceFactory factory = new DataSourceFactory();
		factory.getObjectInstance(createReference(), null, null, null);
	}


	private Reference createReference(RefAddr... properties)
	{
		List<RefAddr> refAddrs = asList(properties);

		Reference reference = createMock(Reference.class);
		expect(reference.getAll()).andStubReturn(enumeration(refAddrs));
		replay(reference);
		return reference;
	}


	@Test(expected = IllegalStateException.class)
	public void testPropertyError2() throws SQLException, NamingException
	{
		DataSourceFactory factory = new DataSourceFactory();
		factory.getObjectInstance(createReference(UNKNOWN_PROPERTY), null, null, null);
	}


	@Test(expected = IllegalStateException.class)
	public void testPropertyError3() throws SQLException, NamingException
	{
		DataSourceFactory factory = new DataSourceFactory();
		factory.getObjectInstance(createReference(DELEGATE_PROPERTY), null, null, null);
	}


	@Test(expected = IllegalStateException.class)
	public void testPropertyError4() throws SQLException, NamingException
	{
		DataSourceFactory factory = new DataSourceFactory();
		factory.getObjectInstance(createReference(POPULATOR_PROPERTY, UNKNOWN_PROPERTY), null, null, null);
	}


	@Test
	public void testPropertyValues() throws SQLException, NamingException
	{
		DataSourceFactory factory = new DataSourceFactory();
		factory.getObjectInstance(createReference(DELEGATE_PROPERTY, POPULATOR_PROPERTY), null, null, null);
	}


	@Test
	public void testCreateObject() throws SQLException, IOException, NamingException
	{
		expect(delegate.getLoginTimeout()).andReturn(1);
		replay(delegate);

		DataSourceFactory factory = new DataSourceFactory();
		Reference reference = createReference(DELEGATE_PROPERTY, POPULATOR_PROPERTY);
		Object dataSource = factory.getObjectInstance(reference, null, null, null);

		assertTrue(dataSource instanceof PopulatingDataSource);
		assertEquals(1, ((CommonDataSource)dataSource).getLoginTimeout());
	}
}
