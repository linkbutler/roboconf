/**
 * Copyright 2013-2014 Linagora, Université Joseph Fourier
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

package net.roboconf.dm.environment.messaging;

import java.io.IOException;
import java.util.logging.Logger;

import net.roboconf.core.model.helpers.ImportHelpers;
import net.roboconf.core.model.helpers.InstanceHelpers;
import net.roboconf.core.model.runtime.Application;
import net.roboconf.core.model.runtime.Instance;
import net.roboconf.core.model.runtime.Instance.InstanceStatus;
import net.roboconf.dm.management.ManagedApplication;
import net.roboconf.dm.management.Manager;
import net.roboconf.dm.management.exceptions.ImpossibleInsertionException;
import net.roboconf.iaas.api.IaasException;
import net.roboconf.messaging.client.AbstractMessageProcessor;
import net.roboconf.messaging.messages.Message;
import net.roboconf.messaging.messages.from_agent_to_dm.MsgNotifHeartbeat;
import net.roboconf.messaging.messages.from_agent_to_dm.MsgNotifInstanceBackedup;
import net.roboconf.messaging.messages.from_agent_to_dm.MsgNotifInstanceChanged;
import net.roboconf.messaging.messages.from_agent_to_dm.MsgNotifInstanceMigrated;
import net.roboconf.messaging.messages.from_agent_to_dm.MsgNotifInstanceRemoved;
import net.roboconf.messaging.messages.from_agent_to_dm.MsgNotifInstanceRestored;
import net.roboconf.messaging.messages.from_agent_to_dm.MsgNotifMachineDown;
import net.roboconf.messaging.messages.from_agent_to_dm.MsgNotifMachineUp;

/**
 * This class is in charge of updating the model from messages / notifications.
 * <p>
 * These messages have been sent by an agent.
 * </p>
 *
 * @author Noël - LIG
 */
public class DmMessageProcessor extends AbstractMessageProcessor {

	private final Logger logger = Logger.getLogger( DmMessageProcessor.class.getName());



	/**
	 * Processes a message (dispatch method).
	 * @param message (not null)
	 */
	@Override
	public void processMessage( Message message ) {

		if( message instanceof MsgNotifMachineUp )
			processMsgNotifMachineUp((MsgNotifMachineUp) message );

		else if( message instanceof MsgNotifMachineDown )
			processMsgNotifMachineDown((MsgNotifMachineDown) message );

		else if( message instanceof MsgNotifInstanceChanged )
			processMsgNotifInstanceChanged((MsgNotifInstanceChanged) message );

		else if( message instanceof MsgNotifInstanceRemoved )
			processMsgNotifInstanceRemoved((MsgNotifInstanceRemoved) message );

		else if( message instanceof MsgNotifHeartbeat )
			processMsgNotifHeartbeat((MsgNotifHeartbeat) message );
		
		else if( message instanceof MsgNotifInstanceBackedup )		// Linh Manh Pham
			processMsgNotifInstanceBackedup((MsgNotifInstanceBackedup) message );
		
		else if( message instanceof MsgNotifInstanceRestored )		// Linh Manh Pham
			processMsgNotifInstanceRestored((MsgNotifInstanceRestored) message );
		
		else if( message instanceof MsgNotifInstanceMigrated )		// Linh Manh Pham
			processMsgNotifInstanceMigrated((MsgNotifInstanceMigrated) message );

		else
			this.logger.warning( "The DM got an undetermined message to process: " + message.getClass().getName());
	}


	private void processMsgNotifMachineUp( MsgNotifMachineUp message ) {

		String ipAddress = message.getIpAddress();
		String rootInstanceName = message.getRootInstanceName();
		ManagedApplication ma = Manager.INSTANCE.getAppNameToManagedApplication().get( message.getApplicationName());
		Application app = ma != null ? ma.getApplication() : null;
		Instance rootInstance = InstanceHelpers.findInstanceByPath( app, "/" + rootInstanceName );

		// If 'app' is null, then 'instance' is also null.
		if( rootInstance == null ) {
			StringBuilder sb = new StringBuilder();
			sb.append( "An 'UP' notification was received from an unknown machine: " );
			sb.append( rootInstanceName );
			sb.append( " @ " );
			sb.append( ipAddress );
			sb.append( " (app =  " );
			sb.append( message.getApplicationName());
			sb.append( ")." );
			this.logger.warning( sb.toString());

		} else {
			rootInstance.setStatus( InstanceStatus.DEPLOYED_STARTED );
			rootInstance.getData().put( Instance.IP_ADDRESS, ipAddress );
			this.logger.fine( rootInstanceName + " @ " + ipAddress + " is up and running." );
			// The UP message has already been stored by the manager. It will be sent on the next timer tick.
			Manager.INSTANCE.saveConfiguration( ma );
		}
	}


