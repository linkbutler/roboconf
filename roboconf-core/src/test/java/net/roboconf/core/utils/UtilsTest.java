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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.Assert;
import net.roboconf.core.internal.tests.TestUtils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Vincent Zurczak - Linagora
 */
public class UtilsTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();


	@Test
	public void testDeleteFilesRecursively() {

		File tmpDir = new File( System.getProperty( "java.io.tmpdir" ), UUID.randomUUID().toString());
		if( ! tmpDir.mkdir())
			Assert.fail( "Could not create a temporary directory." );

		String[] dirs = { "dir1", "dir1/dir2", "dir1/dir2/dir3", "dir1/dir2/dir4", "dir2", "dir1/dir54" };
		for( String dir : dirs ) {
			File f = new File( tmpDir, dir );
			if( ! f.mkdir())
				Assert.fail( "Could not create a sub-directory: " + dir );
		}

		String[] files = { "test.txt", "te.txt", "dir1/test.txt", "dir2/some.txt", "dir1/dir2/dir3/pol.txt" };
		try {
			for( String file : files ) {
				File f = new File( tmpDir, file );
				if( ! f.createNewFile())
					Assert.fail( "Could not create a file: " + file );
			}

		} catch( IOException e ) {
			Assert.fail( "Could not create a file. " + e.getMessage());
		}

		Assert.assertTrue( tmpDir.exists());
		try {
			Utils.deleteFilesRecursively( tmpDir );
			Assert.assertFalse( "Temp directory could not be deleted: " + tmpDir.getName(), tmpDir.exists());

		} catch( IOException e ) {
			Assert.fail( "Failed to delete the temporary directory." );
		}

		try {
			Utils.deleteFilesRecursively((File) null);

		} catch( IOException e ) {
			Assert.fail( "Null file must be supported" );
		}

		try {
			Utils.deleteFilesRecursively((File[]) null);

		} catch( IOException e ) {
			Assert.fail( "Null file array must be supported" );
		}

		try {
			File[] nullFiles = new File[] { null, null };
			Utils.deleteFilesRecursively( nullFiles );

		} catch( IOException e ) {
			Assert.fail( "Array of null files must be supported" );
		}
	}


	@Test
	public void testSplitNicely() {

		List<String> result = Utils.splitNicely( "once, upon, a , time   ", "," );
		Assert.assertEquals( 4, result.size());
		Assert.assertEquals( "once", result.get( 0 ));
		Assert.assertEquals( "upon", result.get( 1 ));
		Assert.assertEquals( "a", result.get( 2 ));
		Assert.assertEquals( "time", result.get( 3 ));

		result = Utils.splitNicely( "once \n\n, upon, a , time \n  ", "\n" );
		Assert.assertEquals( 4, result.size());
		Assert.assertEquals( "once", result.get( 0 ));
		Assert.assertEquals( "", result.get( 1 ).trim());
		Assert.assertEquals( ", upon, a , time", result.get( 2 ));
		Assert.assertEquals( "", result.get( 3 ).trim());

		result = Utils.splitNicely( "once $ $a$ $$ time", "$" );
		Assert.assertEquals( 6, result.size());
		Assert.assertEquals( "once", result.get( 0 ));
		Assert.assertEquals( "", result.get( 1 ).trim());
		Assert.assertEquals( "a", result.get( 2 ));
		Assert.assertEquals( "", result.get( 3 ).trim());
		Assert.assertEquals( "", result.get( 4 ).trim());
		Assert.assertEquals( "time", result.get( 5 ));
		
		result = Utils.splitNicely( "/vmec2mysql/mysql1", "/" );
		Assert.assertEquals( 3, result.size());
		Assert.assertEquals( "vmec2mysql", result.get( 1 ));
		Assert.assertEquals( "", result.get( 0 ));
		Assert.assertEquals( "mysql1", result.get( 2 ));
	}


	@Test( expected = IllegalArgumentException.class )
	public void testSplitNicely_illegalArgument_1() {
		Utils.splitNicely( "once, upon, a , time   ", "" );
	}


	@Test( expected = IllegalArgumentException.class )
	public void testSplitNicely_illegalArgument_2() {
		Utils.splitNicely( "once, upon, a , time   ", null );
	}


	@Test
	public void testAreEqual() {

		Assert.assertTrue( Utils.areEqual( null, null ));
		Assert.assertFalse( Utils.areEqual( null, new Object()));
		Assert.assertFalse( Utils.areEqual( new Object(), null ));
		Assert.assertFalse( Utils.areEqual( new Object(), new Object()));

		Object o = new Object();
		Assert.assertTrue( Utils.areEqual( o, o ));
	}


	@Test
	public void testIsEmptyOrWhitespaces() {

		Assert.assertTrue( Utils.isEmptyOrWhitespaces( null ));
		Assert.assertTrue( Utils.isEmptyOrWhitespaces( "" ));
		Assert.assertTrue( Utils.isEmptyOrWhitespaces( "   " ));
		Assert.assertTrue( Utils.isEmptyOrWhitespaces( " \n  \t" ));
		Assert.assertFalse( Utils.isEmptyOrWhitespaces( " a\n  \t" ));
		Assert.assertFalse( Utils.isEmptyOrWhitespaces( "b" ));
	}


	@Test
	public void testExtractZipArchive() throws Exception {

		// Prepare the original ZIP
		File zipFile = this.folder.newFile( "roboconf_test.zip" );
		Map<String,String> entryToContent = TestUtils.buildZipContent();

		TestUtils.createZipFile( entryToContent, zipFile );
		TestUtils.compareZipContent( zipFile, entryToContent );

		// Prepare the output directory
		File existingDirectory = this.folder.newFolder( "roboconf_test" );
		Assert.assertTrue( existingDirectory.exists());
		Assert.assertEquals( 0, Utils.listAllFiles( existingDirectory ).size());

		// Extract
		Utils.extractZipArchive( zipFile, existingDirectory );

		// And compare
		Assert.assertNotSame( 0, Utils.listAllFiles( existingDirectory ).size());
		Map<String,String> fileToContent = Utils.storeDirectoryResourcesAsString( existingDirectory );
		for( Map.Entry<String,String> entry : fileToContent.entrySet()) {
			Assert.assertTrue( entryToContent.containsKey( entry.getKey()));
			String value = entryToContent.remove( entry.getKey());
			Assert.assertEquals( entry.getValue(), value );
		}

		// Only directories should remain
		for( Map.Entry<String,String> entry : entryToContent.entrySet()) {
			Assert.assertNull( entry.getKey(), entry.getValue());
		}
	}


	@Test
	public void testExtractZipArchive_inexistingDirectory() throws Exception {

		// Prepare the original ZIP
		File zipFile = this.folder.newFile( "roboconf_test.zip" );
		Map<String,String> entryToContent = TestUtils.buildZipContent();
		TestUtils.createZipFile( entryToContent, zipFile );

		// Prepare the output directory
		File unexistingDirectory = this.folder.newFolder( "roboconf_test" );
		if( ! unexistingDirectory.delete())
			throw new IOException( "Failed to delete a directory." );

		Assert.assertFalse( unexistingDirectory.exists());

		// Extract
		Utils.extractZipArchive( zipFile, unexistingDirectory );
		Assert.assertTrue( unexistingDirectory.exists());

		// And compare
		Assert.assertNotSame( 0, Utils.listAllFiles( unexistingDirectory ).size());
		Map<String,String> fileToContent = Utils.storeDirectoryResourcesAsString( unexistingDirectory );
		for( Map.Entry<String,String> entry : fileToContent.entrySet()) {
			Assert.assertTrue( entryToContent.containsKey( entry.getKey()));
			String value = entryToContent.remove( entry.getKey());
			Assert.assertEquals( entry.getValue(), value );
		}

		// Only directories should remain
		for( Map.Entry<String,String> entry : entryToContent.entrySet()) {
			Assert.assertNull( entry.getKey(), entry.getValue());
		}
	}


	@Test( expected = IllegalArgumentException.class )
	public void testExtractZipArchive_illegalArgument_1() throws Exception {
		File existingFile = new File( System.getProperty( "java.io.tmpdir" ));
		Utils.extractZipArchive( new File( "file-that-does-not.exists" ), existingFile );
	}


	@Test( expected = IllegalArgumentException.class )
	public void testExtractZipArchive_illegalArgument_2() throws Exception {
		File existingFile = new File( System.getProperty( "java.io.tmpdir" ));
		Utils.extractZipArchive( null, existingFile );
	}


	@Test( expected = IllegalArgumentException.class )
	public void testExtractZipArchive_illegalArgument_3() throws Exception {
		File existingFile = new File( System.getProperty( "java.io.tmpdir" ));
		Utils.extractZipArchive( existingFile, null );
	}


	@Test( expected = IllegalArgumentException.class )
	public void testExtractZipArchive_illegalArgument_4() throws Exception {

		File existingFile = new File( System.getProperty( "java.io.tmpdir" ));
		File unexistingFile = new File( existingFile, UUID.randomUUID().toString());

		Assert.assertFalse( unexistingFile.exists());
		Utils.extractZipArchive( existingFile, unexistingFile );
	}


	@Test( expected = IllegalArgumentException.class )
	public void testExtractZipArchive_illegalArgument_5() throws Exception {

		File tempZip = this.folder.newFile( "roboconf_test_zip.zip" );
		File tempFile = this.folder.newFile( "roboconf_test.txt" );
		Utils.extractZipArchive( tempZip, tempFile );
	}


	@Test
	public void testCloseQuietly() throws Exception {

		InputStream in = null;
		Utils.closeQuietly( in );

		in = new ByteArrayInputStream( new byte[ 0 ]);
		Utils.closeQuietly( in );

		OutputStream out = new ByteArrayOutputStream();
		Utils.closeQuietly( out );

		out = null;
		Utils.closeQuietly( out );
	}


	@Test
	public void testCloseQuietly_silentInput() throws Exception {

		InputStream in = new InputStream() {
			@Override
			public int read() throws IOException {
				return 0;
			}

			@Override
			public void close() throws IOException {
				throw new IOException();
			}
		};

		Utils.closeQuietly( in );
	}


	@Test
	public void testCloseQuietly_silentOutput() throws Exception {

		OutputStream out = new OutputStream() {
			@Override
			public void write( int b ) throws IOException {
				// nothing
			}

			@Override
			public void close() throws IOException {
				throw new IOException();
			}
		};

		Utils.closeQuietly( out );
	}


	@Test
	public void testWriteException() {

		String msg = "Hello from Roboconf.";
		String stackTrace = Utils.writeException( new Exception( msg ));
		Assert.assertTrue( stackTrace.contains( msg ));
	}


	@Test( expected = IllegalArgumentException.class )
	public void testComputeFileRelativeLocation_failure_notASubFile() {

		final File rootDir = new File( System.getProperty( "java.io.tmpdir" ));
		Utils.computeFileRelativeLocation( rootDir, new File( "invalid-path" ));
	}


	@Test( expected = IllegalArgumentException.class )
	public void testComputeFileRelativeLocation_failure_sameFile() {

		final File rootDir = new File( System.getProperty( "java.io.tmpdir" ));
		Utils.computeFileRelativeLocation( rootDir, rootDir );
	}


	@Test
	public void testComputeFileRelativeLocation_success() {

		final File rootDir = new File( System.getProperty( "java.io.tmpdir" ));
		File directChildFile = new File( rootDir, "woo.txt" );
		Assert.assertEquals(
				directChildFile.getName(),
				Utils.computeFileRelativeLocation( rootDir, directChildFile ));

		String indirectChildPath = "dir1/dir2/script.sh";
		File indirectChildFile = new File( rootDir, indirectChildPath );
		Assert.assertEquals(
				indirectChildPath,
				Utils.computeFileRelativeLocation( rootDir, indirectChildFile ));
	}


	@Test
	public void testListAllFiles() throws Exception {

		final File tempDir = this.folder.newFolder( "roboconf_test" );
		String[] paths = new String[] { "dir1", "dir2", "dir1/dir3" };
		for( String path : paths ) {
			if( ! new File( tempDir, path ).mkdir())
				throw new IOException( "Failed to create " + path );
		}

		paths = new String[] { "dir1/toto.txt", "dir2/script.sh", "dir1/dir3/smart.png" };
		for( String path : paths ) {
			if( ! new File( tempDir, path ).createNewFile())
				throw new IOException( "Failed to create " + path );
		}

		List<File> files = Utils.listAllFiles( tempDir );
		Assert.assertEquals( 3, files.size());
		for( String path : paths )
			Assert.assertTrue( path, files.contains( new File( tempDir, path )));
	}


	@Test( expected = IllegalArgumentException.class )
	public void testListAllFiles_inexistingFile() throws Exception {
		Utils.listAllFiles( new File( "not/existing/file" ));
	}


	@Test( expected = IllegalArgumentException.class )
	public void testListAllFiles_invalidParameter() throws Exception {
		Utils.listAllFiles( this.folder.newFile( "roboconf.txt" ));
	}


	@Test( expected = IllegalArgumentException.class )
	public void testStoreDirectoryResourcesAsBytes_illegalArgument_1() throws Exception {
		Utils.storeDirectoryResourcesAsBytes( new File( "not/existing/file" ));
	}


	@Test( expected = IllegalArgumentException.class )
	public void testStoreDirectoryResourcesAsBytes_illegalArgument_2() throws Exception {
		Utils.storeDirectoryResourcesAsBytes( this.folder.newFile( "roboconf.txt" ));
	}


	@Test
	public void testIsAncestorFile() throws Exception {

		File parent = new File( "home/toto/whatever" );
		Assert.assertTrue( Utils.isAncestorFile( parent, parent ));

		File comp = new File( "home/toto/whatever/" );
		Assert.assertTrue( Utils.isAncestorFile( parent, comp ));

		comp = new File( "home/toto/./whatever/" );
		Assert.assertTrue( Utils.isAncestorFile( parent, comp ));

		comp = new File( "home/toto/../toto/whatever/" );
		Assert.assertTrue( Utils.isAncestorFile( parent, comp ));

		comp = new File( "home/toto/whatever/some-file.txt" );
		Assert.assertTrue( Utils.isAncestorFile( parent, comp ));

		comp = new File( "home/toto/whatever/some/dir/some-file.txt" );
		Assert.assertTrue( Utils.isAncestorFile( parent, comp ));

		comp = new File( "home/toto/" );
		Assert.assertFalse( Utils.isAncestorFile( parent, comp ));

		comp = new File( "home/toto/whateve" );
		Assert.assertFalse( Utils.isAncestorFile( parent, comp ));

		comp = new File( "home/toto/whatevereeeeeee" );
		Assert.assertFalse( Utils.isAncestorFile( parent, comp ));
	}


	@Test
	public void testCopyDirectory_existingTarget() throws Exception {

		// Create a source
		File source = this.folder.newFolder();
		File dir1 = new File( source, "lol/whatever/sub" );
		Assert.assertTrue( dir1.mkdirs());
		File dir2 = new File( source, "sub" );
		Assert.assertTrue( dir2.mkdirs());

		Utils.copyStream( new ByteArrayInputStream( ",kklmsdff sdfl sdfkkl".getBytes( "UTF-8" )), new File( dir1, "f1" ));
		Utils.copyStream( new ByteArrayInputStream( "".getBytes( "UTF-8" )), new File( dir1, "f2" ));
		Utils.copyStream( new ByteArrayInputStream( "sd".getBytes( "UTF-8" )), new File( dir1, "f3" ));

		Utils.copyStream( new ByteArrayInputStream( "sd\ndsfg".getBytes( "UTF-8" )), new File( source, "f" ));

		Utils.copyStream( new ByteArrayInputStream( "sd\ndsfg".getBytes( "UTF-8" )), new File( dir2, "f1" ));
		Utils.copyStream( new ByteArrayInputStream( "sdf df fg".getBytes( "UTF-8" )), new File( dir2, "f45678" ));

		// Copy
		File target = this.folder.newFolder();
		Assert.assertEquals( 0, Utils.listAllFiles( target ).size());
		Utils.copyDirectory( source, target );
		Assert.assertEquals( 6, Utils.listAllFiles( target ).size());
	}


	@Test
	public void testCopyDirectory_inexistingTarget() throws Exception {

		// Create a source
		File source = this.folder.newFolder();
		File dir1 = new File( source, "lol/whatever/sub/many/more/" );
		Assert.assertTrue( dir1.mkdirs());
		File dir2 = new File( source, "sub" );
		Assert.assertTrue( dir2.mkdirs());

		Utils.copyStream( new ByteArrayInputStream( ",kklmsdff sdfl sdfkkl".getBytes( "UTF-8" )), new File( dir1, "f1" ));
		Utils.copyStream( new ByteArrayInputStream( "".getBytes( "UTF-8" )), new File( dir1, "f2" ));
		Utils.copyStream( new ByteArrayInputStream( "sd".getBytes( "UTF-8" )), new File( dir1, "f3" ));

		Utils.copyStream( new ByteArrayInputStream( "sd\ndsfg".getBytes( "UTF-8" )), new File( source, "f" ));

		Utils.copyStream( new ByteArrayInputStream( "sd\ndsfg".getBytes( "UTF-8" )), new File( dir2, "f1" ));
		Utils.copyStream( new ByteArrayInputStream( "".getBytes( "UTF-8" )), new File( dir2, "f4" ));
		Utils.copyStream( new ByteArrayInputStream( "sdf df fg".getBytes( "UTF-8" )), new File( dir2, "f45678" ));

		// Copy
		File target = new File( this.folder.newFolder(), "some" );
		Assert.assertFalse( target.exists());
		Utils.copyDirectory( source, target );
		Assert.assertTrue( target.exists());
		Assert.assertEquals( 7, Utils.listAllFiles( target ).size());
	}
}
