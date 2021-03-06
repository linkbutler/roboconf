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

package net.roboconf.agent;

import java.util.logging.Logger;

import junit.framework.Assert;
import net.roboconf.core.agents.DataHelpers;

import org.junit.Test;

/**
 * @author Vincent Zurczak - Linagora
 */
public class AgentDataTest {

	@Test
	public void testReadIaasProperties_null() {

		AgentData ad = AgentData.readIaasProperties( null, Logger.getAnonymousLogger());
		Assert.assertNull( ad.getApplicationName());
		Assert.assertNull( ad.getIpAddress());
		Assert.assertNull( ad.getMessageServerIp());
		Assert.assertNull( ad.getMessageServerPassword());
		Assert.assertNull( ad.getMessageServerUsername());
		Assert.assertNull( ad.getRootInstanceName());
	}


	@Test
	public void testReadIaasProperties_all() throws Exception {

		String s = DataHelpers.writeIaasDataAsString( "ip", "user", "pwd", "my app", "root name" );

		AgentData ad = AgentData.readIaasProperties( s, Logger.getAnonymousLogger());
		Assert.assertEquals( "my app", ad.getApplicationName());
		Assert.assertNull( ad.getIpAddress());
		Assert.assertEquals( "ip", ad.getMessageServerIp());
		Assert.assertEquals( "pwd", ad.getMessageServerPassword());
		Assert.assertEquals( "user", ad.getMessageServerUsername());
		Assert.assertEquals( "root name", ad.getRootInstanceName());
	}


	@Test
	public void testReadIaasProperties_partial() throws Exception {

		String s = DataHelpers.writeIaasDataAsString( "ip", "user", null, "my app", "root name" );

		AgentData ad = AgentData.readIaasProperties( s, Logger.getAnonymousLogger());
		Assert.assertEquals( "my app", ad.getApplicationName());
		Assert.assertNull( ad.getIpAddress());
		Assert.assertEquals( "ip", ad.getMessageServerIp());
		Assert.assertNull( ad.getMessageServerPassword());
		Assert.assertEquals( "user", ad.getMessageServerUsername());
		Assert.assertEquals( "root name", ad.getRootInstanceName());
	}


	@Test
	public void testValidate() {

		AgentData ad = new AgentData();
		ad.setApplicationName( "my app" );
		ad.setRootInstanceName( "root" );
		ad.setMessageServerIp( "192.168.1.18" );
		ad.setMessageServerPassword( "azerty (;))" );
		ad.setMessageServerUsername( "personne" );
		ad.setIpAddress( "whatever" );
		Assert.assertNull( ad.validate());

		ad.setIpAddress( null );
		Assert.assertNull( ad.validate());

		ad.setApplicationName( null );
		Assert.assertNotNull( ad.validate());
		ad.setApplicationName( "my app" );

		ad.setRootInstanceName( "" );
		Assert.assertNotNull( ad.validate());
		ad.setRootInstanceName( "root" );

		ad.setMessageServerIp( null );
		Assert.assertNotNull( ad.validate());
		ad.setMessageServerIp( "192.168.1.18" );

		ad.setMessageServerPassword( "   " );
		Assert.assertNotNull( ad.validate());
		ad.setMessageServerPassword( "azerty" );

		ad.setMessageServerUsername( "" );
		Assert.assertNotNull( ad.validate());
		ad.setMessageServerUsername( "personne" );
	}
}
