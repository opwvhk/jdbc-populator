package net.sf.opk.populator.sql;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;

import org.junit.Test;

import net.sf.opk.populator.DatabaseTestBase;
import net.sf.opk.populator.JDBCPopulator;
import net.sf.opk.populator.MavenUtil;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class SqlPopulatorFactoryTest extends DatabaseTestBase
{
	private final String EXISTING_SQL_FILE = MavenUtil.findSourcesDirectory() + "/test/classpaths/importOk/import.sql";


	@Test(expected = IllegalStateException.class)
	public void testPropertyError1() throws SQLException
	{
		SqlPopulatorFactory factory = new SqlPopulatorFactory();
		factory.getObjectInstance(null, null, null, null);
	}


	@Test(expected = IllegalStateException.class)
	public void testPropertyError2() throws SQLException
	{
		SqlPopulatorFactory factory = new SqlPopulatorFactory();
		factory.setSqlFile("foo");
		factory.setSqlDirectory("foo");
		factory.getObjectInstance(null, null, null, null);
	}


	@Test
	public void testPropertyValues() throws SQLException
	{
		String fileName = "fileName";
		String directory = "directory";

		SqlPopulatorFactory factory = new SqlPopulatorFactory();
		factory.setSqlFile(fileName);
		factory.setSqlDirectory(directory);

		assertEquals(fileName, factory.getSqlFile());
		assertEquals(directory, factory.getSqlDirectory());
	}


	@Test
	public void testFileSqlPopulator1() throws SQLException, IOException
	{
		SqlPopulatorFactory factory = new SqlPopulatorFactory();
		factory.setSqlFile(EXISTING_SQL_FILE);

		Object populator = factory.getObjectInstance(null, null, null, null);
		assertTrue(populator instanceof JDBCPopulator);
		assertTrue(populator instanceof FileSqlPopulator);

		((JDBCPopulator)populator).populateDatabase(connectionForTest);
		checkRecordCount(1L);
	}


	@Test(expected = SQLException.class)
	public void testFileSqlPopulator2() throws SQLException, IOException
	{
		SqlPopulatorFactory factory = new SqlPopulatorFactory();
		factory.setSqlFile(EXISTING_SQL_FILE + ".bak");

		Object populator = factory.getObjectInstance(null, null, null, null);
		assertTrue(populator instanceof JDBCPopulator);
		assertTrue(populator instanceof FileSqlPopulator);

		((JDBCPopulator)populator).populateDatabase(connectionForTest);
	}


	@Test
	public void testDirSqlPopulator1() throws SQLException, IOException
	{
		File tempDir = new File(MavenUtil.findTargetDirectory() + "/testSqlFiles");
		tempDir.mkdir();
		Files.copy(new File(EXISTING_SQL_FILE).toPath(), new File(tempDir, "import1.sql").toPath(), REPLACE_EXISTING);
		Files.copy(new ByteArrayInputStream(new byte[0]), new File(tempDir, "import2.sql").toPath(), REPLACE_EXISTING);

		SqlPopulatorFactory factory = new SqlPopulatorFactory();
		factory.setSqlDirectory(tempDir.getAbsolutePath());

		Object populator = factory.getObjectInstance(null, null, null, null);
		assertTrue(populator instanceof JDBCPopulator);
		assertTrue(populator instanceof DirectorySqlPopulator);

		((JDBCPopulator)populator).populateDatabase(connectionForTest);
		checkRecordCount(1L);
	}


	@Test(expected = SQLException.class)
	public void testDirSqlPopulator2() throws SQLException, IOException
	{
		SqlPopulatorFactory factory = new SqlPopulatorFactory();
		factory.setSqlDirectory(EXISTING_SQL_FILE + "/foo");

		Object populator = factory.getObjectInstance(null, null, null, null);
		assertTrue(populator instanceof JDBCPopulator);
		assertTrue(populator instanceof DirectorySqlPopulator);

		((JDBCPopulator)populator).populateDatabase(connectionForTest);
	}
}
