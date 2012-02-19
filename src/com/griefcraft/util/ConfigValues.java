package com.griefcraft.util;

public enum ConfigValues{
	BLACKLISTED_MODES("blacklisted-modes", ""), 
	ALLOW_FURNACE_PROTECTION("furnace-locks", "true"), 
	DB_PATH("db-path", "lwc.db"),
	//CUBOID_SAFE_AREAS("only-protect-in-cuboid-safe-zones", "false"), DISABLED
	AUTO_UPDATE("auto-update", "true");

	private String name;
	private String defaultValue;

	private ConfigValues(String name, String defaultValue){
		this.name = name;
		this.defaultValue = defaultValue;
	}
	
	public boolean getBool(){
		return getString().equalsIgnoreCase("true");
	}

	public String getDefaultValue(){
		return this.defaultValue;
	}

	public int getInt(){
		return Integer.parseInt(getString());
	}

	public String getName(){
		return this.name;
	}
 
	public String getString(){
		return Config.getInstance().getProperty(this.name, this.defaultValue);
	}
}
