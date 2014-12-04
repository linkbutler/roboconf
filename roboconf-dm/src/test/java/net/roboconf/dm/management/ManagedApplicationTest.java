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

package net.roboconf.dm.management;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;
import net.roboconf.core.model.helpers.ComponentHelpers;
import net.roboconf.core.model.helpers.InstanceHelpers;
import net.roboconf.core.model.runtime.Component;
import net.roboconf.core.model.runtime.Instance;
import net.roboconf.core.model.runtime.Instance.InstanceStatus;
import net.roboconf.core.utils.Utils;
import net.roboconf.dm.internal.TestApplication;
import net.roboconf.dm.management.exceptions.ImpossibleInsertionException;
import net.roboconf.iaas.api.IaasException;
import net.roboconf.messaging.messages.Message;
import net.roboconf.messaging.messages.from_dm_to_agent.MsgCmdInstanceRestore;
import net.roboconf.messaging.messages.from_dm_to_agent.MsgCmdInstanceStop;
import net.roboconf.messaging.messages.from_dm_to_agent.MsgCmdInstanceUndeploy;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Vincent Zurczak - Linagora
 */
public class ManagedApplicationTest {

	private ManagedApplication ma;
	private TestApplication app;


	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();


	@Before
	public void initializeMa() {

		File f = this.folder.newFolder( "Roboconf_test" );
		this.app = new TestApplication();
		this.ma = new ManagedApplication( this.app, f );
	}


	@Test
	public void testConstructor() throws Exception {
		Assert.assertEquals( this.app, this.ma.getApplication());
	}


	@Test
	public void testGetName() {

		ManagedApplication ma = new ManagedApplication( null, null );
		Assert.assertNull( ma.getName());

		ma = new ManagedApplication( this.app, null );
		Assert.assertEquals( this.app.getName(), ma.getName());
	}


