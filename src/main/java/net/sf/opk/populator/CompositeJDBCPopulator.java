package net.sf.opk.populator;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.naming.InitialContext;
import javax.naming.NamingException;


/**
 * A composite {@code JDBCPopulator}. Executes all added populators in order.
 *
 * @author <a href="mailto:oscar@westravanholthe.nl">Oscar Westra van Holthe - Kind</a>
 */
public class CompositeJDBCPopulator implements JDBCPopulator
{
	/**
	 * The JNDI names (space separated) of the {@link JDBCPopulator}s to delegate to.
	 */
	private String populatorNames;
	/**
	 * The populators to delegate to. May be empty if {@link #populatorNames} is set.
	 */
	private List<JDBCPopulator> populators = new ArrayList<JDBCPopulator>();


	@Override
	public void populateDatabase(Connection connection) throws SQLException, IOException
	{
		for (JDBCPopulator populator : getPopulators())
		{
			populator.populateDatabase(connection);
		}
	}


	/**
	 * Set the delegate of this data source to a data source loaded from JNDI.
	 *
	 * @param jndiName the name of the data source to delegate to
	 */
	public void setPopulatorNames(String jndiName)
	{
		populatorNames = jndiName;
	}


	/**
	 * Get the value of {@link #populators}, loading the JNDI entries named in {@link #populatorNames} if necessary.
	 *
	 * @return the populators
	 * @throws IllegalStateException when the populators cannot be found
	 */
	private List<JDBCPopulator> getPopulators()
	{
		if (populators.isEmpty())
		{
			try
			{
				populators = loadPopulators();
			}
			catch (NamingException e)
			{
				throw new IllegalStateException("Failed to load the data source.", e);
			}
		}
		return populators;
	}


	/**
	 * Load the populators from JNDI.
	 *
	 * @return the populators
	 * @throws NamingException when a populator cannot be found
	 */
	private List<JDBCPopulator> loadPopulators() throws NamingException
	{
		String[] jndiNames = populatorNames.split("\\s+");

		List<JDBCPopulator> result = new ArrayList<JDBCPopulator>();
		for (String jndiName : jndiNames)
		{
			result.add(loadPopulator(jndiName));
		}
		return result;
	}


	/**
	 * Load a populators from JNDI.
	 *
	 * @param jndiName the JNDI name of the populator to load
	 * @return the populator
	 * @throws NamingException when the populator cannot be found
	 */
	private JDBCPopulator loadPopulator(String jndiName) throws NamingException
	{
		Object datasource = new InitialContext().lookup(jndiName);
		if (datasource instanceof JDBCPopulator)
		{
			return (JDBCPopulator)datasource;
		}
		else
		{
			throw new NamingException(jndiName + " is not a " + JDBCPopulator.class.getName());
		}
	}


	public void addDelegate(JDBCPopulator populator)
	{
		populators.add(populator);
	}
}
