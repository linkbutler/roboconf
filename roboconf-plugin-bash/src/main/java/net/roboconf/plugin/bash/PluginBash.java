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

package net.roboconf.plugin.bash;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import net.roboconf.core.model.helpers.InstanceHelpers;
import net.roboconf.core.model.helpers.VariableHelpers;
import net.roboconf.core.model.runtime.Import;
import net.roboconf.core.model.runtime.Instance;
import net.roboconf.core.model.runtime.Instance.InstanceStatus;
import net.roboconf.core.utils.ProgramUtils;
import net.roboconf.core.utils.Utils;
import net.roboconf.plugin.api.ExecutionLevel;
import net.roboconf.plugin.api.PluginException;
import net.roboconf.plugin.api.PluginInterface;
import net.roboconf.plugin.api.template.InstanceTemplateHelper;

/**
 * The plug-in invokes a shell script on every life cycle change.
 * <p>
 * The action is one of "deploy", "start", "stop", "undeploy" and "update".<br />
 * Let's take an example with the "start" action to understand the way this plug-in works.
 * </p>
 * <ul>
 * 	<li>The plug-in will load scripts/start.sh</li>
 * 	<li>If it is not found, it will try to load templates/start.sh.template</li>
 * 	<li>If it is not found, it will try to load templates/default.sh.template</li>
 * 	<li>If it is not found, the plug-in will do nothing</li>
 * </ul>
 * <p>
 * The default template is used to factorize actions.
 * </p>
 *
 * @author Noël - LIG
 * @author Linh-Manh Pham - LIG
 * @author Pierre-Yves Gibello - Linagora
 * @author Christophe Hamerling - Linagora
 */
public class PluginBash implements PluginInterface {

    private final Logger logger = Logger.getLogger( getClass().getName());
    private ExecutionLevel executionLevel;
    private String agentName;

    private static final String SCRIPTS_FOLDER_NAME = "scripts";
    private static final String TEMPLATES_FOLDER_NAME = "roboconf-templates";
    private static final String FILES_FOLDER_NAME = "files";


    @Override
    public String getPluginName() {
        return "bash";
    }


    @Override
    public void setExecutionLevel( ExecutionLevel executionLevel ) {
        this.executionLevel = executionLevel;
    }


    @Override
    public void setDumpDirectory( File dumpDirectory ) {
        // nothing
    }


    @Override
    public void setAgentName( String agentName ) {
        this.agentName = agentName;
    }


	@Override
	public void initialize( Instance instance ) throws PluginException {
		this.logger.fine( this.agentName + " is initializing the plug-in for " + instance.getName());
	}


	@Override
	public void deploy( Instance instance ) throws PluginException {

		this.logger.fine( this.agentName + " is deploying instance " + instance.getName());
		if( this.executionLevel == ExecutionLevel.LOG )
			return;

		try {
			prepareAndExecuteCommand( "deploy", instance, null, null );

		} catch( Exception e ) {
			throw new PluginException( e );
		}
	}


    @Override
    public void start( Instance instance ) throws PluginException {

        this.logger.fine( this.agentName + " is starting instance " + instance.getName());
        if( this.executionLevel == ExecutionLevel.LOG )
			return;

        try {
			prepareAndExecuteCommand( "start", instance, null, null );

		} catch( Exception e ) {
			throw new PluginException( e );
		}
    }


    @Override
    public void update(Instance instance, Import importChanged, InstanceStatus statusChanged) throws PluginException {

        this.logger.fine( this.agentName + " is updating instance " + instance.getName());
        if( this.executionLevel == ExecutionLevel.LOG )
			return;

        try {
			prepareAndExecuteCommand( "update", instance, importChanged, statusChanged );

		} catch( Exception e ) {
			throw new PluginException( e );
		}
    }


    @Override
    public void stop( Instance instance ) throws PluginException {

        this.logger.fine( this.agentName + " is stopping instance " + instance.getName());
        if( this.executionLevel == ExecutionLevel.LOG )
			return;

        try {
			prepareAndExecuteCommand( "stop", instance, null, null );

		} catch( Exception e ) {
			throw new PluginException( e );
		}
    }


