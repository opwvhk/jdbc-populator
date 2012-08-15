package net.sf.opk.populator.sql;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import net.sf.opk.populator.DatabaseTestBase;
import net.sf.opk.populator.MavenUtil;
import org.junit.Test;


public class FileSqlPopulatorTest extends DatabaseTestBase
{
	private static final File SQL_DIRECTORY = new File(MavenUtil.findSourcesDirectory(), "test/sql");


	@Test(expected = SQLException.class)
	public void testWithMissingFile() throws IOException, SQLException
	{
		FileSqlPopulator populator = new FileSqlPopulator("missingFile");

		populator.populateDatabase(getConnectionForTest());

		checkRecordCount(0);
	}


	@Test
	public void testWithGoodFile() throws IOException, SQLException
	{
		String importPath = new File(SQL_DIRECTORY, "import1.sql").getAbsolutePath();

		FileSqlPopulator populator = new FileSqlPopulator(importPath);
		populator.populateDatabase(getConnectionForTest());

		checkRecordCount(1);
	}
}