	@Test
	public void testStoreAwaitingMessage() {

		Instance rootInstance = new Instance( "root" );
		Assert.assertEquals( 0, this.ma.rootInstanceToAwaitingMessages.size());
		this.ma.storeAwaitingMessage( rootInstance, new MsgCmdInstanceRestore());
		Assert.assertEquals( 1, this.ma.rootInstanceToAwaitingMessages.size());

		List<Message> messages = this.ma.rootInstanceToAwaitingMessages.get( rootInstance );
		Assert.assertEquals( 1, messages.size());
		Assert.assertEquals( MsgCmdInstanceRestore.class, messages.get( 0 ).getClass());

		Instance childInstance = new Instance( "child" );
		InstanceHelpers.insertChild( rootInstance, childInstance );
		this.ma.storeAwaitingMessage( childInstance, new MsgCmdInstanceStop( childInstance ));
		Assert.assertEquals( 1, this.ma.rootInstanceToAwaitingMessages.size());

		Assert.assertEquals( 2, messages.size());
		Assert.assertEquals( MsgCmdInstanceRestore.class, messages.get( 0 ).getClass());
		Assert.assertEquals( MsgCmdInstanceStop.class, messages.get( 1 ).getClass());

		childInstance.setParent( null );
		this.ma.storeAwaitingMessage( childInstance, new MsgCmdInstanceUndeploy( childInstance ));
		Assert.assertEquals( 2, this.ma.rootInstanceToAwaitingMessages.size());

		messages = this.ma.rootInstanceToAwaitingMessages.get( childInstance );
		Assert.assertEquals( 1, messages.size());
		Assert.assertEquals( MsgCmdInstanceUndeploy.class, messages.get( 0 ).getClass());
	}
	
	
	@Test
	public void testDestPathValidation() throws ImpossibleInsertionException {
		
		Instance rootInstance = new Instance( "root" );
		Component rootComponent = new Component( "vm" );
		rootInstance.setComponent(rootComponent);
		
		Instance childInstance = new Instance( "child" );
	    Component childComponent = new Component( "mysql" );
	    childInstance.setComponent(childComponent);
		
	    InstanceHelpers.insertChild( rootInstance, childInstance );
	    
		Manager.INSTANCE.addInstance(ma, null, rootInstance);
		
		String instancePath = "/root/child";
		String destPath = "/rootd/childd";
		List<String> instancesInPath = Utils.splitNicely(instancePath, "/");
		instancesInPath.remove(0);
		String componentsInPath = "";
		String stringTemplateToCompare = "";
		String result = "";

		for (String i : instancesInPath ) {
			Instance currentInstance = InstanceHelpers.findInstanceByName(ma.getApplication(), i);
			String componentName = currentInstance.getComponent().getName();
			componentsInPath = componentsInPath + "/instance_of_" + componentName;
			stringTemplateToCompare = stringTemplateToCompare + "[/]{1}[^/]*";
		}
		
		if ( !destPath.matches(stringTemplateToCompare) ) result = "not match";
		else result = "match";
	
		Assert.assertEquals( "[/]{1}[^/]*[/]{1}[^/]*", stringTemplateToCompare );
		Assert.assertEquals( "/instance_of_vm/instance_of_mysql", componentsInPath );
		Assert.assertEquals( "match", result );
	}
	
	
	@Test
	public void testDuplicateInstancesChangeNamesStraightPath() throws ImpossibleInsertionException, IaasException, IOException {
		
		Instance rootInstance = new Instance( "vm1" );
		Component rootComponent = new Component( "vm" );
		rootInstance.setComponent(rootComponent);
		
		Instance childInstance = new Instance( "tomcat" );
	    Component childComponent = new Component( "tomcat" );
	    childInstance.setComponent(childComponent);
	    
	    Instance grandChildInstance = new Instance( "rubis" );
	    Component grandChildComponent = new Component( "rubis" );
	    grandChildInstance.setComponent(grandChildComponent);
	    
	    ComponentHelpers.insertChild(childComponent, grandChildComponent);
	    ComponentHelpers.insertChild(rootComponent, childComponent);
		
	    InstanceHelpers.insertChild( childInstance, grandChildInstance );
	    InstanceHelpers.insertChild( rootInstance, childInstance );
	    
		Manager.INSTANCE.addInstance(ma, null, rootInstance);
		String destPath = "/vm1/tomcat1/rubis";
		List<String> instanceNamesOnDestPath = Utils.splitNicely(destPath, "/");
		instanceNamesOnDestPath.remove(0);
		instanceNamesOnDestPath.remove(0);
		//instanceNamesOnDestPath.remove(0);
		Instance rootOfBranch = InstanceHelpers.duplicateInstanceChangeNamesStraightPath( childInstance, instanceNamesOnDestPath );
		rootOfBranch.setParent(childInstance.getParent());
		Manager.INSTANCE.addInstance( ma, childInstance.getParent(), rootOfBranch );
	
		Assert.assertEquals( "tomcat1", rootOfBranch.getName());
		Assert.assertEquals( "vm1", rootOfBranch.getParent().getName());
		Instance seekInstance = InstanceHelpers.findInstanceByPath(ma.getApplication(), "/vm1/tomcat");
		Assert.assertEquals( "tomcat", seekInstance.getName() );
	}


	@Test
	public void testRemoveAwaitingMessages() {

		Assert.assertEquals( 0, this.ma.rootInstanceToAwaitingMessages.size());
		Assert.assertEquals( 0, this.ma.removeAwaitingMessages( new Instance( "whatever" )).size());

		Instance rootInstance = new Instance( "root" );
		this.ma.storeAwaitingMessage( rootInstance, new MsgCmdInstanceRestore());
		Assert.assertEquals( 1, this.ma.rootInstanceToAwaitingMessages.size());
		Assert.assertEquals( 1, this.ma.removeAwaitingMessages( rootInstance ).size());
		Assert.assertEquals( 0, this.ma.rootInstanceToAwaitingMessages.size());
		Assert.assertEquals( 0, this.ma.removeAwaitingMessages( rootInstance ).size());
	}


	@Test
	public void testStoreAndRemove() {

		Instance rootInstance = new Instance( "root" );
		Assert.assertEquals( 0, this.ma.rootInstanceToAwaitingMessages.size());
		this.ma.storeAwaitingMessage( rootInstance, new MsgCmdInstanceRestore());
		this.ma.storeAwaitingMessage( rootInstance, new MsgCmdInstanceRestore());
		Assert.assertEquals( 1, this.ma.rootInstanceToAwaitingMessages.size());

		List<Message> messages = this.ma.rootInstanceToAwaitingMessages.get( rootInstance );
		Assert.assertEquals( 2, messages.size());
		Assert.assertEquals( MsgCmdInstanceRestore.class, messages.get( 0 ).getClass());
		Assert.assertEquals( MsgCmdInstanceRestore.class, messages.get( 1 ).getClass());

		messages = this.ma.removeAwaitingMessages( rootInstance );
		Assert.assertEquals( 2, messages.size());
		Assert.assertEquals( MsgCmdInstanceRestore.class, messages.get( 0 ).getClass());
		Assert.assertEquals( MsgCmdInstanceRestore.class, messages.get( 1 ).getClass());
		Assert.assertEquals( 0, this.ma.rootInstanceToAwaitingMessages.size());

		this.ma.storeAwaitingMessage( rootInstance, new MsgCmdInstanceUndeploy( rootInstance ));
		messages = this.ma.rootInstanceToAwaitingMessages.get( rootInstance );
		Assert.assertEquals( 1, messages.size());
		Assert.assertEquals( MsgCmdInstanceUndeploy.class, messages.get( 0 ).getClass());
	}


