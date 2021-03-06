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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Logger;

import net.roboconf.core.utils.ProgramUtils;

import org.junit.Assume;
import org.junit.Test;

/**
 * @author Vincent Zurczak - Linagora
 */
public class ProgramUtilsTest {

	@Test
	public void testNonNullMap_Windows() throws Exception {

		boolean isWin = System.getProperty( "os.name" ).toLowerCase().contains( "win" );
		Assume.assumeTrue( isWin );
		ProgramUtils.executeCommand(
				Logger.getLogger( getClass().getName()),
				Arrays.asList( "cmd", "/C", "dir" ),
				new HashMap<String,String> ());
	}


	@Test
	public void testNonNullMap_UnixFamily() throws Exception {

		String osName = System.getProperty( "os.name" ).toLowerCase();
		boolean isUnix = osName.contains( "linux" )
				|| osName.contains( "unix" )
				|| osName.contains( "freebsd" );

		Assume.assumeTrue( isUnix );
		ProgramUtils.executeCommand(
				Logger.getLogger( getClass().getName()),
				Arrays.asList( "/bin/sh", "-c", "pwd" ),
				new HashMap<String,String> ());
	}


	@Test
	public void testNullMap_Windows() throws Exception {

		boolean isWin = System.getProperty( "os.name" ).toLowerCase().contains( "win" );
		Assume.assumeTrue( isWin );
		ProgramUtils.executeCommand(
				Logger.getLogger( getClass().getName()),
				Arrays.asList( "cmd", "/C", "dir" ),
				null );
	}


	@Test
	public void testNullMap_UnixFamily() throws Exception {

		String osName = System.getProperty( "os.name" ).toLowerCase();
		boolean isUnix = osName.contains( "linux" )
				|| osName.contains( "unix" )
				|| osName.contains( "freebsd" );

		Assume.assumeTrue( isUnix );
		ProgramUtils.executeCommand(
				Logger.getLogger( getClass().getName()),
				Arrays.asList( "/bin/sh", "-c", "pwd" ),
				null );
	}


	@Test( expected = IOException.class )
	public void testExecutionFailure_Windows() throws Exception {

		boolean isWin = System.getProperty( "os.name" ).toLowerCase().contains( "win" );
		Assume.assumeTrue( isWin );
		ProgramUtils.executeCommand(
				Logger.getLogger( getClass().getName()),
				Arrays.asList( "help" ),
				null );
	}


	@Test( expected = IOException.class )
	public void testExecutionFailure_UnixFamily() throws Exception {

		String osName = System.getProperty( "os.name" ).toLowerCase();
		boolean isUnix = osName.contains( "linux" )
				|| osName.contains( "unix" )
				|| osName.contains( "freebsd" );

		Assume.assumeTrue( isUnix );
		ProgramUtils.executeCommand(
				Logger.getLogger( getClass().getName()),
				Arrays.asList( "/bin/sh", "-c", "apt-get-update" ),
				new HashMap<String,String> ());

		// Either it requires root privileges, or it is not installed.
	}


	@Test( expected = IOException.class )
	public void testInvalidCommand() throws Exception {

		ProgramUtils.executeCommand(
				Logger.getLogger( getClass().getName()),
				Arrays.asList( "whatever" ), null );
	}
}
