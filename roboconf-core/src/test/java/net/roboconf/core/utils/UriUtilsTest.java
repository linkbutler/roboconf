/**
 * Copyright 2014 Linagora, Université Joseph Fourier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.roboconf.core.utils;

import java.net.URI;
import java.net.URL;

import junit.framework.Assert;
import net.roboconf.core.utils.UriUtils;

import org.junit.Test;

/**
 * @author Vincent Zurczak - Linagora
 */
public class UriUtilsTest {

	@Test
	public void testUrlToUri_1() throws Exception {
		URL url = new URL( "http://roboconf.net" );
		Assert.assertEquals( url.toURI(), UriUtils.urlToUri( url ) );
	}


	@Test
	public void testUrlToUri_2() throws Exception {
		URL url = new URL( "http://url.com/some%20folder" );
		Assert.assertEquals( url.toURI(), UriUtils.urlToUri( url ) );
	}


	@Test
	public void testUrlToUri_3() throws Exception {
		URL url = new URL( "http://url.com/some folder" );
		Assert.assertEquals(
				new URI( "http://url.com/some%20folder" ),
				UriUtils.urlToUri( url ) );
	}


	@Test
	public void testUrlToUri_4() throws Exception {
		String url = "http://roboconf.net";
		Assert.assertEquals( new URI( url ), UriUtils.urlToUri( url ) );
	}


	@Test
	public void testUrlToUri_5() throws Exception {
		String url = "http://url.com/some%20folder";
		Assert.assertEquals( new URI( url ), UriUtils.urlToUri( url ) );
	}


	@Test
	public void testUrlToUri_6() throws Exception {
		String url = "http://url.com/some folder";
		Assert.assertEquals(
				new URI( "http://url.com/some%20folder" ),
				UriUtils.urlToUri( url ) );
	}


	@Test
	public void testBuildNewURI_1() throws Exception {
		String suffix = "http://absolute-url.fr";
		Assert.assertEquals( new URI( suffix ), UriUtils.buildNewURI( null, suffix ));
	}


	@Test
	public void testBuildNewURI_2() throws Exception {
		String url = "http://absolute-url.fr/";
		String suffix = "readme.txt";
		Assert.assertEquals( new URI( url + suffix ), UriUtils.buildNewURI( new URI( url ), suffix ));
	}


	@Test
	public void testBuildNewURI_3() throws Exception {
		String url = "http://absolute-url.fr";
		String suffix = "readme.txt";
		Assert.assertEquals( new URI( url + "/" + suffix ), UriUtils.buildNewURI( new URI( url ), suffix ));
	}


	@Test
	public void testBuildNewURI_4() throws Exception {
		String url = "http://absolute-url.fr/folder";
		String suffix = "readme.txt";
		Assert.assertEquals( new URI( url + "/" + suffix ), UriUtils.buildNewURI( new URI( url ), suffix ));
	}


	@Test
	public void testBuildNewURI_5() throws Exception {
		String url = "http://absolute-url.fr/folder";
		String suffix = "../readme.txt";
		Assert.assertEquals(
				new URI( "http://absolute-url.fr/readme.txt" ),
				UriUtils.buildNewURI( new URI( url ), suffix ));
	}


	@Test
	public void testBuildNewURI_6() throws Exception {
		String url = "http://absolute-url.fr/folder";
		String suffix = "./readme.txt";
		Assert.assertEquals(
				new URI( "http://absolute-url.fr/folder/readme.txt" ),
				UriUtils.buildNewURI( new URI( url ), suffix ));
	}


	@Test
	public void testBuildNewURI_7() throws Exception {
		String url = "http://absolute-url.fr/folder";
		String suffix = "f1/f2/f3/readme.txt";
		Assert.assertEquals(
				new URI( "http://absolute-url.fr/folder/f1/f2/f3/readme.txt" ),
				UriUtils.buildNewURI( new URI( url ), suffix ));
	}


	@Test( expected = IllegalArgumentException.class )
	public void testBuildNewURI_8() throws Exception {
		UriUtils.buildNewURI( new URI( "http://absolute-url.fr/folder" ), null );
	}
}
