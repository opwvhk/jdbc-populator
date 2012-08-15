package net.sf.opk.populator;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;


/**
 * JNDI Factory for PopulatingXADataSource.
 *
 * @author <a href="mailto:oscar@westravanholthe.nl">Oscar Westra van Holthe - Kind</a>
 */
public class XADataSourceFactory implements ObjectFactory
{
	/**
	 * The JNDI name of the XADataSource delegate to use.
	 */
	private String delegate;
	/**
	 * The JNDI name of the populator to use.
	 */
	private String populator;


	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws NamingException
	{
		validateProperties();

		PopulatingXADataSource dataSource = new PopulatingXADataSource();
		dataSource.setDelegate(delegate);
		dataSource.setPopulator(populator);
		return dataSource;
	}


	private void validateProperties()
	{
		if (delegate == null || populator == null)
		{
			throw new IllegalStateException("The properties 'delegate' and 'populator' must both be specified.");
		}
	}


	public String getDelegate()
	{
		return delegate;
	}


	public void setDelegate(String delegate)
	{
		this.delegate = delegate;
	}


	public String getPopulator()
	{
		return populator;
	}


	public void setPopulator(String populator)
	{
		this.populator = populator;
	}
}
