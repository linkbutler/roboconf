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

package net.roboconf.dm.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.roboconf.core.model.comparators.InstanceComparator;
import net.roboconf.core.model.helpers.ComponentHelpers;
import net.roboconf.core.model.helpers.InstanceHelpers;
import net.roboconf.core.model.runtime.Application;
import net.roboconf.core.model.runtime.Component;
import net.roboconf.core.model.runtime.Instance;
import net.roboconf.core.model.runtime.Instance.InstanceStatus;
import net.roboconf.core.utils.Utils;
import net.roboconf.dm.management.ManagedApplication;
import net.roboconf.dm.management.Manager;
import net.roboconf.dm.management.exceptions.ImpossibleInsertionException;
import net.roboconf.dm.management.exceptions.UnauthorizedActionException;
import net.roboconf.dm.rest.api.ApplicationAction;
import net.roboconf.dm.rest.api.IApplicationWs;
import net.roboconf.iaas.api.IaasException;

/**
 * @author Vincent Zurczak - Linagora
 */
@Path( IApplicationWs.PATH )
public class ApplicationWs implements IApplicationWs {

	private final Logger logger = Logger.getLogger( ApplicationWs.class.getName());


	/*
	 * (non-Javadoc)
	 * @see net.roboconf.dm.rest.api.IApplicationWs
	 * #perform(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public Response perform( String applicationName, String actionAS, String instancePath ) {

		this.logger.fine( "Request: perform action '" + actionAS + "' in " + applicationName + ", instance " + instancePath + "." );
		Response response;
		try {
			ManagedApplication ma;
			Instance instance;
			ApplicationAction action = ApplicationAction.whichAction( actionAS );
			if(( ma = Manager.INSTANCE.getAppNameToManagedApplication().get( applicationName )) == null )
				response = Response.status( Status.NOT_FOUND ).entity( "Application " + applicationName + " does not exist." ).build();

			else if(( instance = InstanceHelpers.findInstanceByPath( ma.getApplication(), instancePath )) == null )
				response = Response.status( Status.NOT_FOUND ).entity( "Instance " + instancePath + " was not found." ).build();

			else if( action == ApplicationAction.DEPLOY ) {
				if( instance.getParent() == null )
					Manager.INSTANCE.deployRoot( ma, instance );
				else
					Manager.INSTANCE.deploy( ma, instance );
				response = Response.ok().build();

			} else if( action == ApplicationAction.START ) {
				Manager.INSTANCE.start( ma, instance );
				response = Response.ok().build();

			} else if( action == ApplicationAction.STOP ) {
				Manager.INSTANCE.stop( ma, instance );
				response = Response.ok().build();

			} else if( action == ApplicationAction.UNDEPLOY ) {
				if( instance.getParent() == null )
					Manager.INSTANCE.undeployRoot( ma, instance );
				else
					Manager.INSTANCE.undeploy( ma, instance );
				response = Response.ok().build();

			} else if( action == ApplicationAction.REMOVE ) {
				Manager.INSTANCE.removeInstance( ma, instance );
				response = Response.ok().build();

			} else if( action == ApplicationAction.BACKUP ) {	// Linh Manh Pham
				Manager.INSTANCE.backup( ma, instance );
				response = Response.ok().build();

			} else {
				response = Response.status( Status.BAD_REQUEST ).entity( "Invalid action: " + actionAS ).build();
			}

		} catch( UnauthorizedActionException e ) {
			response = Response.status( Status.FORBIDDEN ).entity( e.getMessage()).build();

		} catch( IOException e ) {
			response = Response.status( Status.FORBIDDEN ).entity( e.getMessage()).build();

		} catch( IaasException e ) {
			response = Response.status( Status.FORBIDDEN ).entity( e.getMessage()).build();

		} catch( Exception e ) {
			response = Response.status( Status.INTERNAL_SERVER_ERROR ).entity( e.getMessage()).build();
			this.logger.finest( Utils.writeException( e ));
		}

		return response;
	}
	
	
	@Override
	public Response migrate( String applicationName, String instancePath, String deleteOldRoot ) {

		this.logger.fine( "Request: perform action 'migrate' in " + applicationName + ", instance " + instancePath + " , delete old root: " + deleteOldRoot + "." );
		Response response;
		try {
			ManagedApplication ma;
			Instance instance;

			if(( ma = Manager.INSTANCE.getAppNameToManagedApplication().get( applicationName )) == null )
				response = Response.status( Status.NOT_FOUND ).entity( "Application " + applicationName + " does not exist." ).build();

			else if(( instance = InstanceHelpers.findInstanceByPath( ma.getApplication(), instancePath )) == null )
				response = Response.status( Status.NOT_FOUND ).entity( "Instance " + instancePath + " was not found." ).build();

			else if( "-1".equals(deleteOldRoot) || "1".equals(deleteOldRoot) || "0".equals(deleteOldRoot) ) {   
				Manager.INSTANCE.migrate( ma, instance, deleteOldRoot );
				response = Response.ok().build();
				
			} else {
				response = Response.status( Status.BAD_REQUEST ).entity( "Invalid parameter delete-old-root: " + deleteOldRoot + " . It should only be '-1', '0' or '1'." ).build();
			}

		} catch( IOException e ) {
			response = Response.status( Status.FORBIDDEN ).entity( e.getMessage()).build();

		} catch( Exception e ) {
			response = Response.status( Status.INTERNAL_SERVER_ERROR ).entity( e.getMessage()).build();
			this.logger.finest( Utils.writeException( e ));
		}

		return response;
	}
	
	
	@Override
	public Response restore( String applicationName, String instancePath, String destPath, String deleteOldRoot ) {

		this.logger.fine( "Request: perform action 'restore' in " + applicationName + ", instance " + instancePath + ", destination path " + destPath + ", delete old root: " + deleteOldRoot + "." );
		Response response;
		try {
			ManagedApplication ma = Manager.INSTANCE.getAppNameToManagedApplication().get( applicationName );
			Instance instance = InstanceHelpers.findInstanceByPath( ma.getApplication(), instancePath );

			if( instance == null )	{
				response = Response.status( Status.NOT_FOUND ).entity( "Instance " + instancePath + " was not found." ).build();
			}
			
			else if( deleteOldRoot == null ) {
				response = Response.status( Status.NOT_FOUND ).entity( "Missing 'deleteOldRoot' parameter." ).build();
			}
			
			else if( destPath == null || "".equals(destPath) ) {
				response = Response.status( Status.NOT_FOUND ).entity( "Missing 'destPath' or destination path empty." ).build();
			}
			
			else if( "-1".equals(deleteOldRoot) || "1".equals(deleteOldRoot) || "0".equals(deleteOldRoot) ) {
				List<Instance> instancesInPath = InstanceHelpers.getAllInstancesInTheInstancePath(ma.getApplication(), instancePath);
				String componentsInPath = "";
				String stringTemplateToCompare = "";

				for (Instance i : instancesInPath ) {
					String componentName = i.getComponent().getName();
					componentsInPath = componentsInPath + "/instance_of_" + componentName;
					stringTemplateToCompare = stringTemplateToCompare + "[/]{1}[^/]*";
				}
				
				if ( !destPath.matches(stringTemplateToCompare) ) {
					response = Response.status( Status.BAD_REQUEST ).entity( "The 'destPath' must follow template: " + componentsInPath ).build();
				} else {
					// Calculate where to put restored instance based on the destPath, recreate all instances on the path to the new one
					List<String> instanceNamesOnDestPath = Utils.splitNicely(destPath, "/");
					instanceNamesOnDestPath.remove(0);
					Map<String,String> componentNameToInstanceDestPath = new HashMap<String,String> ();
					String cumulativePath = "";
					
					List<Instance> instancesOnInstancePath = InstanceHelpers.getAllInstancesInTheInstancePath(ma.getApplication(), instancePath);
					int n = 0;
					Instance rootOfBranch = null;
					Response responseInCase = null;
					Instance instancePointOfSeparation = null;
					
					for ( Instance i : instancesOnInstancePath ) {
						String componentName = i.getComponent().getName();
						List<Instance> allInstancesofCurrentComponent = new ArrayList<Instance>();
						if (instancePointOfSeparation == null) allInstancesofCurrentComponent = InstanceHelpers.findChildInstancesByComponentName(ma.getApplication(), componentName );
						else allInstancesofCurrentComponent = InstanceHelpers.findChildInstancesByComponentName(instancePointOfSeparation, componentName );
						cumulativePath = cumulativePath + "/" + instanceNamesOnDestPath.get(0).toString();
						componentNameToInstanceDestPath.put( componentName, cumulativePath );
						boolean immediateQuit = false;
						
						if ( allInstancesofCurrentComponent.size() != 0 ) {
							for ( Instance j : allInstancesofCurrentComponent ) {
								this.logger.info( "Instance to compare in InstancePath=" + componentNameToInstanceDestPath.get(componentName) + " and current comparing instance=" + InstanceHelpers.computeInstancePath(j) + " and componentName=" + componentName + "." );
								if ( componentNameToInstanceDestPath.get(componentName).equals(InstanceHelpers.computeInstancePath(j)) ) {								
									this.logger.info( "Instance " + InstanceHelpers.computeInstancePath(j) + " is reused for instance " + instancePath + " in application=" + ma.getName() + "." );
									if ( n == instancesOnInstancePath.size() - 1 ) {	// insPath coincidence destPath
										if ( i.getStatus() == InstanceStatus.DEPLOYED_STOPPED ) {	
											Manager.INSTANCE.restore( ma, i, instancePath, "-1" );	// restore only i and force deleteOldRoot to '-1'
											Manager.INSTANCE.start( ma, i );
											responseInCase = Response.ok().build();
										//} else if ( i.getStatus() == InstanceStatus.DEPLOYED_STARTED ) {	// it's better do not overwrite a running instance, but sometime we can try
											//Manager.INSTANCE.stop( ma, i );		//  need to stop the instance first
											//Manager.INSTANCE.restore( ma, i, instancePath, "-1" );	// restore only i and force deleteOldRoot to '-1'
											//Manager.INSTANCE.start( ma, i );
											//responseInCase = Response.ok().build();
										} else if ( i.getStatus() == InstanceStatus.NOT_DEPLOYED ) {
											Manager.INSTANCE.deploy(ma, i );		//  need to deploy the instance first
											Manager.INSTANCE.restore( ma, i, instancePath, "-1" );	// restore only i and force deleteOldRoot to '-1'
											Manager.INSTANCE.start( ma, i );
											responseInCase = Response.ok().build();
										}
										else responseInCase = Response.status( Status.BAD_REQUEST ).entity( "Instance " + i.getName() + " must not be in 'STARTED' and 'PROBLEM' state in this case." ).build();
									}
									instancePointOfSeparation = j;
									immediateQuit = false;
									break;
								} else {
									this.logger.info( "Instance " + instancePath + " in " + ma.getName() + " will be put on a new instancePath=" + cumulativePath + "." );
									immediateQuit = true;
								}
							}
						} else {
							this.logger.info( "Instance " + instancePath + " in " + ma.getName() + " will be put on a new instancePath=" + cumulativePath + "." );
							immediateQuit = true;
						}
						
						if (immediateQuit) {
							List<Instance> instancesToDuplicate = new ArrayList<Instance>();
							for(int runner=n; runner<instancesOnInstancePath.size(); runner++) instancesToDuplicate.add( instancesOnInstancePath.get(runner) );
							rootOfBranch = InstanceHelpers.duplicateInstanceChangeNamesStraightPath( instancesToDuplicate, instanceNamesOnDestPath );
							if( n == 0 ) {
								Manager.INSTANCE.addInstance( ma, null, rootOfBranch );	// n==0 means 'i' is a rootInstance, otherwise it's not
							}
							else {
								Manager.INSTANCE.addInstance( ma, instancePointOfSeparation, rootOfBranch );	// n==0 means 'i' is a rootInstance, otherwise it's not
							}
							Manager.INSTANCE.deployAll( ma, rootOfBranch );
							List<String> oldInstancePaths = new ArrayList<String>();
							for(int runner=n; runner<instancesOnInstancePath.size(); runner++) oldInstancePaths.add( InstanceHelpers.computeInstancePath(instancesOnInstancePath.get(runner)) );
							this.logger.info( "oldInstancePaths=" + oldInstancePaths.toString() );
							Manager.INSTANCE.restoreAll( ma, rootOfBranch, oldInstancePaths, deleteOldRoot );
							Manager.INSTANCE.startAll( ma, rootOfBranch );
							responseInCase = Response.ok().build();
							break;
						}
						instanceNamesOnDestPath.remove(0);
						n++;
					}	
					response = responseInCase;
				}
				
			} else {
				response = Response.status( Status.BAD_REQUEST ).entity( "Invalid parameter delete-old-root: " + deleteOldRoot + " . It should only be '-1', '0' or '1'." ).build();
			}

		} catch( IOException e ) {
			response = Response.status( Status.FORBIDDEN ).entity( e.getMessage()).build();

		} catch( Exception e ) {
			response = Response.status( Status.INTERNAL_SERVER_ERROR ).entity( e.getMessage()).build();
			this.logger.finest( Utils.writeException( e ));
		}

		return response;
	}


	/*
	 * (non-Javadoc)
	 * @see net.roboconf.dm.rest.api.IApplicationWs
	 * #deployAndStartAll(java.lang.String, java.lang.String)
	 */
	@Override
	public Response deployAndStartAll( String applicationName, String instancePath ) {

		this.logger.fine( "Request: deploy and start instances in " + applicationName + ", from instance = " + instancePath + "." );
		Response response;
		ManagedApplication ma;
		try {
			if(( ma = Manager.INSTANCE.getAppNameToManagedApplication().get( applicationName )) == null ) {
				response = Response.status( Status.NOT_FOUND ).entity( "Application " + applicationName + " does not exist." ).build();
			} else {
				Manager.INSTANCE.deployAndStartAll( ma, InstanceHelpers.findInstanceByPath( ma.getApplication(), instancePath ));
				response = Response.ok().build();
			}

		} catch( IaasException e ) {
			response = Response.status( Status.FORBIDDEN ).entity( e.getMessage()).build();

		} catch( IOException e ) {
			response = Response.status( Status.FORBIDDEN ).entity( e.getMessage()).build();
		}

		return response;
	}


