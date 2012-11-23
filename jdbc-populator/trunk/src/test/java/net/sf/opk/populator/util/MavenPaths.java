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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;


/**
 * Utility class for Maven projects: locate the various directories.
 *
 * @author <a href="mailto:oscar@westravanholthe.nl">Oscar Westra van Holthe - Kind</a>
 */
public class MavenPaths
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
			URL location = MavenPaths.class.getProtectionDomain().getCodeSource().getLocation();
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
	private MavenPaths()
	{
		// Nothing to do.
	}
}
