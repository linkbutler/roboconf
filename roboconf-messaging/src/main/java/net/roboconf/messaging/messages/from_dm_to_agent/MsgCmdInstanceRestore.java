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
 * @author Vincent Zurczak - Linagora
 * @author Linh Manh Pham - LIG
 */
public class MsgCmdInstanceRestore extends Message {

	private static final long serialVersionUID = 411037586577734609L;
	private final String instancePath;
	private final String oldInstancePath;
	private final String destPath;
	private final String deleteOldRoot;
	
	/**
	 * Constructor.
	 * @param instancePath 
	 */
	public MsgCmdInstanceRestore() {
		super();
		this.instancePath = null;
		this.oldInstancePath = null;
		this.destPath = null;
		this.deleteOldRoot = null;
	}

	/**
	 * Constructor.
	 * @param instancePath 
	 */
	public MsgCmdInstanceRestore(String instancePath) {
		super();
		this.instancePath = instancePath;
		this.oldInstancePath = null;
		this.destPath = null;
		this.deleteOldRoot = null;
	}

	/**
	 * Constructor.
	 * @param instance
	 */
	public MsgCmdInstanceRestore( Instance instance ) {
		this( InstanceHelpers.computeInstancePath( instance ));
	}
	
	/**
	 * Constructor.
	 * @param instance
	 * @param oldInstance
	 */
	public MsgCmdInstanceRestore( Instance instance, String oldInstancePath ) {
		super();
		this.instancePath = InstanceHelpers.computeInstancePath( instance );
		this.oldInstancePath = oldInstancePath;
		this.destPath = null;
		this.deleteOldRoot = null;
	}
	
	/**
	 * Constructor.
	 * @param instance
	 * @param oldInstance
	 * @param deleteOldRoot
	 */
	public MsgCmdInstanceRestore( Instance instance, String oldInstancePath, String destPath, String deleteOldRoot ) {
		super();
		this.instancePath = InstanceHelpers.computeInstancePath( instance );
		this.oldInstancePath = oldInstancePath;
		this.destPath = destPath;
		this.deleteOldRoot = deleteOldRoot;
	}

	/**
	 * @return the instancePath
	 */
	public String getInstancePath() {
		return this.instancePath;
	}
	
	/**
	 * @return the oldInstancePath
	 */
	public String getOldInstancePath() {
		return this.oldInstancePath;
	}
	
	/**
	 * @return the destPath
	 */
	public String getDestPath() {
		return this.destPath;
	}
	
	/**
	 * @return the decision if delete old root
	 */
	public String getDeleteOldRoot() {
		return this.deleteOldRoot;
	}
}
