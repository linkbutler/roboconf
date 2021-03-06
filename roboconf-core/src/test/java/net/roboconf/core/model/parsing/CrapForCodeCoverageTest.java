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

package net.roboconf.core.model.parsing;

import java.io.File;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Vincent Zurczak - Linagora
 */
public class CrapForCodeCoverageTest {

	@Test
	public void testCrap() {
		FileDefinition def = new FileDefinition( new File( "whatever" ));

		// Imports
		BlockImport imp = new BlockImport( def );
		Assert.assertNotNull( imp.toString());

		imp.setUri( "http://an-import.uri" );
		Assert.assertNotNull( imp.toString());

		// Comment
		BlockComment comment = new BlockComment( imp.getDeclaringFile(), "we don't ware" );
		Assert.assertNotNull( comment.toString());

		comment = new BlockComment( def, null );
		Assert.assertNotNull( comment.toString());

		// Component
		BlockComponent comp = new BlockComponent( def );
		Assert.assertNull( comp.toString());

		comp.setName( "woo" );
		Assert.assertEquals( "woo", comp.toString());

		// Facet
		BlockFacet facet = new BlockFacet( def );
		Assert.assertNull( facet.toString());

		facet.setName( "woo 2" );
		Assert.assertEquals( "woo 2", facet.toString());

		// Block Property
		BlockProperty prop = new BlockProperty( def );
		Assert.assertNotNull( prop.toString());

		prop.setName( "you" );
		Assert.assertNotNull( prop.toString());

		prop.setValue( "whatever" );
		Assert.assertNotNull( prop.toString());

		// Blank
		BlockBlank blank = new BlockBlank( def, null );
		Assert.assertNotNull( blank.toString());

		blank = new BlockBlank( def, "..." );
		Assert.assertNotNull( blank.toString());
	}


	@Test( expected = IllegalArgumentException.class )
	public void testInvalidDefinition() {
		new BlockProperty( null );
	}
}
