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
