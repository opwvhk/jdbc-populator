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
		File testClassesDirectory = findClasspathLocation();
		return testClassesDirectory.getParentFile();
	}


	private static File findClasspathLocation()
	{
		try
		{
			URL location = MavenUtil.class.getProtectionDomain().getCodeSource().getLocation();
			return new File(location.toURI()).getAbsoluteFile();
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