	/*
	 * (non-Javadoc)
	 * @see net.roboconf.dm.rest.api.IApplicationWs
	 * #stopAll(java.lang.String, java.lang.String)
	 */
	@Override
	public Response stopAll( String applicationName, String instancePath ) {

		this.logger.fine( "Request: stop instances in " + applicationName + ", from instance = " + instancePath + "." );
		Response response;
		ManagedApplication ma;
		try {
			if(( ma = Manager.INSTANCE.getAppNameToManagedApplication().get( applicationName )) == null ) {
				response = Response.status( Status.NOT_FOUND ).entity( "Application " + applicationName + " does not exist." ).build();
			} else {
				Manager.INSTANCE.stopAll( ma, InstanceHelpers.findInstanceByPath( ma.getApplication(), instancePath ));
				response = Response.ok().build();
			}

		} catch( IOException e ) {
			response = Response.status( Status.FORBIDDEN ).entity( e.getMessage()).build();
		}

		return response;
	}


	/*
	 * (non-Javadoc)
	 * @see net.roboconf.dm.rest.api.IApplicationWs
	 * #undeployAll(java.lang.String, java.lang.String)
	 */
	@Override
	public Response undeployAll( String applicationName, String instancePath ) {

		this.logger.fine( "Request: deploy and start instances in " + applicationName + ", from instance = " + instancePath + "." );
		Response response;
		ManagedApplication ma;
		try {
			if(( ma = Manager.INSTANCE.getAppNameToManagedApplication().get( applicationName )) == null ) {
				response = Response.status( Status.NOT_FOUND ).entity( "Application " + applicationName + " does not exist." ).build();
			} else {
				Manager.INSTANCE.undeployAll( ma, InstanceHelpers.findInstanceByPath( ma.getApplication(), instancePath ));
				response = Response.ok().build();
			}

		} catch( IaasException e ) {
			response = Response.status( Status.FORBIDDEN ).entity( e.getMessage()).build();

		} catch( IOException e ) {
			response = Response.status( Status.FORBIDDEN ).entity( e.getMessage()).build();
		}

		return response;
	}


