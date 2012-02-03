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

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;


/**
 * An iterator that reads all SQL statements from an SQL file. Newlines are replaces by spaces, and whitespace around
 * statements are trimmed.
 *
 * @author <a href="mailto:oscar.westra@42.nl">Oscar Westra van Holthe - Kind</a>
 */
public class SqlStatementIterator implements Iterator<String>
{
	protected static final int BUFFER_SIZE = 1024;
	private static final Pattern NEWLINE_PATTERN = Pattern.compile("[\n\r]+");
	private static final String NEWLINE_REPLACEMENT = " ";
	private PushbackReader reader;
	private String nextSqlStatement;


	public SqlStatementIterator(Reader input)
	{
		reader = new PushbackReader(input, BUFFER_SIZE);
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

		char[] buffer = new char[BUFFER_SIZE]; // The buffer will be set to null if it is no longer needed.
		int charsRead = 0;
		//noinspection NestedAssignment
		while (buffer != null && (charsRead = reader.read(buffer)) > -1)
		{
			int firstSemiOrEnd = charIndexOrEnd(buffer, ';', 0, charsRead);
			statement.append(buffer, 0, firstSemiOrEnd);
			if (firstSemiOrEnd < charsRead)
			{
				reader.unread(buffer, firstSemiOrEnd + 1, charsRead - firstSemiOrEnd - 1);
				buffer = null;
			}
		}

		if (charsRead == -1)
		{
			// End of input reached. Any buffered characters are trailing characters; skip them.
			return null;
		}
		else
		{
			return NEWLINE_PATTERN.matcher(statement).replaceAll(NEWLINE_REPLACEMENT).trim();
		}
	}


	private int charIndexOrEnd(char[] array, char c, int startIndex, int endIndex)
	{
		for (int i = startIndex; i < endIndex; i++)
		{
			if (array[i] == c)
			{
				return i;
			}
		}
		return endIndex;
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
