package net.sf.opk.populator;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;


/**
 * Utility class for Maven projects: locate the various directories.
 *
 * @author <a href="mailto:oscar@westravanholthe.nl">Oscar Westra van Holthe - Kind</a>
 */
public class MavenUtil
{
	public static File findSourcesDirectory()
	{
		return new File(findBaseDirectory(), "src");
	}


	public static File findBaseDirectory()
	{
		File targetDirectory = findTargetDirectory();
		return targetDirectory.getParentFile();
	}


	public static File findTargetDirectory()
	{
		File markerFile = findMarkerFile();
		File testClassesDirectory = markerFile.getParentFile();
		return testClassesDirectory.getParentFile();
	}


	private static File findMarkerFile()
	{
		try
		{
			URL markerUri = MavenUtil.class.getResource("/resourceToLocateSiblingDirectoryWith.txt");
			if (markerUri == null)
			{
				throw new IllegalStateException("Missing marker file.");
			}
			return new File(markerUri.toURI()).getAbsoluteFile();
		}
		catch (URISyntaxException e)
		{
			throw new IllegalStateException("Internal Java error: the classloader constructed an illegal URI.", e);
		}
	}


	/**
	 * Utility class: do not instantiate.
	 */
	private MavenUtil()
	{
		// Nothing to do.
	}
}
