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

package net.roboconf.plugin.logger;

import java.io.File;

import junit.framework.Assert;
import net.roboconf.core.model.runtime.Instance;

import org.junit.Test;

/**
 * @author Vincent Zurczak - Linagora
 */
public class PluginLoggerTest {

	@Test
	public void testPlugin() throws Exception {

		PluginLogger pl = new PluginLogger();
		Assert.assertEquals( "logger", pl.getPluginName());

		// Make sure we can invoke invoke method in any order
		pl.setAgentName( "My Agent" );
		pl.setDumpDirectory( null );
		pl.setDumpDirectory( new File( "whatever" ));

		pl.undeploy( null );
		pl.undeploy( new Instance( "inst" ));

		pl.start( null );
		pl.start( new Instance( "inst" ));

		pl.deploy( null );
		pl.deploy( new Instance( "inst" ));

		pl.stop( null );
		pl.stop( new Instance( "inst" ));

		pl.update( null, null, null );
		pl.update( new Instance( "inst" ), null, null );

		pl.initialize( null );
		pl.initialize( new Instance( "inst" ));
	}
}
