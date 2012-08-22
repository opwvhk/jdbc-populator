package net.sf.opk.populator;

import java.util.Enumeration;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;


/**
 * JNDI Factory for PopulatingDataSource.
 *
 * @author <a href="mailto:oscar@westravanholthe.nl">Oscar Westra van Holthe - Kind</a>
 */
public class DataSourceFactory implements ObjectFactory
{
	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
			throws NamingException
	{
		String delegate = null;
		String populator = null;

		// Customize the bean properties from our attributes
		Enumeration<RefAddr> attributes = ((Reference)obj).getAll();
		while (attributes.hasMoreElements())
		{
			RefAddr attribute = attributes.nextElement();
			String attributeName = attribute.getType();
			String attributeValue = (String)attribute.getContent();
			if ("delegateName".equals(attributeName))
			{
				delegate = attributeValue;
			}
			else if ("populatorName".equals(attributeName))
			{
				populator = attributeValue;
			}
		}

		if (delegate == null || populator == null)
		{
			throw new IllegalStateException(
					"The properties 'delegateName' and 'populatorName' must both be specified.");
		}

		PopulatingDataSource dataSource = new PopulatingDataSource();
		dataSource.setDelegateName(delegate);
		dataSource.setPopulatorName(populator);
		return dataSource;
	}
}
