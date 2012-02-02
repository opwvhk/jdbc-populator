package nl.opk;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import static javax.xml.xpath.XPathConstants.NODESET;


/**
 * A JDBC initializer. For databases with only empty tables, it imports data by executing statements from a file
 * named <code>import.sql</code>.
 *
 * @author <a href="mailto:oscar.westra@42.nl">Oscar Westra van Holthe - Kind</a>
 */
public class JdbcInitializer implements ServletContextListener
{
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(JdbcInitializer.class.getName());


	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent)
	{
		LOGGER.info("Listing JNDI contents");
		try
		{
			listContext("java:module");
			listContext("java:app");
			listContext("java:global");
		}
		catch (NamingException e)
		{
			LOGGER.log(Level.WARNING, "Something went wrong.", e);
		}

		try
		{
			List<String> jtaDataSources = new ArrayList<String>();
			List<String> nonJTADataSources = new ArrayList<String>();

			XPath xpath = XPathFactory.newInstance().newXPath();
			XPathExpression jtaDataSourcePath = xpath.compile("/*/*/*[name()='jta-data-source']/text()");
			XPathExpression nonJTADataSourcePath = xpath.compile("/*/*/*[name()='non-jta-data-source']/text()");

			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true);
			DocumentBuilder builder = domFactory.newDocumentBuilder();

			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Enumeration<URL> persistenceLocations = classLoader.getResources("/META-INF/persistence.xml");
			LOGGER.info("Listing datasources for persistence units.");
			while (persistenceLocations.hasMoreElements())
			{
				URL persistenceLocation = persistenceLocations.nextElement();
				LOGGER.info("Reading datasource JNDI names from " + persistenceLocation.toString());
				Document persistenceXml = builder.parse(persistenceLocation.openStream());
				NodeList jta = (NodeList)jtaDataSourcePath.evaluate(persistenceXml, NODESET);
				for (int i = 0; i < jta.getLength(); i++)
				{
					String jndiName = jta.item(i).getTextContent();
					jtaDataSources.add(jndiName);
					LOGGER.info("JTA DataSource used in a persistence unit: " + jndiName);
				}
				NodeList nonJTA = (NodeList)nonJTADataSourcePath.evaluate(persistenceXml, NODESET);
				for (int i = 0; i < nonJTA.getLength(); i++)
				{
					String jndiName = nonJTA.item(i).getTextContent();
					nonJTADataSources.add(jndiName);
					LOGGER.info("non-JTA DataSource used in a persistence unit: " + jndiName);
				}
			}
			LOGGER.info("Finished listing datasources for persistence units.");
			int count = jtaDataSources.size() + nonJTADataSources.size();
			LOGGER.info(String.format("Found %d datasources.", count));
			if (count == 1)
			{
				String jndiName;
				String type;
				if (jtaDataSources.isEmpty())
				{
					type = "non-JTA";
					jndiName = nonJTADataSources.get(0);
				}
				else
				{
					type = "JTA";
					jndiName = jtaDataSources.get(0);
				}
				LOGGER.info(String.format("Using %s datasource: %s", type, jndiName));
				listObject(jndiName, new InitialContext().lookup(jndiName));
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Something went wrong.", e);
		}
	}


	protected void listContext(String name) throws NamingException
	{
		LOGGER.info(String.format("Listing contents of \"%s\":", name));
		listObject(name, new InitialContext());
	}


	protected void listObject(String fullName, Object object)
	{
		String objectType = object.getClass().getName();
		String msgFormat;
		if (object instanceof Context)
		{
			msgFormat = "Found a javax.naming.Context at   %s: %s";
		}
		else if (object instanceof DataSource)
		{
			msgFormat = "Found a javax.sql.DataSource at   %s: %s";
		}
		else if (object instanceof XADataSource)
		{
			msgFormat = "Found a javax.sql.XADataSource at %s: %s";
		}
		else
		{
			msgFormat = "Nothing useful at                 %s: %s";
		}
		LOGGER.info(String.format(msgFormat, fullName, objectType));

		if (object instanceof Context)
		{
			listContext(fullName, (Context)object);
		}
	}


	protected void listContext(String prefix, Context context)
	{
		NamingEnumeration<Binding> bindings = null;
		try
		{
			bindings = context.listBindings("");
		}
		catch (NamingException e)
		{
			LOGGER.log(Level.FINE, "Failed to list the context at " + prefix, e);
			LOGGER.log(Level.WARNING, "Failed to list the context at " + prefix);
		}
		if (bindings != null)
		{
			while (bindings.hasMoreElements())
			{
				Binding binding = bindings.nextElement();
				String name = binding.getName();
				String fullName = prefix.length() == 0 ? name : prefix + '/' + name;
				if (name.length() == 0)
				{
					continue;
				}
				Object object;
				try
				{
					object = context.lookup(name);
					listObject(fullName, object);
				}
				catch (NamingException e)
				{
					LOGGER.log(Level.FINE, "Failed to lookup " + fullName, e);
					LOGGER.log(Level.WARNING, "Failed to lookup " + fullName);
				}
			}
		}
	}


	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent)
	{
		// Nothing to do.
	}
}
