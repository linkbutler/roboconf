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

package net.roboconf.messaging.messages.from_dm_to_agent;

import net.roboconf.core.model.helpers.InstanceHelpers;
import net.roboconf.core.model.runtime.Instance;
import net.roboconf.messaging.messages.Message;

/**
 * @author Linh Manh Pham - LIG
 */
public class MsgCmdInstanceMigrate extends Message {


	private static final long serialVersionUID = -8987925389702692849L;
	private final String instancePath;
	private final String destPath;
	private final String deleteOldRoot;


	/**
	 * Constructor.
	 * @param instancePath
	 */
	public MsgCmdInstanceMigrate( String instancePath, String deleteOldRoot ) {
		super();
		this.instancePath = instancePath;
		this.deleteOldRoot = deleteOldRoot;
		this.destPath = null;
	}

	/**
	 * Constructor.
	 * @param instance
	 */
	public MsgCmdInstanceMigrate( Instance instance, String deleteOldRoot ) {
		this( InstanceHelpers.computeInstancePath( instance ), deleteOldRoot);
	}
	
	/**
	 * Constructor.
	 * @param instancePath
	 */
	public MsgCmdInstanceMigrate( Instance instance, String destPath, String deleteOldRoot ) {
		super();
		this.instancePath = InstanceHelpers.computeInstancePath( instance );
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
	 * @return the destPath
	 */
	public String getDestPath() {
		return this.destPath;
	}
	
	/**
	 * @return the instancePath
	 */
	public String getDeleteOldRoot() {
		return this.deleteOldRoot;
	}
}