	/*
	 * (non-Javadoc)
	 * @see net.roboconf.dm.rest.api.IApplicationWs
	 * #listChildrenInstances(java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public List<Instance> listChildrenInstances( String applicationName, String instancePath, boolean allChildren ) {

		List<Instance> result = new ArrayList<Instance> ();
		Application app = Manager.INSTANCE.findApplicationByName( applicationName );

		// Log
		if( instancePath == null )
			this.logger.fine( "Request: list " + (allChildren ? "all" : "root") + " instances for " + applicationName + "." );
		else
			this.logger.fine( "Request: list " + (allChildren ? "all" : "direct") + " children instances for " + instancePath + " in " + applicationName + "." );

		// Find the instances
		Instance inst;
		if( app != null ) {
			if( instancePath == null ) {
				if( allChildren )
					result.addAll( InstanceHelpers.getAllInstances( app ));
				else
					result.addAll( app.getRootInstances());
			}

			else if(( inst = InstanceHelpers.findInstanceByPath( app, instancePath )) != null ) {
				if( allChildren ) {
					result.addAll( InstanceHelpers.buildHierarchicalList( inst ));
					result.remove( inst );
				} else {
					result.addAll( inst.getChildren());
				}
			}
		}

		// Bug #64: sort instance paths for the clients
		Collections.sort( result, new InstanceComparator());
		return result;
	}


	/* (non-Javadoc)
	 * @see net.roboconf.dm.rest.client.exceptions.server.IInstanceWs
	 * #addInstance(java.lang.String, java.lang.String, net.roboconf.core.model.runtime.Instance)
	 */
	@Override
	public Response addInstance( String applicationName, String parentInstancePath, Instance instance ) {

		// Invoke the manager
		if( parentInstancePath == null )
			this.logger.fine( "Request: add root instance " + instance.getName() + " in " + applicationName + "." );
		else
			this.logger.fine( "Request: add instance " + instance.getName() + " under " + parentInstancePath + " in " + applicationName + "." );

		Response response;
		try {
			ManagedApplication ma;
			if(( ma = Manager.INSTANCE.getAppNameToManagedApplication().get( applicationName )) == null ) {
				response = Response.status( Status.NOT_FOUND ).entity( "Application " + applicationName + " does not exist." ).build();

			} else {
				Instance parentInstance = InstanceHelpers.findInstanceByPath( ma.getApplication(), parentInstancePath );
				Manager.INSTANCE.addInstance( ma, parentInstance, instance );
				response = Response.ok().build();
			}

		} catch( ImpossibleInsertionException e ) {
			response = Response.status( Status.NOT_ACCEPTABLE ).entity( e.getMessage()).build();
		}

		return response;
	}


