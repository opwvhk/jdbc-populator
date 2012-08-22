package net.sf.opk.populator.sql;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import org.junit.Test;

import net.sf.opk.populator.DatabaseTestBase;
import net.sf.opk.populator.util.MavenPaths;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;


public class FileSqlPopulatorTest extends DatabaseTestBase
{
	private static final File SQL_DIRECTORY = new File(MavenPaths.findSourcesDirectory(), "test/sql");


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


	@Test
	public void testInternalsWithNullStream() throws IOException, SQLException
	{
		FileSqlPopulator populator = new FileSqlPopulator("anything");

		populator.populateFromStream(null, getConnectionForTest());

		checkRecordCount(0);
	}


	@Test
	public void testInternalsWithFailingStream() throws IOException, SQLException
	{
		FileSqlPopulator populator = new FileSqlPopulator("anything");

		InputStream stream = createMock(InputStream.class);
		expect(stream.read(anyObject(byte[].class), anyInt(), anyInt())).andThrow(new IOException("oops")).once();
		stream.close();
		expectLastCall().once();

		replay(stream);

		populator.populateFromStream(stream, getConnectionForTest());
	}

}
