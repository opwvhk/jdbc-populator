package net.sf.opk.populator.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;


public class SkipCommentsReaderTest
{
	private String textWithComments;
	private String textWithoutComments;
	private String commentStart;


	@Before
	public void setUp() throws Exception
	{
		commentStart = "--";

		StringWriter withComments = new StringWriter();
		StringWriter withoutComments = new StringWriter();

		PrintWriter writerC = new PrintWriter(withComments, true);
		PrintWriter writerN = new PrintWriter(withoutComments, true);

		writerC.println("A line of text without comments.");
		writerN.println("A line of text without comments.");
		writerC.println();
		writerN.println();
		writerC.print("A line ending in a comment.");
		writerN.print("A line ending in a comment.");
		writerC.print(commentStart + " a comment");
		writerC.println();
		writerN.println();
		writerC.print("    ");
		writerN.print("    ");
		writerC.print(commentStart + " another comment");
		writerC.println();
		writerN.println();
		writerC.println(commentStart + " A full line comment.");
		writerN.println();
		writerC.println("A line of text without comments.");
		writerN.println("A line of text without comments.");

		//writerC.close(); // Also flushes to the underlying writer.
		//writerN.close();
		textWithComments = withComments.toString();
		textWithoutComments = withoutComments.toString();
	}


	@Test(expected = NullPointerException.class)
	public void testSillyInput1() throws IOException
	{
		new SkipCommentsReader(null, "--");
	}


	@Test(expected = NullPointerException.class)
	public void testSillyInput2() throws IOException
	{
		new SkipCommentsReader(new StringReader(""), null);
	}


	@Test
	public void testReading1() throws IOException
	{
		StringReader source = new StringReader(textWithComments);
		SkipCommentsReader reader = new SkipCommentsReader(source, commentStart);

		char[] buffer = new char[textWithComments.length()];
		assertEquals(textWithoutComments.length(), reader.read(buffer));
		assertEquals(-1, reader.read());
		reader.close();
		assertEquals(textWithoutComments, String.valueOf(buffer, 0, textWithoutComments.length()));
	}


	@Test
	public void testReading2() throws IOException
	{
		StringReader source = new StringReader(textWithComments);
		SkipCommentsReader reader = new SkipCommentsReader(source, commentStart);

		char[] buffer = new char[textWithoutComments.length()];
		assertEquals(textWithoutComments.length(), reader.read(buffer));
		assertEquals(-1, reader.read());
		reader.close();
		assertEquals(textWithoutComments, String.valueOf(buffer, 0, textWithoutComments.length()));
	}
}