	private void processMsgNotifMachineDown( MsgNotifMachineDown message ) {

		String rootInstanceName = message.getRootInstanceName();
		Application app = Manager.INSTANCE.findApplicationByName( message.getApplicationName());
		Instance rootInstance = InstanceHelpers.findInstanceByPath( app, "/" + rootInstanceName );

		// If 'app' is null, then 'instance' is also null.
		if( rootInstance == null ) {
			StringBuilder sb = new StringBuilder();
			sb.append( "A 'DOWN' notification was received from an unknown machine: " );
			sb.append( rootInstanceName );
			sb.append( " (app =  " );
			sb.append( app );
			sb.append( ")." );
			this.logger.warning( sb.toString());

		} else {
			rootInstance.setStatus( InstanceStatus.NOT_DEPLOYED );
			this.logger.info( rootInstanceName + " is now terminated. Back to NOT_DEPLOYED state." );
		}
	}



	private void processMsgNotifHeartbeat( MsgNotifHeartbeat message ) {

		String rootInstanceName = message.getRootInstanceName();
		ManagedApplication ma = Manager.INSTANCE.getAppNameToManagedApplication().get( message.getApplicationName());
		Application app = ma == null ? null : ma.getApplication();
		Instance rootInstance = InstanceHelpers.findInstanceByPath( app, "/" + rootInstanceName );

		// If 'app' is null, then 'instance' is also null.
		if( rootInstance == null ) {
			StringBuilder sb = new StringBuilder();
			sb.append( "A 'HEART BEAT' was received from an unknown machine: " );
			sb.append( rootInstanceName );
			sb.append( " (app =  " );
			sb.append( app );
			sb.append( ")." );
			this.logger.warning( sb.toString());

		} else {
			ma.acknowledgeHeartBeat( rootInstance );
			this.logger.finest( "A heart beat was acknowledged for " + rootInstance.getName() + " in the application " + app.getName() + "." );
		}
	}


	private void processMsgNotifInstanceChanged( MsgNotifInstanceChanged message ) {

		String instancePath = message.getInstancePath();
		Application app = Manager.INSTANCE.findApplicationByName( message.getApplicationName());
		Instance instance = InstanceHelpers.findInstanceByPath( app, instancePath );

		// If 'app' is null, then 'instance' is also null.
		if( instance == null ) {
			StringBuilder sb = new StringBuilder();
			sb.append( "A 'CHANGED' notification was received from an unknown instance: " );
			sb.append( instancePath );
			sb.append( " (app =  " );
			sb.append( app );
			sb.append( ")." );
			this.logger.warning( sb.toString());

		} else {
			InstanceStatus oldStatus = instance.getStatus();
			instance.setStatus( message.getNewStatus());
			ImportHelpers.updateImports( instance, message.getNewImports());

			StringBuilder sb = new StringBuilder();
			sb.append( "Status changed from " );
			sb.append( oldStatus );
			sb.append( " to " );
			sb.append( message.getNewStatus() );
			sb.append( " for instance " );
			sb.append( instancePath );
			sb.append( ". Imports were updated too." );
			this.logger.fine( sb.toString());
		}
	}


