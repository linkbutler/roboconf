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
public class MsgNotifInstanceRestored extends Message {

	private static final long serialVersionUID = 2504203778404700032L;
	private final String instancePath;
	private final String applicationName;
	private final String oldInstancePath;
	private final String deleteOldRoot;


	/**
	 * Constructor.
	 * @param applicationName
	 * @param componentInstance
	 */
	public MsgNotifInstanceRestored( String applicationName, Instance instance, String oldInstancePath, String deleteOldRoot ) {
		super();
		this.instancePath = InstanceHelpers.computeInstancePath( instance );
		this.applicationName = applicationName;
		this.oldInstancePath = oldInstancePath;
		this.deleteOldRoot = deleteOldRoot;
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
	 * @return the oldInstancePath
	 */
	public String getOldInstancePath() {
		return this.oldInstancePath;
	}
	
	/**
	 * @return value of the deleteOldRoot para
	 */
	public String getDeleteOldRoot() {
		return this.deleteOldRoot;
	}
}