	@Test
	public void testAcknowledgeHeartBeat() {

		Assert.assertEquals( 0, this.ma.rootInstanceToMissedHeartBeatsCount.size());
		this.ma.acknowledgeHeartBeat( this.app.getMySqlVm());
		Assert.assertEquals( 0, this.ma.rootInstanceToMissedHeartBeatsCount.size());

		this.app.getMySqlVm().setStatus( InstanceStatus.DEPLOYED_STARTED );
		this.ma.acknowledgeHeartBeat( this.app.getMySqlVm());
		Assert.assertEquals( InstanceStatus.DEPLOYED_STARTED, this.app.getMySqlVm().getStatus());
		Assert.assertNull( this.ma.rootInstanceToMissedHeartBeatsCount.get( this.app.getMySqlVm()));

		this.app.getMySqlVm().setStatus( InstanceStatus.PROBLEM );
		this.ma.acknowledgeHeartBeat( this.app.getMySqlVm());
		Assert.assertEquals( InstanceStatus.DEPLOYED_STARTED, this.app.getMySqlVm().getStatus());
		Assert.assertNull( this.ma.rootInstanceToMissedHeartBeatsCount.get( this.app.getMySqlVm()));

		this.app.getMySqlVm().setStatus( InstanceStatus.PROBLEM );
		this.ma.rootInstanceToMissedHeartBeatsCount.put( this.app.getMySqlVm(), 5 );
		this.ma.acknowledgeHeartBeat( this.app.getMySqlVm());
		Assert.assertEquals( InstanceStatus.DEPLOYED_STARTED, this.app.getMySqlVm().getStatus());
		Assert.assertNull( this.ma.rootInstanceToMissedHeartBeatsCount.get( this.app.getMySqlVm()));
	}


	@Test
	public void testCheckStates() {

		Assert.assertEquals( 0, this.ma.rootInstanceToMissedHeartBeatsCount.size());
		this.app.getMySqlVm().setStatus( InstanceStatus.DEPLOYED_STARTED );
		this.ma.checkStates();
		Assert.assertEquals( 1, this.ma.rootInstanceToMissedHeartBeatsCount.size());

		this.ma.acknowledgeHeartBeat( this.app.getMySqlVm());
		Assert.assertEquals( 0, this.ma.rootInstanceToMissedHeartBeatsCount.size());

		Assert.assertEquals( InstanceStatus.DEPLOYED_STARTED, this.app.getMySqlVm().getStatus());
		for( int i=0; i<=ManagedApplication.MISSED_HEARTBEATS_THRESHOLD; i++ ) {
			this.ma.checkStates();
			Assert.assertEquals( i, i, this.ma.rootInstanceToMissedHeartBeatsCount.get( this.app.getMySqlVm()));
		}

		Assert.assertEquals( InstanceStatus.PROBLEM, this.app.getMySqlVm().getStatus());
		this.app.getMySqlVm().setStatus( InstanceStatus.UNDEPLOYING );
		this.ma.checkStates();
		Assert.assertNull( this.ma.rootInstanceToMissedHeartBeatsCount.get( this.app.getMySqlVm()));

		this.app.getMySqlVm().setStatus( InstanceStatus.DEPLOYING );
		this.ma.checkStates();
		Assert.assertNull( this.ma.rootInstanceToMissedHeartBeatsCount.get( this.app.getMySqlVm()));

		this.app.getMySqlVm().setStatus( InstanceStatus.NOT_DEPLOYED );
		this.ma.checkStates();
		Assert.assertNull( this.ma.rootInstanceToMissedHeartBeatsCount.get( this.app.getMySqlVm()));
	}
}
