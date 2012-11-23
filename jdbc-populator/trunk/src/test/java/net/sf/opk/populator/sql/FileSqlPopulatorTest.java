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
package net.sf.opk.populator.sql;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
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
		FileSqlPopulator populator = new FileSqlPopulator();
		populator.setFileName("missingFile");

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
		FileSqlPopulator populator = new FileSqlPopulator();

		populator.populateFromStream(null, getConnectionForTest());

		checkRecordCount(0);
	}


	@Test
	public void testInternalsWithFailingStream() throws IOException, SQLException
	{
		FileSqlPopulator populator = new FileSqlPopulator();

		InputStream stream = createMock(InputStream.class);
		expect(stream.read(anyObject(byte[].class), anyInt(), anyInt())).andThrow(new IOException("oops")).once();
		stream.close();
		expectLastCall().once();

		replay(stream);

		populator.populateFromStream(stream, getConnectionForTest());
	}


	@Test(expected = SQLException.class)
	public void testInternalsWithFailingConnection() throws IOException, SQLException
	{
		FileSqlPopulator populator = new FileSqlPopulator();

		InputStream stream = createMock(InputStream.class);
		Connection connection = createMock(Connection.class);
		expect(connection.createStatement()).andThrow(new SQLException("oops")).once();

		replay(stream, connection);

		populator.populateFromStream(stream, connection);
	}
}
