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

package net.roboconf.plugin.api.internal.template;

import java.util.ArrayList;
import java.util.List;

import net.roboconf.core.model.runtime.Import;

/**
 * Bean used to inject an data into a {@link Import} template.
 * @author gcrosmarie - Linagora
 */
public class ImportBean {

	private final Import imprt;

	public ImportBean(Import imprt) {
		this.imprt = imprt;
	}

	public List<Var> getExportedVars() {
		List<Var> result = new ArrayList<ImportBean.Var>();
		for(String name : this.imprt.getExportedVars().keySet()) {
			result.add(new Var(name, this.imprt.getExportedVars().get(name)));
		}

		return result;
	}

	static class Var {
		String name, value;

		public Var(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}

		public void setValue( String value ) {
			this.value = value;
		}
	}
}