	/*
	 * (non-Javadoc)
	 * @see net.roboconf.dm.rest.client.exceptions.server.IGraphWs
	 * #listComponents(java.lang.String)
	 */
	@Override
	public List<Component> listComponents( String applicationName ) {

		this.logger.fine( "Request: list components for the application " + applicationName + "." );
		List<Component> result = new ArrayList<Component> ();
		Application app = Manager.INSTANCE.findApplicationByName( applicationName );
		if( app != null )
			result.addAll( ComponentHelpers.findAllComponents( app ));

		return result;
	}


	/*
	 * (non-Javadoc)
	 * @see net.roboconf.dm.rest.api.IApplicationWs
	 * #findPossibleComponentChildren(java.lang.String, java.lang.String)
	 */
	@Override
	public List<Component> findPossibleComponentChildren( String applicationName, String instancePath ) {

		if( instancePath == null )
			this.logger.fine( "Request: list possible root instances in " + applicationName + "." );
		else
			this.logger.fine( "Request: find components that can be deployed under " + instancePath + " in " + applicationName + "." );

		Application app = Manager.INSTANCE.findApplicationByName( applicationName );
		Instance instance = null;
		if( app != null
				&& instancePath != null )
			instance = InstanceHelpers.findInstanceByPath( app, instancePath );

		List<Component> result = new ArrayList<Component> ();
		if( instance != null )
			result.addAll( instance.getComponent().getChildren());

		else if( app != null
				&& instancePath == null )
			result.addAll( app.getGraphs().getRootComponents());

		return result;
	}


