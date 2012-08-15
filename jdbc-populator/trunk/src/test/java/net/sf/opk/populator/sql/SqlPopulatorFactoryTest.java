package net.sf.opk.populator.sql;

import java.sql.SQLException;
import java.util.List;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.enumeration;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertTrue;


public class SqlPopulatorFactoryTest
{
	private static final RefAddr SQL_FILE_PROPERTY = new StringRefAddr("sqlFile", "randomPath");
	private static final RefAddr SQL_DIRECTORY_PROPERTY = new StringRefAddr("sqlDirectory", "randomPath");
	private static final RefAddr UNKNOWN_PROPERTY = new StringRefAddr("foo", "bar");


	@Test(expected = IllegalStateException.class)
	public void testPropertyError1() throws SQLException, NamingException
	{
		SqlPopulatorFactory factory = new SqlPopulatorFactory();
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
		SqlPopulatorFactory factory = new SqlPopulatorFactory();
		factory.getObjectInstance(createReference(UNKNOWN_PROPERTY), null, null, null);
	}


	@Test
	public void testCreateFileSqlPopulator() throws SQLException, NamingException
	{
		SqlPopulatorFactory factory = new SqlPopulatorFactory();
		Object instance = factory.getObjectInstance(createReference(SQL_FILE_PROPERTY), null, null, null);
		assertTrue(instance instanceof FileSqlPopulator);
	}


	@Test
	public void testCreateDirectorySqlPopulator() throws SQLException, NamingException
	{
		SqlPopulatorFactory factory = new SqlPopulatorFactory();
		Object instance = factory.getObjectInstance(createReference(SQL_DIRECTORY_PROPERTY), null, null, null);
		assertTrue(instance instanceof DirectorySqlPopulator);
	}
}