    @Override
    public void undeploy( Instance instance ) throws PluginException {

    	this.logger.fine( this.agentName + " is undeploying instance " + instance.getName());
    	if( this.executionLevel == ExecutionLevel.LOG )
			return;

        try {
			prepareAndExecuteCommand( "undeploy", instance, null, null );

		} catch( Exception e ) {
			throw new PluginException( e );
		}
    }
    
    
    @Override
    public void backup( Instance instance ) throws PluginException {
    	this.logger.fine( this.agentName + " is backing up instance " + instance.getName());
    	if( this.executionLevel == ExecutionLevel.LOG )
			return;

        try {
			prepareAndExecuteCommand( "backup", instance, null, null );

		} catch( Exception e ) {
			throw new PluginException( e );
		}
    }
    	
   
    @Override
    public void restore( Instance instance, String oldInstancePath ) throws PluginException {
    	this.logger.info( this.agentName + " is restoring instance " + instance.getName() + " from instance " + oldInstancePath + "." );
    	if( this.executionLevel == ExecutionLevel.LOG )
			return;

        try {
        	prepareAndExecuteCommandforRestore( "restore", instance, null, null, oldInstancePath );

		} catch( Exception e ) {
			throw new PluginException( e );
		}
    }
    
    
    private void prepareAndExecuteCommandforRestore(String action, Instance instance, Import importChanged, InstanceStatus statusChanged, String oldInstancePath)
    	    throws IOException, InterruptedException {

    	        this.logger.info("Preparing the invocation of " + action + ".sh for instance " + instance.getName());
    	        File instanceDirectory = InstanceHelpers.findInstanceDirectoryOnAgent( instance, getPluginName());

    	        File scriptsFolder = new File(instanceDirectory, SCRIPTS_FOLDER_NAME);
    	        File templatesFolder = new File(instanceDirectory, TEMPLATES_FOLDER_NAME);

    	        File script = new File(scriptsFolder, action + ".sh");
    	        File template = new File(templatesFolder, action + ".sh.template");
    	        if( ! template.exists())
    	        	template = new File(templatesFolder, "default.sh.template");

    	        if (script.exists()) {
    	            executeScriptforRestore(script, instance, importChanged, statusChanged, instanceDirectory.getAbsolutePath(), oldInstancePath);

    	        } else if (template.exists()) {
    	            File generated = generateTemplate(template, instance);
    	            if (generated == null || !generated.exists())
    	                throw new IOException("Not able to get the generated file from template for action " + action);

    	            executeScriptforRestore(generated, instance, importChanged, statusChanged, instanceDirectory.getAbsolutePath(), oldInstancePath);
    	            Utils.deleteFilesRecursively( generated );

    	        } else {
    	            this.logger.info("Can not find a script or a template for action " + action);
    	        }
    }


    private void prepareAndExecuteCommand(String action, Instance instance, Import importChanged, InstanceStatus statusChanged)
    throws IOException, InterruptedException {

        this.logger.info("Preparing the invocation of " + action + ".sh for instance " + instance.getName());
        File instanceDirectory = InstanceHelpers.findInstanceDirectoryOnAgent( instance, getPluginName());

        File scriptsFolder = new File(instanceDirectory, SCRIPTS_FOLDER_NAME);
        File templatesFolder = new File(instanceDirectory, TEMPLATES_FOLDER_NAME);

        File script = new File(scriptsFolder, action + ".sh");
        File template = new File(templatesFolder, action + ".sh.template");
        if( ! template.exists())
        	template = new File(templatesFolder, "default.sh.template");

        if (script.exists()) {
            executeScript(script, instance, importChanged, statusChanged, instanceDirectory.getAbsolutePath());

        } else if (template.exists()) {
            File generated = generateTemplate(template, instance);
            if (generated == null || !generated.exists())
                throw new IOException("Not able to get the generated file from template for action " + action);

            executeScript(generated, instance, importChanged, statusChanged, instanceDirectory.getAbsolutePath());
            Utils.deleteFilesRecursively( generated );

        } else {
            this.logger.info("Can not find a script or a template for action " + action);
        }
    }


