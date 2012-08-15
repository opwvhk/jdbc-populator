package net.sf.opk.populator.sql;

import java.util.Enumeration;
import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;


/**
 * JNDI Factory for SQL based populators.
 *
 * @author <a href="mailto:oscar@westravanholthe.nl">Oscar Westra van Holthe - Kind</a>
 */
public class SqlPopulatorFactory implements ObjectFactory
{
	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
	{
		// Customize the bean properties from our attributes
		Enumeration<RefAddr> attributes = ((Reference) obj).getAll();
		while (attributes.hasMoreElements())
		{
			RefAddr attribute = attributes.nextElement();
			String attributeName = attribute.getType();
			String attributeValue = (String) attribute.getContent();
			if ("sqlFile".equals(attributeName))
			{
				return new FileSqlPopulator(attributeValue);
			}
			else if ("sqlDirectory".equals(attributeName))
			{
				return new DirectorySqlPopulator(attributeValue);
			}
		}
		throw new IllegalStateException("Either sqlFile or sqlDirectory must be specified.");
	}
}
