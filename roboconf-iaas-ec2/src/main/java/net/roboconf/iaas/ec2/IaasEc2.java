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

package net.roboconf.iaas.ec2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.roboconf.core.agents.DataHelpers;
import net.roboconf.iaas.api.IaasException;
import net.roboconf.iaas.api.IaasInterface;
import net.roboconf.iaas.ec2.internal.Ec2Constants;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AttachVolumeRequest;
import com.amazonaws.services.ec2.model.AttachVolumeResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateVolumeRequest;
import com.amazonaws.services.ec2.model.CreateVolumeResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumeAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.EbsBlockDevice;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.elasticmapreduce.model.InstanceState;

/**
 * @author Noël - LIG
 */
public class IaasEc2 implements IaasInterface {

	private Logger logger;
	private AmazonEC2 ec2;
	private Map<String, String> iaasProperties;


	/**
	 * Constructor.
	 */
	public IaasEc2() {
		this.logger = Logger.getLogger( getClass().getName());
	}


	/**
	 * @param logger the logger to set
	 */
	public void setLogger( Logger logger ) {
		this.logger = logger;
	}


	/*
	 * (non-Javadoc)
	 * @see net.roboconf.iaas.api.IaasInterface
	 * #setIaasProperties(java.util.Properties)
	 */
	@Override
	public void setIaasProperties(Map<String, String> iaasProperties) throws IaasException {

		// Check the properties
		parseProperties(iaasProperties );
		this.iaasProperties = iaasProperties;

		// Configure the IaaS client
		AWSCredentials credentials = new BasicAWSCredentials(
				iaasProperties.get(Ec2Constants.EC2_ACCESS_KEY),
				iaasProperties.get(Ec2Constants.EC2_SECRET_KEY));

		this.ec2 = new AmazonEC2Client( credentials );
		this.ec2.setEndpoint( iaasProperties.get(Ec2Constants.EC2_ENDPOINT));
	}


	/*
	 * (non-Javadoc)
	 * @see net.roboconf.iaas.api.IaasInterface
	 * #createVM(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public String createVM(
			String messagingIp,
			String messagingUsername,
			String messagingPassword,
			String rootInstanceName,
			String applicationName )
	throws IaasException {

		String instanceId = null;
		try {
			String userData = DataHelpers.writeIaasDataAsString( messagingIp, messagingUsername, messagingPassword, applicationName, rootInstanceName );
			RunInstancesRequest runInstancesRequest = prepareEC2RequestNode(
					this.iaasProperties.get(Ec2Constants.AMI_VM_NODE),
					userData );

			RunInstancesResult runInstanceResult = this.ec2.runInstances( runInstancesRequest );
			instanceId = runInstanceResult.getReservation().getInstances().get( 0 ).getInstanceId();

			// Is there any volume (ID or name) to attach ?
			String snapshotIdToAttach = iaasProperties.get(Ec2Constants.VOLUME_SNAPSHOT_ID);
			if(snapshotIdToAttach != null) {
				boolean running = false;
				while(! running) {
					DescribeInstancesRequest dis = new DescribeInstancesRequest();
					ArrayList<String> instanceIds = new ArrayList<String>();
					instanceIds.add(instanceId);
					dis.setInstanceIds(instanceIds);
					DescribeInstancesResult disresult = ec2.describeInstances(dis);
					running = "running".equals(disresult.getReservations().get(0).getInstances().get(0).getState().getName());
					if(! running) {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				CreateVolumeRequest createVolumeRequest = new CreateVolumeRequest()
					.withAvailabilityZone("eu-west-1c")
					.withSnapshotId(snapshotIdToAttach);
					//.withSize(2); // The size of the volume, in gigabytes.

				CreateVolumeResult createVolumeResult = ec2.createVolume(createVolumeRequest);
				
				running = false;
				while(! running) {
					DescribeVolumesRequest dvs = new DescribeVolumesRequest();
					ArrayList<String> volumeIds = new ArrayList<String>();
					volumeIds.add(createVolumeResult.getVolume().getVolumeId());
					DescribeVolumesResult dvsresult = ec2.describeVolumes(dvs);
					running = "available".equals(dvsresult.getVolumes().get(0).getState());
					System.out.println(dvsresult.getVolumes().get(0).getState());
					if(! running) {
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				AttachVolumeRequest attachRequest = new AttachVolumeRequest()
					.withInstanceId(instanceId)
					.withDevice("/dev/sda2")
					.withVolumeId(createVolumeResult.getVolume().getVolumeId());

				AttachVolumeResult attachResult = ec2.attachVolume(attachRequest);
			}
			
			// Set name tag for instance (human-readable in AWS webapp)
			List<Tag> tags = new ArrayList<Tag>();
			Tag t = new Tag();
			t.setKey("Name");
			t.setValue(applicationName + "." + rootInstanceName);
			tags.add(t);
			CreateTagsRequest ctr = new CreateTagsRequest();
			ctr.setTags(tags);
			ctr.withResources(instanceId);
			this.ec2.createTags(ctr);

		} catch( AmazonServiceException e ) {
			this.logger.severe( "An error occurred on Amazon while instantiating a machine. " + e.getMessage());
			throw new IaasException( e );

		} catch( AmazonClientException e ) {
			this.logger.severe( "An error occurred while creating a machine on Amazon EC2. " + e.getMessage());
			throw new IaasException( e );

		} catch( UnsupportedEncodingException e ) {
			this.logger.severe( "An error occurred while contacting Amazon EC2. " + e.getMessage());
			throw new IaasException( e );

		} catch( IOException e ) {
			this.logger.severe( "An error occurred while preparing the user data. " + e.getMessage());
			throw new IaasException( e );
		}

		return instanceId;
	}


	/*
	 * (non-Javadoc)
	 * @see net.roboconf.iaas.api.IaasInterface
	 * #terminateVM(java.lang.String)
	 */
	@Override
	public void terminateVM( String instanceId ) throws IaasException {
		try {
			TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
			terminateInstancesRequest.withInstanceIds( instanceId );
			this.ec2.terminateInstances( terminateInstancesRequest );

		} catch( AmazonServiceException e ) {
			this.logger.severe( "An error occurred on Amazon while terminating the machine. " + e.getMessage());
			throw new IaasException( e );

		} catch( AmazonClientException e ) {
			this.logger.severe( "An error occurred while terminating a machine on Amazon EC2. " + e.getMessage());
			throw new IaasException( e );
		}
	}