    /**
     * Generates a file from the template and the instance.
     * @param template
     * @param instance
     * @return
     * @throws IOException
     */
    protected File generateTemplate(File template, Instance instance) throws IOException {
        File generated = File.createTempFile(instance.getName(), ".sh");
        InstanceTemplateHelper.injectInstanceImports(instance, template, generated);
        return generated;
    }


    protected void executeScript(File script, Instance instance, Import importChanged, InstanceStatus statusChanged, String instanceDir)
    throws IOException, InterruptedException {

        String[] command = { "bash", script.getAbsolutePath()};
        Map<String, String> environmentVars = new HashMap<String, String>();
        Map<String, String> vars = formatExportedVars(instance);
        environmentVars.putAll(vars);
        Map<String, String> importedVars = formatImportedVars(instance);
        environmentVars.putAll(importedVars);
        environmentVars.put("ROBOCONF_INSTANCE_NAME", instance.getName());
        environmentVars.put("ROBOCONF_FILES_DIR", new File( instanceDir, FILES_FOLDER_NAME ).getAbsolutePath());
        environmentVars.put("ROBOCONF_SCRIPTS_DIR", new File( instanceDir, SCRIPTS_FOLDER_NAME ).getAbsolutePath());
        // Upon update, retrieve the status of the instance that triggered the update.
        // Should be either DEPLOYED_STARTED or DEPLOYED_STOPPED...
        if(statusChanged != null) {
        	environmentVars.put("ROBOCONF_UPDATE_STATUS", statusChanged.toString());
        }

        // Upon update, retrieve the import that changed
        // (removed when an instance stopped, or added when it started)
        if(importChanged != null) {
        	environmentVars.put("ROBOCONF_IMPORT_CHANGED_INSTANCE_PATH", importChanged.getInstancePath());
        	environmentVars.put("ROBOCONF_IMPORT_CHANGED_COMPONENT", importChanged.getComponentName());
        	for (Entry<String, String> entry : importChanged.getExportedVars().entrySet()) {
        		// "ROBOCONF_IMPORT_CHANGED_ip=127.0.0.1"
        		String vname = VariableHelpers.parseVariableName(entry.getKey()).getValue();
        		importedVars.put("ROBOCONF_IMPORT_CHANGED_" + vname, entry.getValue());
        	}
        }

        int exitCode = ProgramUtils.executeCommand(this.logger, command, environmentVars);
        if(exitCode != 0) {
        	throw new IOException( "Bash script execution failed. Exit code: " + exitCode );
        }
    }
    
    
    protected void executeScriptforRestore(File script, Instance instance, Import importChanged, InstanceStatus statusChanged, String instanceDir, String oldInstancePath)
    	    throws IOException, InterruptedException {

    	        String[] command = { "bash", script.getAbsolutePath()};
    	        Map<String, String> environmentVars = new HashMap<String, String>();
    	        Map<String, String> vars = formatExportedVars(instance);
    	        environmentVars.putAll(vars);
    	        Map<String, String> importedVars = formatImportedVars(instance);
    	        environmentVars.putAll(importedVars);
    	        environmentVars.put("ROBOCONF_INSTANCE_NAME", instance.getName());
    	        environmentVars.put("ROBOCONF_FILES_DIR", new File( instanceDir, FILES_FOLDER_NAME ).getAbsolutePath());
    	        environmentVars.put("ROBOCONF_SCRIPTS_DIR", new File( instanceDir, SCRIPTS_FOLDER_NAME ).getAbsolutePath());
    	        List<String> splittedInstancePath = Utils.splitNicely(oldInstancePath, "/");
    	        String oldInstanceName = splittedInstancePath.get( splittedInstancePath.size()-1 );
    	        environmentVars.put("ROBOCONF_COPIED_FROM", oldInstanceName);
    	        
    	        // Upon update, retrieve the status of the instance that triggered the update.
    	        // Should be either DEPLOYED_STARTED or DEPLOYED_STOPPED...
    	        if(statusChanged != null) {
    	        	environmentVars.put("ROBOCONF_UPDATE_STATUS", statusChanged.toString());
    	        }

    	        // Upon update, retrieve the import that changed
    	        // (removed when an instance stopped, or added when it started)
    	        if(importChanged != null) {
    	        	environmentVars.put("ROBOCONF_IMPORT_CHANGED_INSTANCE_PATH", importChanged.getInstancePath());
    	        	environmentVars.put("ROBOCONF_IMPORT_CHANGED_COMPONENT", importChanged.getComponentName());
    	        	for (Entry<String, String> entry : importChanged.getExportedVars().entrySet()) {
    	        		// "ROBOCONF_IMPORT_CHANGED_ip=127.0.0.1"
    	        		String vname = VariableHelpers.parseVariableName(entry.getKey()).getValue();
    	        		importedVars.put("ROBOCONF_IMPORT_CHANGED_" + vname, entry.getValue());
    	        	}
    	        }

    	        int exitCode = ProgramUtils.executeCommand(this.logger, command, environmentVars);
    	        if(exitCode != 0) {
    	        	throw new IOException( "Bash script execution failed. Exit code: " + exitCode );
    	        }
    }


