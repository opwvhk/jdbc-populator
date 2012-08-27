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
package net.sf.opk.jdbc_populator.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * An iterator that reads all SQL statements from an SQL file. Newlines are replaces by spaces, and whitespace around
 * statements are trimmed.
 *
 * @author <a href="mailto:oscar.westra@42.nl">Oscar Westra van Holthe - Kind</a>
 */
public class SqlStatementIterator implements Iterator<String>
{
	private BufferedReader reader;
	private String nextSqlStatement;


	public SqlStatementIterator(Reader input)
	{
		reader = input instanceof BufferedReader ? reader : new BufferedReader(input);
		nextSqlStatement = null;
	}


	@Override
	public boolean hasNext()
	{
		if (nextSqlStatement == null)
		{
			try
			{
				nextSqlStatement = readSqlStatement();
			}
			catch (IOException ignored)
			{
				try
				{
					reader.close();
				}
				catch (IOException e)
				{
					// Failed to close the input. Ignoring; we cannot do anything about it.
				}
				finally
				{
					reader = null;
				}
			}
		}
		return nextSqlStatement != null;
	}


	private String readSqlStatement() throws IOException
	{
		StringBuilder statement = new StringBuilder();

		String line;
		//noinspection NestedAssignment
		while ((line = reader.readLine()) != null)
		{
			statement.append(' ');

			line = line.trim();
			if (line.endsWith(";"))
			{
				statement.append(line.substring(0, line.length() - 1));
				break;
			}
			else
			{
				statement.append(line);
			}
		}

		if (line == null)
		{
			// There was no semi-colon terminated statement.
			return null;
		}
		else
		{
			// Return the statement, skipping the initial space.
			return statement.substring(1);
		}
	}


	@Override
	public String next()
	{
		if (!hasNext())
		{
			throw new NoSuchElementException("There is no SQL statement to return.");
		}

		String result = nextSqlStatement;
		nextSqlStatement = null;
		return result;
	}


	@Override
	public void remove()
	{
		throw new UnsupportedOperationException("This iterator is read-only.");
	}
}