	private void processMsgNotifInstanceRemoved( MsgNotifInstanceRemoved message ) {

		String instancePath = message.getInstancePath();
		Application app = Manager.INSTANCE.findApplicationByName( message.getApplicationName());
		Instance instance = InstanceHelpers.findInstanceByPath( app, instancePath );

		// If 'app' is null, then 'instance' is also null.
		if( instance == null ) {
			StringBuilder sb = new StringBuilder();
			sb.append( "A 'REMOVE' notification was received for an unknown instance: " );
			sb.append( instancePath );
			sb.append( " (app =  " );
			sb.append( app );
			sb.append( ")." );
			this.logger.warning( sb.toString());

		} else {
			if( instance.getParent() == null )
				this.logger.warning( "Abnormal behavior. A 'REMOVE' notification was received for a root instance: " + instancePath + "." );
			else
				instance.getParent().getChildren().remove( instance );

			this.logger.info( "Instance " + instancePath + " was removed from the model." );
		}
	}
	
	
	private void processMsgNotifInstanceBackedup( MsgNotifInstanceBackedup message ) {		// Linh Manh Pham

		String instancePath = message.getInstancePath();
		String deleteOldRoot = message.getDeleteOldRoot();
		ManagedApplication ma = Manager.INSTANCE.getAppNameToManagedApplication().get( message.getApplicationName());
		Application app = ma != null ? ma.getApplication() : null;
		Instance instance = InstanceHelpers.findInstanceByPath( app, instancePath );

		// If 'app' is null, then 'instance' is also null.
		if( instance == null ) {
			StringBuilder sb = new StringBuilder();
			sb.append( "A 'BACKED_UP' notification was received for an unknown instance: " );
			sb.append( instancePath );
			sb.append( " (app =  " );
			sb.append( app );
			sb.append( ")." );
			this.logger.warning( sb.toString());

		} else {
			String instanceName = instance.getName();
			String copyInstanceName = instanceName + "_migrated";
			//Instance rootInstance = InstanceHelpers.findRootInstance(instance);
			Instance rootCopy = new Instance();
			if( instance.getParent() == null )
				this.logger.warning( "Abnormal behavior. A 'BACKED_UP' notification was received for a root instance: " + instancePath + "." );
			else {
				if ( deleteOldRoot == null ) {	// only back up
					this.logger.info( "Instance: " + instancePath + " was backed up succesfully." );
				} else {	// for back up as a part of migration progress
					rootCopy = InstanceHelpers.duplicateAllInstancesOnTheInstancePathOf( instance, "_migrated" );
					try {
						Manager.INSTANCE.addInstance(ma, null, rootCopy);
						Instance instanceCopy = InstanceHelpers.findInstanceByName(ma.getApplication(), copyInstanceName);
						Manager.INSTANCE.deployAll(ma, rootCopy);
						Manager.INSTANCE.restore(ma, instanceCopy, instancePath, deleteOldRoot);
						Manager.INSTANCE.start(ma, instanceCopy);
						this.logger.info( "A request to restore " + instancePath + " to " + InstanceHelpers.computeInstancePath(instanceCopy) + " was sent." );
					} catch (ImpossibleInsertionException e) {
						this.logger.warning( "ImpossibleInsertionException. Duplicate instance failed!" );
					} catch (IaasException e) {
						this.logger.warning( "IaasException. Deploy and start instanced failed!" );
					} catch (IOException e) {
						this.logger.warning( "IOException. Deploy and start instanced failed!" );
					}
				}
			}
		}
	}
	
	
	private void processMsgNotifInstanceRestored( MsgNotifInstanceRestored message ) {		// Linh Manh Pham

		StringBuilder sb = new StringBuilder();
		sb.append( "A 'RESTORED' notification was received. The restore operation finished!" );
		this.logger.info( sb.toString());
	}
	
	
	private void processMsgNotifInstanceMigrated( MsgNotifInstanceMigrated message ) {		// Linh Manh Pham

		String oldInstancePath = message.getOldInstancePath();
		String deleteOldRoot = message.getDeleteOldRoot();
		ManagedApplication ma = Manager.INSTANCE.getAppNameToManagedApplication().get( message.getApplicationName());
		Application app = ma != null ? ma.getApplication() : null;
		Instance oldInstance = InstanceHelpers.findInstanceByPath( app, oldInstancePath );

		// Do not turn off and clean the old instance and its machine
		if( oldInstance == null ) {
			StringBuilder sb = new StringBuilder();
			sb.append( "A 'MIGRATED' notification was received for 'RESTORE' or 'MIGRATE' operation but we don't know how to do next." );
			this.logger.warning( sb.toString());

		} else {
			if ( "1".equals(deleteOldRoot) ) {	// delete rootInstance (and every instances inside) of the oldInstance
				try {
					Instance rootInstance = InstanceHelpers.findRootInstance(oldInstance);
					Manager.INSTANCE.stopAllThoroughly(ma, rootInstance);
					Manager.INSTANCE.undeployAllThoroughly(ma, rootInstance);
					//Thread.sleep(30 * 1000);
					//Manager.INSTANCE.removeInstance(ma, rootInstance);
					this.logger.info( "All instances of the path " + oldInstancePath + " were removed from the model." );
				} catch (IaasException e) {
					this.logger.warning( "IaasException. Undeploy instance failed!" );
				} catch (IOException e) {
					this.logger.warning( "IOException. Undeploy instance failed!" );
				}
			} else if ( "0".equals(deleteOldRoot) ) {	// only delete the oldInstance
				try {
					Manager.INSTANCE.stopAllThoroughly(ma, oldInstance);
					Manager.INSTANCE.undeployAllThoroughly(ma, oldInstance);
					//Thread.sleep(30 * 1000);
					//Manager.INSTANCE.removeInstance(ma, oldInstance);
					this.logger.info( "Instance " + oldInstance.getName() + " was removed from the model." );
				} catch (IOException e) {
					this.logger.warning( "IOException. Undeploy instance failed!" );
				} catch (IaasException e) {
					this.logger.warning( "IOException. Stop instance failed!" );
				}
			} else {
				this.logger.info( "We keep all instances in the " + oldInstancePath + ". Nothing was removed from the model." );
			}
		}
	}
}