	/**
	 * Parses the properties and saves them in a Java bean.
	 * @param iaasProperties the IaaS properties
	 * @throws InvalidIaasPropertiesException
	 */
	private void parseProperties( Map<String, String> iaasProperties ) throws IaasException {

		// Quick check
		String[] properties = {
			Ec2Constants.EC2_ENDPOINT,
			Ec2Constants.EC2_ACCESS_KEY,
			Ec2Constants.EC2_SECRET_KEY,
			Ec2Constants.AMI_VM_NODE,
			Ec2Constants.VM_INSTANCE_TYPE,
			Ec2Constants.SSH_KEY_NAME,
			Ec2Constants.SECURITY_GROUP_NAME
		};

		for( String property : properties ) {
			if( StringUtils.isBlank( iaasProperties.get( property )))
				throw new IaasException( "The value for " + property + " cannot be null or empty." );
		}
	}

	/**
	 * Prepares the request.
	 * @param machineImageId the ID of the image to use
	 * @param userData the user data to pass
	 * @return a request
	 * @throws UnsupportedEncodingException
	 */
	private RunInstancesRequest prepareEC2RequestNode( String machineImageId, String userData )
	throws UnsupportedEncodingException {

		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
		String flavor = this.iaasProperties.get(Ec2Constants.VM_INSTANCE_TYPE);
		if(StringUtils.isBlank(flavor)) flavor = "t1.micro";
		runInstancesRequest.setInstanceType( this.iaasProperties.get(Ec2Constants.VM_INSTANCE_TYPE));
		if( StringUtils.isBlank( machineImageId ))
			runInstancesRequest.setImageId( this.iaasProperties.get(Ec2Constants.AMI_VM_NODE));
		else
			runInstancesRequest.setImageId( machineImageId );

		// FIXME (VZ): why this kernel ID?
		runInstancesRequest.setKernelId( "aki-62695816" );
		runInstancesRequest.setMinCount( 1 );
		runInstancesRequest.setMaxCount( 1 );
		runInstancesRequest.setKeyName( this.iaasProperties.get(Ec2Constants.SSH_KEY_NAME));
		String secGroup = this.iaasProperties.get(Ec2Constants.SECURITY_GROUP_NAME);
		if(StringUtils.isBlank(secGroup)) secGroup = "default";
		runInstancesRequest.setSecurityGroups(Arrays.asList(secGroup));

/*
		// Create the block device mapping to describe the root partition.
		BlockDeviceMapping blockDeviceMapping = new BlockDeviceMapping();
		blockDeviceMapping.setDeviceName("/dev/sda1");

		// Set the delete on termination flag to false.
		EbsBlockDevice ebs = new EbsBlockDevice();
		ebs.setSnapshotId(snapshotId);
		ebs.setDeleteOnTermination(Boolean.FALSE);

		blockDeviceMapping.setEbs(ebs);

		// Add the block device mapping to the block list.
		ArrayList<BlockDeviceMapping> blockList = new ArrayList<BlockDeviceMapping>();
		blockList.add(blockDeviceMapping);

		// Set the block device mapping configuration in the launch specifications.
		runInstancesRequest.setBlockDeviceMappings(blockList);
*/


		// The following part enables to transmit data to the VM.
		// When the VM is up, it will be able to read this data.
		String encodedUserData = new String( Base64.encodeBase64( userData.getBytes( "UTF-8" )), "UTF-8" );
		runInstancesRequest.setUserData( encodedUserData );

		return runInstancesRequest;
	}
}
