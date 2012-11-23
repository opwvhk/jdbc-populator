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
package net.sf.opk.populator.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


public class OnceIterableTest
{
	@Test(expected = NullPointerException.class)
	public void testMistake1() throws Exception
	{
		new OnceIterable<Void>(null);
	}


	@Test(expected = IllegalStateException.class)
	public void testMistake2() throws Exception
	{
		OnceIterable<String> iterable = new OnceIterable<String>(Arrays.asList("abc", "def", "ghi").iterator());
		assertNotNull(iterable.iterator());
		iterable.iterator();
	}


	@Test
	public void testWithForLoop() throws Exception
	{
		List<String> list = Arrays.asList("abc", "def", "ghi");

		Iterator<String> iterator = list.iterator();
		Iterable<String> iterable = new OnceIterable<String>(iterator);

		List<String> newList = new ArrayList<String>();
		for (String element : iterable)
		{
			newList.add(element);
		}
		assertEquals(list, newList);
	}
}
