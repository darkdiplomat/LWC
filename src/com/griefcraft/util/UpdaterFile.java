package com.griefcraft.util;
 
public class UpdaterFile{
	private String remoteLocation;
	private String localLocation;

	public UpdaterFile(String location){
		this.remoteLocation = location;
		this.localLocation = location;
	}
		
	public String getRemoteLocation(){
		return this.remoteLocation;
	}
	
	public String getLocalLocation(){
		return this.localLocation;
	}
	
	public void setRemoteLocation(String remoteLocation){
		this.remoteLocation = remoteLocation;
	}

	public void setLocalLocation(String localLocation){
		this.localLocation = localLocation;
	}
}
