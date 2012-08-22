package net.sf.opk.populator.sql;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import net.sf.opk.populator.DatabaseTestBase;
import net.sf.opk.populator.util.MavenPaths;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Arrays.asList;


public class DirectorySqlPopulatorTest extends DatabaseTestBase
{
	private static final File SQL_SOURCE_DIRECTORY = new File(MavenPaths.findSourcesDirectory(), "test/sql");
	private static final File SQL_TARGET_DIRECTORY = new File(MavenPaths.findTargetDirectory(), "test-sql");


	@BeforeClass
	public static void copySqlFiles() throws IOException
	{
		SQL_TARGET_DIRECTORY.mkdir();
		Path sourcePath = SQL_SOURCE_DIRECTORY.toPath();
		Path targetPath = SQL_TARGET_DIRECTORY.toPath();
		for (String fileName : asList("import1.sql", "import2.sql"))
		{
			Files.copy(sourcePath.resolve(fileName), targetPath.resolve(fileName), REPLACE_EXISTING);
		}
	}


	@Test(expected = SQLException.class)
	public void testWithMissingDirectory() throws IOException, SQLException
	{
		DirectorySqlPopulator populator = new DirectorySqlPopulator("missingDirectory");

		populator.populateDatabase(getConnectionForTest());

		checkRecordCount(0);
	}


	@Test(expected = SQLException.class)
	public void testWithFilePath() throws IOException, SQLException
	{
		String importFile = new File(SQL_TARGET_DIRECTORY, "import1.sql").getAbsolutePath();
		DirectorySqlPopulator populator = new DirectorySqlPopulator(importFile);

		populator.populateDatabase(getConnectionForTest());

		checkRecordCount(0);
	}


	@Test
	public void testWithGoodDirectory() throws IOException, SQLException
	{
		DirectorySqlPopulator populator = new DirectorySqlPopulator(SQL_TARGET_DIRECTORY.getAbsolutePath());
		populator.populateDatabase(getConnectionForTest());

		checkRecordCount(2);
	}
}
