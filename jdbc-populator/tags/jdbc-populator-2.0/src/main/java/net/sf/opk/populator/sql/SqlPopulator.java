package net.sf.opk.populator.sql;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import net.sf.opk.populator.JDBCPopulator;
import net.sf.opk.populator.util.OnceIterable;
import net.sf.opk.populator.util.SkipCommentsReader;


/**
 * {@code JDBCPopulator} that reads a fixed SQL file to import data with.
 *
 * @author <a href="mailto:oscar@westravanholthe.nl">Oscar Westra van Holthe - Kind</a>
 */
public abstract class SqlPopulator implements JDBCPopulator
{
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(SqlPopulator.class.getName());


	/**
	 * Populate the connection from an SQL stream.
	 *
	 * @param sqlStream the SQL stream to read commands from
	 * @param connection the connection to write the SQL commands to
	 * @throws SQLException when an SQL command fails
	 */
	protected void populateFromStream(InputStream sqlStream, Connection connection) throws SQLException
	{
		if (sqlStream == null)
		{
			return;
		}

		Statement statement = null;
		try
		{
			statement = connection.createStatement();

			InputStreamReader sqlReader = new InputStreamReader(sqlStream, Charset.forName("UTF-8"));
			SqlStatementIterator statementIterator = new SqlStatementIterator(new SkipCommentsReader(sqlReader, "--"));
			for (String sqlStatement : new OnceIterable<String>(statementIterator))
			{
				LOGGER.finer(String.format("Executing SQL: %s", sqlStatement));
				statement.execute(sqlStatement);
			}
		}
		finally
		{
			if (statement != null)
			{
				statement.close();
			}
		}
	}
}
