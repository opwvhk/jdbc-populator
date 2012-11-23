/*
 * Copyright 2012 Oscar Westra van Holthe - Kind
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.
 *
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations under the
 * License.
 */
package net.sf.opk.populator.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.NoSuchElementException;

import org.junit.Test;

import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class SqlStatementIteratorTest
{
	private static final String STATEMENT1a = "insert into table1 (col1, col2, col3) values";
	private static final String STATEMENT1b = "(1, 'value 2', 'value 3')";
	private static final String STATEMENT2 = "insert into table2 (col) values (2)";
	private static final String TRAILING = "foo";
	private static final String SQL_STREAM1 =
			STATEMENT1a + "\n" + STATEMENT1b + ";\n" + STATEMENT2 + ";\n\n" + TRAILING;
	private static final String SQL_STREAM2 = STATEMENT1a + "\n" + STATEMENT1b + ";\n" + STATEMENT2 + ";\n\n";
	private SqlStatementIterator iterator;


	@Test(expected = UnsupportedOperationException.class)
	public void testRemove() throws Exception
	{
		iterator = new SqlStatementIterator(new BufferedReader(new StringReader(SQL_STREAM1)));
		iterator.remove();
	}


	@Test
	public void testWithIOErrors1() throws Exception
	{
		Reader faultyReader = createMock(Reader.class);
		expect(faultyReader.read(anyObject(char[].class), anyInt(), anyInt())).andStubThrow(new IOException());
		faultyReader.close();
		expectLastCall().asStub();

		replay(faultyReader);

		iterator = new SqlStatementIterator(faultyReader);
		assertFalse(iterator.hasNext());

		verify(faultyReader);
	}


	@Test
	public void testWithIOErrors2() throws Exception
	{
		Reader faultyReader = createMock(Reader.class);
		expect(faultyReader.read(anyObject(char[].class), anyInt(), anyInt())).andStubThrow(new IOException());
		faultyReader.close();
		expectLastCall().andStubThrow(new IOException());

		replay(faultyReader);

		iterator = new SqlStatementIterator(faultyReader);
		assertFalse(iterator.hasNext());

		verify(faultyReader);
	}


	@Test
	public void testResults1() throws Exception
	{
		iterator = new SqlStatementIterator(new StringReader(SQL_STREAM1));
		assertTrue(iterator.hasNext());
		assertEquals(STATEMENT1a + ' ' + STATEMENT1b, iterator.next());
		assertEquals(STATEMENT2, iterator.next());
		assertFalse(iterator.hasNext());
		try
		{
			iterator.next();
			fail("Got an element, but hasNext() returned false...");
		}
		catch (NoSuchElementException e)
		{
			// Ok.
		}
	}


	@Test
	public void testResults2() throws Exception
	{
		iterator = new SqlStatementIterator(new StringReader(SQL_STREAM2));
		assertTrue(iterator.hasNext());
		assertEquals(STATEMENT1a + ' ' + STATEMENT1b, iterator.next());
		assertEquals(STATEMENT2, iterator.next());
		assertFalse(iterator.hasNext());
		try
		{
			iterator.next();
			fail("Got an element, but hasNext() returned false...");
		}
		catch (NoSuchElementException e)
		{
			// Ok.
		}
	}
}
