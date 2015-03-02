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

package net.roboconf.dm.rest.api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.roboconf.core.model.runtime.Component;
import net.roboconf.core.model.runtime.Instance;
import net.roboconf.dm.rest.UrlConstants;

/**
 * The REST API to manipulate instances on the DM.
 * <p>
 * Implementing classes may have to redefine the "Path" annotation
 * on the class. This is not required on methods.
 * </p>
 *
 * @author Vincent Zurczak - Linagora
 * @author Linh Manh Pham - Linagora
 */
public interface IApplicationWs {

	String PATH = "/" + UrlConstants.APP + "/{name}";


	/**
	 * Performs an action on an instance of an application.
	 * @param applicationName the application name
	 * @param action see {@link ApplicationAction}
	 * @param instancePath the instance path (not null)
	 * @return a response
	 */
	@POST
	@Path( "/{action}" )
	@Consumes( MediaType.APPLICATION_JSON )
	Response perform( @PathParam("name") String applicationName, @PathParam("action") String action, @QueryParam("instance-path") String instancePath );

	
	/**
	 * Request a "migrate" operation on an instance of an application.
	 * @param applicationName the application name
	 * @param action see {@link ApplicationAction}
	 * @param instancePath the instance path (not null)
	 * @param destPath the instance path where instance will be restored (not null)
	 * @param deleteOldRoot: a String, if == 0, the old migrated instance will be removed; if == 1, the old root will be removed; 
	 *                       if == -1, don't remove anything; otherwise fail
	 * @return a response
	 */
	@POST
	@Path( "/migrate" )
	@Consumes( MediaType.APPLICATION_JSON )
	Response migrate( @PathParam("name") String applicationName, @QueryParam("instance-path") String instancePath, @QueryParam("dest-path") String destPath, @QueryParam("delete-old-root") String deleteOldRoot );

	
	/**
	 * Request a "restore" operation on an instance of an application.
	 * @param applicationName the application name
	 * @param action see {@link ApplicationAction}
	 * @param instancePath the instance path (not null)
	 * @param destPath the instance path where instance will be restored (not null)
	 * @param deleteOldRoot: a String, if == 0, the old migrated instance will be removed; if == 1, the old root will be removed; 
	 *                       if == -1, don't remove anything; otherwise fail
	 * @return a response
	 */
	@POST
	@Path( "/restore" )
	@Consumes( MediaType.APPLICATION_JSON )
	Response restore( @PathParam("name") String applicationName, @QueryParam("instance-path") String instancePath, @QueryParam("dest-path") String destPath, @QueryParam("delete-old-root") String deleteOldRoot );
	
	
	/**
	 * Deploys and starts several instances at once.
	 * @param applicationName the application name
	 * @param instancePath the instance pat (null to consider the whole application)
	 * @return a response
	 */
	@POST
	@Path( "/deploy-all" )
	@Consumes( MediaType.APPLICATION_JSON )
	Response deployAndStartAll( @PathParam("name") String applicationName, @QueryParam("instance-path") String instancePath );


	/**
	 * Stops several instances at once.
	 * @param applicationName the application name
	 * @param instancePath the instance pat (null to consider the whole application)
	 * @return a response
	 */
	@POST
	@Path( "/stop-all" )
	@Consumes( MediaType.APPLICATION_JSON )
	Response stopAll( @PathParam("name") String applicationName, @QueryParam("instance-path") String instancePath );


	/**
	 * Undeploys several instances at once.
	 * @param applicationName the application name
	 * @param instancePath the instance pat (null to consider the whole application)
	 * @return a response
	 */
	@POST
	@Path( "/undeploy-all" )
	@Consumes( MediaType.APPLICATION_JSON )
	Response undeployAll( @PathParam("name") String applicationName, @QueryParam("instance-path") String instancePath );


	/**
	 * Adds a new instance.
	 * @param applicationName the application name
	 * @param parentInstancePath the path of the parent instance (optional, null to consider the application as the root)
	 * @param instance the new instance
	 * @return a response
	 */
	@POST
	@Path( "/add" )
	@Consumes( MediaType.APPLICATION_JSON )
	Response addInstance( @PathParam("name") String applicationName, @QueryParam("instance-path") String parentInstancePath, Instance instance );


	/**
	 * Lists the paths of the children of an instance.
	 * @param applicationName the application name
	 * @param instancePath the instance pat (null to consider the whole application)
	 * @param allChildren true to get all the children, false to only get the direct children
	 * @return a non-null list
	 */
	@GET
	@Path( "/children" )
	@Produces( MediaType.APPLICATION_JSON )
	List<Instance> listChildrenInstances(
			@PathParam("name") String applicationName,
			@QueryParam("instance-path") String instancePath,
			@QueryParam("all-children") boolean allChildren );


	/**
	 * Finds possible components under a given instance.
	 * <p>
	 * This method answers the question: what can we deploy on this instance?
	 * </p>
	 *
	 * @param applicationName the application name
	 * @param instancePath the instance path (if null, we consider the application as the root)
	 * @return a non-null list of components names
	 */
	@GET
	@Path( "/possibilities" )
	@Produces( MediaType.APPLICATION_JSON )
	List<Component> findPossibleComponentChildren( @PathParam("name") String applicationName, @QueryParam("instance-path") String instancePath );


	/**
	 * Lists the available components in this application.
	 * @param applicationName the application name
	 * @return a non-null list of components
	 */
	@GET
	@Path( "/components" )
	@Produces( MediaType.APPLICATION_JSON )
	List<Component> listComponents( @PathParam("name") String applicationName );


	/**
	 * Finds possible parent instances for a given component.
	 * <p>
	 * This method answers the question: where could I deploy such a component?
	 * </p>
	 *
	 *
	 * @param applicationName the application name
	 * @return a non-null list of instances paths
	 */
	@GET
	@Path("/component/{componentName}")
	@Produces( MediaType.APPLICATION_JSON )
	List<String> findPossibleParentInstances( @PathParam("name") String applicationName, @PathParam("componentName") String componentName );


	/**
	 * Creates an instance from a component name.
	 * @param applicationName the application name
	 * @return a response
	 */
	@GET
	@Path("/component/{componentName}/new")
	@Produces( MediaType.APPLICATION_JSON )
	Instance createInstanceFromComponent( @PathParam("name") String applicationName, @PathParam("componentName") String componentName );
}
