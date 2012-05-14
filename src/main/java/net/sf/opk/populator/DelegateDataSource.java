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
	 * The data source to delegate to, if it is a simple data source.
	 */
	private DataSource delegate;


	/**
	 * Set the delegate of this data source to a data source loaded from JNDI.
	 *
	 * @param jndiName the name of the data source to delegate to
	 * @throws NamingException when the data source cannot be found
	 */
	public void setDelegate(String jndiName) throws NamingException
	{

		Object datasource = new InitialContext().lookup(jndiName);
		if (datasource instanceof DataSource)
		{
			setDelegate((DataSource)datasource);
		}
		else
		{
			throw new NamingException(jndiName + " is not a " + DataSource.class.getName());
		}
	}


	public void setDelegate(DataSource delegate)
	{

		this.delegate = delegate;
	}


	@Override
	public Connection getConnection() throws SQLException
	{

		return delegate.getConnection();
	}


	@Override
	public Connection getConnection(String username, String password) throws SQLException
	{

		return delegate.getConnection(username, password);
	}


	@Override
	public PrintWriter getLogWriter() throws SQLException
	{

		return delegate.getLogWriter();
	}


	@Override
	public void setLogWriter(PrintWriter out) throws SQLException
	{

		delegate.setLogWriter(out);
	}


	@Override
	public void setLoginTimeout(int seconds) throws SQLException
	{

		delegate.setLoginTimeout(seconds);
	}


	@Override
	public int getLoginTimeout() throws SQLException
	{

		return delegate.getLoginTimeout();
	}


	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException
	{

		return delegate.getParentLogger();
	}


	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException
	{

		return delegate.unwrap(iface);
	}


	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{

		return delegate.isWrapperFor(iface);
	}
}
