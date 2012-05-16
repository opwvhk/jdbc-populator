/*
 * Copyright (c) 2012 Oscar Westra van Holthe - Kind
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package net.sf.opk.populator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.sf.opk.populator.sql.SqlPopulator;

import static javax.xml.xpath.XPathConstants.NODESET;
import static javax.xml.xpath.XPathConstants.STRING;


/**
 * A JDBC populator. For databases with only empty tables, it imports data by executing statements from a file named
 * <code>import.sql</code>.
 *
 * @author <a href="mailto:oscar@westravanholthe.nl">Oscar Westra van Holthe - Kind</a>
 * @author <a href="mailto:oscar@westravanholthe.nl">Oscar Westra van Holthe - Kind</a>
 */
@WebListener
public class ContextListener implements ServletContextListener
{
	/**
	 * Logger for this class.
	 */
	private static final Logger LOGGER = Logger.getLogger(ContextListener.class.getName());
	private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY;
	private static final XPath XPATH = XPathFactory.newInstance().newXPath();
	private static final XPathExpression PERSISTENCE_UNIT_PATH;
	private static final XPathExpression JTA_DATASOURCE_SUBPATH;
	private static final XPathExpression NON_JTA_DATASOURCE_SUBPATH;


	static
	{
		DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
		DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);

