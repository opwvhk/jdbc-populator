package net.sf.opk.populator.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Logger;


/**
 * {@code JDBCPopulator} that reads SQL files from a directory in alphabetical order to import data with.
 *
 * @author <a href="mailto:oscar@westravanholthe.nl">Oscar Westra van Holthe - Kind</a>
 */
public class DirectorySqlPopulator extends SqlPopulator
{
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(DirectorySqlPopulator.class.getName());

	/**
	 * The name of the directory to read files with SQL statements from.
	 */
	private String directory;


	/**
	 * Create a DirectorySqlPopulator.
	 */
	public DirectorySqlPopulator()
	{
		// Nothing to do.
	}


	/**
	 * Create a DirectorySqlPopulator.
	 *
	 * @param directory the name of the directory to read files with SQL statements from
	 */
	public DirectorySqlPopulator(String directory)
	{
		this.directory = directory;
	}


	/**
	 * Set the name of the directory to read files with SQL statements from.
	 *
	 * @param directory the name of the directory to read files with SQL statements from
	 */
	public void setDirectoryName(String directory)
	{
		this.directory = directory;
	}


	@Override
	public void populateDatabase(Connection connection) throws SQLException, IOException
	{
		InputStream sqlStream = null;
		try
		{
			File directory = new File(this.directory);
			File[] files = directory.listFiles();
			if (files == null)
			{
				throw new SQLException("Cannot read SQL commands, not a directory: " + this.directory);
			}
			Collections.sort(Arrays.asList(files), new Comparator<File>()
			{
				@Override
				public int compare(File o1, File o2)
				{
					return o1.getName().compareTo(o2.getName());
				}
			});
			for (File file : files)
			{
				LOGGER.info("Populating database using " + file);
				sqlStream = new FileInputStream(file);
				populateFromStream(sqlStream, connection);
			}
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

