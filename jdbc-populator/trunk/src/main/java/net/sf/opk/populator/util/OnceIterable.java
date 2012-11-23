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

import java.util.Iterator;


/**
 * An {@code Iterable} that can be iterated once.
 *
 * @author <a href="mailto:oscar@westravanholthe.nl">Oscar Westra van Holthe - Kind</a>
 */
public class OnceIterable<T> implements Iterable<T>
{
	/**
	 * The iterator to return.
	 */
	private Iterator<T> iterator;


	/**
	 * Create an iterable that can be iterated once.
	 *
	 * @param iterator the iterator to return
	 */
	public OnceIterable(Iterator<T> iterator)
	{
		if (iterator == null)
		{
			throw new NullPointerException();
		}
		this.iterator = iterator;
	}


	/**
	 * Returns the iterator this object was created with on the first invocation. Subsequent invications throw an
	 * exception.
	 *
	 * @return on the first call, the iterator this object was created with
	 * @throws IllegalStateException on the second and subsequent calls
	 */
	@Override
	public Iterator<T> iterator()
	{
		if (iterator == null)
		{
			throw new IllegalStateException("This object has already been iterated.");
		}

		Iterator<T> result = iterator;
		iterator = null;
		return result;
	}
}
