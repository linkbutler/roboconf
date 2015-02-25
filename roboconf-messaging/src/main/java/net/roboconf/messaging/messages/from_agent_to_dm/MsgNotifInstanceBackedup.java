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

package net.roboconf.messaging.messages.from_agent_to_dm;

import net.roboconf.core.model.helpers.InstanceHelpers;
import net.roboconf.core.model.runtime.Instance;
import net.roboconf.messaging.messages.Message;

/**
 * @author Linh Manh Pham - LIG
 */
public class MsgNotifInstanceBackedup extends Message {

	private static final long serialVersionUID = -7071403536196140938L;
	private final String instancePath;
	private final String destPath;
	private final String applicationName;
	private final String deleteOldRoot;


	
	/**
	 * Constructor.
	 * @param applicationName
	 * @param componentInstance
	 */
	public MsgNotifInstanceBackedup( String applicationName, Instance instance ) {
		super();
		this.instancePath = InstanceHelpers.computeInstancePath( instance );
		this.applicationName = applicationName;
		this.deleteOldRoot = null;
		this.destPath = null;
	}
	
	/**
	 * Constructor.
	 * @param applicationName
	 * @param componentInstance
	 * @param deleteOldRoot
	 */
	public MsgNotifInstanceBackedup( String applicationName, Instance instance, String deleteOldRoot ) {
		super();
		this.instancePath = InstanceHelpers.computeInstancePath( instance );
		this.applicationName = applicationName;
		this.deleteOldRoot = deleteOldRoot;
		this.destPath = null;
	}
	
	/**
	 * Constructor.
	 * @param applicationName
	 * @param componentInstance
	 * @param deleteOldRoot
	 * @param destPath - only use for replicate and migration
	 */
	public MsgNotifInstanceBackedup( String applicationName, Instance instance, String destPath, String deleteOldRoot ) {
		super();
		this.instancePath = InstanceHelpers.computeInstancePath( instance );
		this.applicationName = applicationName;
		this.deleteOldRoot = deleteOldRoot;
		this.destPath = destPath;
	}

	/**
	 * @return the instancePath
	 */
	public String getInstancePath() {
		return this.instancePath;
	}

	/**
	 * @return the applicationName
	 */
	public String getApplicationName() {
		return this.applicationName;
	}
	
	/**
	 * @return the deleteOldRoot decision
	 */
	public String getDeleteOldRoot() {
		return this.deleteOldRoot;
	}
	
	/**
	 * @return the destPath
	 */
	public String getDestPath() {
		return this.destPath;
	}
}
