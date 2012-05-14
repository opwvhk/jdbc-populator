package net.sf.opk.populator.sql;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;


/**
 * JNDI Factory for SQL based populators.
 *
 * @author <a href="mailto:oscar@westravanholthe.nl">Oscar Westra van Holthe - Kind</a>
 */
public class SqlPopulatorFactory implements ObjectFactory
{
	/**
	 * The file to populate the database with.
	 * Either this property or {@link #sqlDirectory} is must be specified (but not both).
	 */
	private String sqlFile;
	/**
	 * The file to populate the database with.
	 * Either this property or {@link #sqlFile} is must be specified (but not both).
	 */
	private String sqlDirectory;


	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
	{
		validateProperties();

		if (sqlFile == null)
		{
			return new DirectorySqlPopulator(sqlDirectory);
		}
		else
		{
			return new FileSqlPopulator(sqlFile);
		}
	}


	private void validateProperties()
	{
		if (sqlFile == null && sqlDirectory == null)
		{
			throw new IllegalStateException("Either SqlFile or SqlDirectory must be specified.");
		}
		if (sqlFile != null && sqlDirectory != null)
		{
			throw new IllegalStateException("Either SqlFile or SqlDirectory must be specified. Not both.");
		}
	}


	public String getSqlFile()
	{
		return sqlFile;
	}


	public void setSqlFile(String sqlFile)
	{
		this.sqlFile = sqlFile;
	}


	public String getSqlDirectory()
	{
		return sqlDirectory;
	}


	public void setSqlDirectory(String sqlDirectory)
	{
		this.sqlDirectory = sqlDirectory;
	}
}