		PERSISTENCE_UNIT_PATH = compileXPathExpression("/*/*[name()='persistence-unit']");
		JTA_DATASOURCE_SUBPATH = compileXPathExpression("*[name()='jta-data-source']/text()");
		NON_JTA_DATASOURCE_SUBPATH = compileXPathExpression("*[name()='non-jta-data-source']/text()");
	}


	protected static XPathExpression compileXPathExpression(String expression)
	{
		try
		{
			return XPATH.compile(expression);
		}
		catch (XPathExpressionException e)
		{
			LOGGER.log(Level.SEVERE, "Failed to initialize an XPath expression. Expect NullPointerExceptions.", e);
			return null;
		}
	}


	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent)
	{
		try
		{
			String datasource = findDataSource();
			if (datasource == null)
			{
				LOGGER.info("No unambiguous datasource found; not populating the database.");
			}
			else
			{
				LOGGER.info(String.format("Found datasource %s to populate the database", datasource));
				JDBCPopulator populator = getJDBCPopulator();
				populateDatabase(datasource, populator);
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Something went wrong.", e);
		}
	}


	private JDBCPopulator getJDBCPopulator()
	{

		return new SqlPopulator() {
			@Override
			public void populateDatabase(Connection connection) throws SQLException
			{
				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
				InputStream sqlStream = classLoader.getResourceAsStream("import.sql");

				populateFromStream(sqlStream, connection);
			}
		};
	}


	private String findDataSource()
			throws IOException, URISyntaxException, SAXException, ParserConfigurationException, XPathExpressionException
	{
		Map<String, String> jtaDataSourcesByPersistenceUnits = new HashMap<String, String>();
		Map<String, String> nonJTADataSourcesByPersistenceUnits = new HashMap<String, String>();

		locateDataSources(jtaDataSourcesByPersistenceUnits, nonJTADataSourcesByPersistenceUnits);

		TreeSet<String> persistenceUnits = new TreeSet<String>();
		persistenceUnits.addAll(jtaDataSourcesByPersistenceUnits.keySet());
		persistenceUnits.addAll(nonJTADataSourcesByPersistenceUnits.keySet());
		int numberOfPersistenceUnits = persistenceUnits.size();

		String datasource = null;
		if (numberOfPersistenceUnits == 0)
		{
			LOGGER.log(Level.INFO, "Found no persistence units to populate.");
		}
		else if (numberOfPersistenceUnits > 1)
		{
			LOGGER.log(Level.INFO, String.format("Found %d persistence units %s. Not populating any of them.",
			                                     numberOfPersistenceUnits, persistenceUnits));
		}
		else
		{
			String persistenceUnit = persistenceUnits.first();
			String jtaDataSource = jtaDataSourcesByPersistenceUnits.get(persistenceUnit);
			String nonJTADataSource = nonJTADataSourcesByPersistenceUnits.get(persistenceUnit);
			LOGGER.info(String.format("Persistence unit '%s' has these datasources: %s and %s", persistenceUnit,
			                          jtaDataSource, nonJTADataSource));
			datasource = jtaDataSource;
			if (datasource == null)
			{
				datasource = nonJTADataSource;
			}
		}
		return datasource;
	}


	private void locateDataSources(Map<String, String> jtaDataSourcesByPersistenceUnit,
	                               Map<String, String> nonJTADataSourcesByPersistenceUnit)
			throws IOException, URISyntaxException, SAXException, ParserConfigurationException, XPathExpressionException
	{
		LOGGER.info("Listing datasources for persistence units.");
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Enumeration<URL> urlEnumeration = classLoader.getResources("META-INF/persistence.xml");

		Set<URI> locations = new HashSet<URI>();
		while (urlEnumeration.hasMoreElements())
		{
			locations.add(urlEnumeration.nextElement().toURI());
		}
		for (URI persistenceLocation : locations)
		{
			LOGGER.info("Reading datasource JNDI names from " + persistenceLocation.toString());
			Document persistenceXml = loadXml(persistenceLocation);
			NodeList persistenceUnits = (NodeList)PERSISTENCE_UNIT_PATH.evaluate(persistenceXml, NODESET);
			for (int i = 0; i < persistenceUnits.getLength(); i++)
			{
				Node persistenceUnit = persistenceUnits.item(i);
				String persistenceUnitName = persistenceUnit.getAttributes().getNamedItem("name").getNodeValue();
				LOGGER.info("Found a persistence unit: " + persistenceUnitName);

				String jtaDataSource = (String)JTA_DATASOURCE_SUBPATH.evaluate(persistenceUnit, STRING);
				if (!jtaDataSource.isEmpty())
				{
					LOGGER.info("Found a JTA DataSource: " + jtaDataSource);
					jtaDataSourcesByPersistenceUnit.put(persistenceUnitName, jtaDataSource);
				}
				String nonJTADataSource = (String)NON_JTA_DATASOURCE_SUBPATH.evaluate(persistenceUnit, STRING);
				if (!nonJTADataSource.isEmpty())
				{
					LOGGER.info("Found a non-JTA DataSource: " + nonJTADataSource);
					nonJTADataSourcesByPersistenceUnit.put(persistenceUnitName, nonJTADataSource);
				}
			}
		}
		LOGGER.info("Finished listing datasources for persistence units.");
	}


	private Document loadXml(URI location) throws ParserConfigurationException, IOException, SAXException
	{
		return DOCUMENT_BUILDER_FACTORY.newDocumentBuilder().parse(location.toString());
	}


	protected void populateDatabase(String jndiName, JDBCPopulator populator)
			throws NamingException, SQLException, IOException
	{
		Object datasource = new InitialContext().lookup(jndiName);
		if (datasource instanceof XADataSource)
		{
			populateDatabase((XADataSource)datasource, populator);
		}
		else
		{
			populateDatabase((DataSource)datasource, populator);
		}
	}


	private void populateDatabase(DataSource datasource, JDBCPopulator populator) throws SQLException, IOException
	{
		Connection connection = null;
		try
		{
			connection = datasource.getConnection();
			populator.populateDatabase(connection);
			connection.commit();
		}
		finally
		{
			if (connection != null)
			{
				connection.rollback(); // upon success, the data has already been committed.
				connection.close();
			}
		}
	}


	private void populateDatabase(XADataSource datasource, JDBCPopulator populator) throws SQLException, IOException
	{
		Connection connection = null;
		try
		{
			connection = datasource.getXAConnection().getConnection();
			populator.populateDatabase(connection);
			connection.commit();
		}
		finally
		{
			if (connection != null)
			{
				connection.rollback(); // upon success, the data has already been committed.
				connection.close();
			}
		}
	}


	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent)
	{
		// Nothing to do.
	}
}
