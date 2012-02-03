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


/**
 * A reader that skips any single-comments in the input, converting line ends into newlines. The comment delimiter is
 * configurable.
 *
 * @author <a href="mailto:oscar.westra@42.nl">Oscar Westra van Holthe - Kind</a>
 */
public class SkipCommentsReader extends Reader
{
	/**
	 * The underlying reader.
	 */
	private BufferedReader reader;
	/**
	 * The comment start delimiter.
	 */
	private String commentStart;
	/**
	 * The buffer used. Will be set to null when the end of the input is reached.
	 */
	private StringBuffer buffer;


	/**
	 * Create a reader that skips comments.
	 *
	 * @param reader       the reader to wrap
	 * @param commentStart the comment start delimiter
	 */
	public SkipCommentsReader(Reader reader, String commentStart)
	{
		if (commentStart == null)
		{
			throw new NullPointerException("You must provide a comment delimiter.");
		}

		this.commentStart = commentStart;
		this.reader = new BufferedReader(reader);
		buffer = new StringBuffer();
	}


	@Override
	public int read(char[] cbuf, int off, int len) throws IOException
	{
		int totalCharsRead = -1;
		while (fillBuffer())
		{
			int lastCharsRead = emptyBuffer(cbuf, off, len);
			totalCharsRead += lastCharsRead;
			off += lastCharsRead;
			len -= lastCharsRead;
		}
		if (totalCharsRead > -1)
		{
			// Due to the initial value (used to detect the end of input), totalCharsRead is one too small.
			totalCharsRead++;
		}
		return totalCharsRead;
	}


	/**
	 * Fill the buffer. If the end of the input from the underlying reader is reached, the buffer will be null.
	 * Otherwise, the buffer will contain at least one character.
	 *
	 * @return {@literal true} as long as the end of the input has not been reached; {@literal false} otherwise
	 * @throws IOException when reading from the underlying reader fails
	 */
	private boolean fillBuffer() throws IOException
	{
		while (buffer != null && buffer.length() == 0)
		{
			String lineRead = reader.readLine();
			if (lineRead == null)
			{
				buffer = null;
			}
			else
			{
				int skipIndex = lineRead.indexOf(commentStart);
				if (skipIndex != -1)
				{
					buffer.append(lineRead.substring(0, skipIndex));
				}
				else
				{
					buffer.append(lineRead);
				}
				buffer.append('\n');
			}
		}
		return buffer != null;
	}


	/**
	 * Empty the buffer into the destination array. Assumes that the last call to {@link #fillBuffer()} did not return
	 * {@literal false}.
	 *
	 * @param destination the destination character array
	 * @param offset      the offset in the array to start writing characters to
	 * @param length      the maximum number of characters to write
	 * @return the number of characters written to the array
	 */
	private int emptyBuffer(char[] destination, int offset, int length)
	{
		int numberOfCharactersToCopy = Math.min(length, buffer.length());

		String usedPart = buffer.substring(0, numberOfCharactersToCopy);
		buffer.delete(0, numberOfCharactersToCopy);

		usedPart.getChars(0, numberOfCharactersToCopy, destination, offset);

		return numberOfCharactersToCopy;
	}


	@Override
	public void close() throws IOException
	{
		reader.close();
	}
}
