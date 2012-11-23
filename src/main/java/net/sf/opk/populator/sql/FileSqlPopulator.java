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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;


/**
 * {@code JDBCPopulator} that reads an SQL file to import data with.
 *
 * @author <a href="mailto:oscar@westravanholthe.nl">Oscar Westra van Holthe - Kind</a>
 */
public class FileSqlPopulator extends SqlPopulator
{
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(FileSqlPopulator.class.getName());

	/**
	 * The name of the file to read SQL statements from.
	 */
	private String fileName;

	/**
	 * Create a FileSqlPopulator.
	 */
	public FileSqlPopulator()
	{
		// Nothing to do.
	}


	/**
	 * Create a FileSqlPopulator.
	 *
	 * @param fileName the name of the file to read SQL statements from
	 */
	public FileSqlPopulator(String fileName)
	{
		this.fileName = fileName;
	}


	/**
	 * Set the name of the file to read SQL statements from.
	 *
	 * @param fileName the name of the file to read SQL statements from
	 */
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}


	@Override
	public void populateDatabase(Connection connection) throws SQLException, IOException
	{
		InputStream sqlStream = null;
		try
		{
			sqlStream = new FileInputStream(fileName);
			LOGGER.info("Populating database using " + fileName);
			populateFromStream(sqlStream, connection);
		}
		catch (FileNotFoundException e)
		{
			throw new SQLException("Failed to read SQL commands.", e);
		}
		finally
		{
			if (sqlStream != null)
			{
				sqlStream.close();
			}
		}
	}
}