	/*
	 * (non-Javadoc)
	 * @see net.roboconf.dm.rest.client.exceptions.server.IGraphWs
	 * #findPossibleParentInstances(java.lang.String, java.lang.String)
	 */
	@Override
	public List<String> findPossibleParentInstances( String applicationName, String componentName ) {

		this.logger.fine( "Request: find instances where a component " + componentName + " could be deployed on, in " + applicationName + "." );
		List<String> result = new ArrayList<String> ();
		Application app = Manager.INSTANCE.findApplicationByName( applicationName );

		// Run through all the instances.
		// See if their component can support a child "of type componentName".
		if( app != null ) {
			for( Instance instance : InstanceHelpers.getAllInstances( app )) {
				for( Component c : instance.getComponent().getChildren()) {
					if( componentName.equals( c.getName())) {
						String instancePath = InstanceHelpers.computeInstancePath( instance );
						result.add( instancePath );
					}
				}
			}
		}

		return result;
	}


	/*
	 * (non-Javadoc)
	 * @see net.roboconf.dm.rest.client.exceptions.server.IGraphWs
	 * #createInstanceFromComponent(java.lang.String, java.lang.String)
	 */
	@Override
	public Instance createInstanceFromComponent( String applicationName, String componentName ) {

		this.logger.fine( "Request: create a new instance for component " + componentName + " in " + applicationName + "." );
		Instance result = null;
		Component comp = null;

		Application app = Manager.INSTANCE.findApplicationByName( applicationName );
		if( app != null )
			comp = ComponentHelpers.findComponent( app.getGraphs(), componentName );

		// TODO: In the real implementation, properties should be set in the exports.
		// The interest of this operation is to display default export values
		// in a web console (as an example) and thus allow to override them if necessary.
		if( comp != null ) {
			result = new Instance( "new-instance" ).component( comp );
		}

		return result;
	}
}