    private Map<String, String> formatExportedVars(Instance instance) {

    	// The map we will return
        Map<String, String> exportedVars = new HashMap<String, String>();
        for(Entry<String, String> entry : instance.getExports().entrySet()) {
            String vname = VariableHelpers.parseVariableName(entry.getKey()).getValue();
            exportedVars.put(vname, entry.getValue());
        }

        return exportedVars;
    }


    /**
     * Simple vars are formatted a simple way ("myVarName=varValue"), while Imported vars must be formatted in a more complex way.
     * <p>
     * Taking the example of Apache needing workers in the example Apache-Tomcat-SQL,
     * we chose to adopt the following style:
     * <pre>
     * ==========================
     * "workers_size=3"
     * "workers_0_name=tomcat1"
     * "workers_0_ip=127.0.0.1"
     * "workers_0_portAJP=8009"
     * "workers_1_name=tomcat2"
     * .....
     * "workers_2_portAJP=8010"
     * ==========================
     * </pre>
     * With this way of formatting vars, the Bash script will know
     * everything it needs to use these vars
     * </p>
     *
     * @param instance
     * @return
     */
    private Map<String,String> formatImportedVars( Instance instance ) {

        // The map we will return
        Map<String, String> importedVars = new HashMap<String, String>();
        for( Map.Entry<String,Collection<Import>> entry : instance.getImports().entrySet()) {
            Collection<Import> importList = entry.getValue();
            String importTypeName = entry.getKey();

            // For each ImportType, put the number of Import it has, so the script knows
            importedVars.put(importTypeName + "_size", "" + importList.size());

            // Now put each var contained in an Import
            int i = 0;
            for( Import imprt : importList ) {
                // "workers_0_name=tomcat1"

                /*int index = imprt.getInstancePath().lastIndexOf( '/' );
                String instanceName = imprt.getInstancePath().substring( index + 1 );
                importedVars.put(importTypeName + "_" + i + "_name", instanceName);*/

                importedVars.put(importTypeName + "_" + i + "_name", imprt.getInstancePath());
                for (Entry<String, String> entry2 : imprt.getExportedVars().entrySet()) {
                    // "workers_0_ip=127.0.0.1"
                    String vname = VariableHelpers.parseVariableName(entry2.getKey()).getValue();
                    importedVars.put(importTypeName + "_" + i + "_" + vname, entry2.getValue());
                }
                ++i;
            }
        }

        return importedVars;
    }
}
