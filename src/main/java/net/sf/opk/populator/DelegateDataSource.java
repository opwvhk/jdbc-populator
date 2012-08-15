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

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;


/**
 * A data source that delegates all method calls to a data source obtained via JNDI. It can delegate to both XA and
 * non-XA data sources. Calling methods exclusive to the type not currently being delegated to will yield an {@link
 * IllegalStateException}.
 *
 * @author <a href="mailto:oscar@westravanholthe.nl">Oscar Westra van Holthe - Kind</a>
 */
public class DelegateDataSource implements DataSource
{
	/**
	 * The JNDI name of the data source to delegate to.
	 */
	private String delegateName;
	/**
	 * The data source to delegate to. May be {@literal null} if {@link #delegateName} is set.
	 */
	private DataSource delegate;


	/**
	 * Set the delegate of this data source to a data source loaded from JNDI.
	 *
	 * @param jndiName the name of the data source to delegate to
	 */
	public void setDelegate(String jndiName)
	{
		delegateName = jndiName;
	}


	/**
	 * Get the value of {@lilnk #delegate}, loading the JNDI entry named {@link #delegateName} if necessary.
	 *
	 * @return the data source
	 * @throws IllegalStateException when the data source cannot be found
	 */
	private DataSource getDelegate()
	{
		if (delegate == null)
		{
			try
			{
				delegate = loadDataSource();
			}
			catch (NamingException e)
			{
				throw new IllegalStateException("Failed to load the data source.", e);
			}
		}
		return delegate;
	}


	/**
	 * Load the data source from JNDI.
	 *
	 * @return the data source
	 * @throws NamingException when the data source cannot be found
	 */
	private DataSource loadDataSource() throws NamingException
	{

		Object datasource = new InitialContext().lookup(delegateName);
		if (datasource instanceof DataSource)
		{
			return (DataSource)datasource;
		}
		else
		{
			throw new NamingException(delegateName + " is not a " + DataSource.class.getName());
		}
	}


	public void setDelegate(DataSource delegate)
	{

		this.delegate = delegate;
	}


	@Override
	public Connection getConnection() throws SQLException
	{

		return getDelegate().getConnection();
	}


	@Override
	public Connection getConnection(String username, String password) throws SQLException
	{

		return getDelegate().getConnection(username, password);
	}


	@Override
	public PrintWriter getLogWriter() throws SQLException
	{

		return getDelegate().getLogWriter();
	}


	@Override
	public void setLogWriter(PrintWriter out) throws SQLException
	{

		getDelegate().setLogWriter(out);
	}


	@Override
	public void setLoginTimeout(int seconds) throws SQLException
	{

		getDelegate().setLoginTimeout(seconds);
	}


	@Override
	public int getLoginTimeout() throws SQLException
	{

		return getDelegate().getLoginTimeout();
	}


	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException
	{

		return getDelegate().getParentLogger();
	}


	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException
	{

		return getDelegate().unwrap(iface);
	}


	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{

		return getDelegate().isWrapperFor(iface);
	}
}
