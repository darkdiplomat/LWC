package com.griefcraft.util;

import com.griefcraft.logging.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
 
public class Config extends Properties{

	private static final long serialVersionUID = -1683098738922159272L;
	private Logger logger = Logger.getLogger(getClass().getSimpleName());
	private static Config instance;
	private String NL = System.getProperty("line.separator");
 
	public static void destroy(){
		instance = null;
	}
 
	public static Config getInstance(){
		return instance;
	}

	public static void init(){
		if (instance == null){
			instance = new Config();
		}
	}
 
	private Config(){
		for (ConfigValues value : ConfigValues.values()) {
			setProperty(value.getName(), value.getDefaultValue());
		}

		try{
			File conf = new File("lwc.properties");

			if (!conf.exists()) {
				save();
				return;
			}

			InputStream inputStream = new FileInputStream(conf);
			load(inputStream);
			inputStream.close();

			this.logger.info("Loaded " + size() + " config entries");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void save() {
		try {
			File file = new File("lwc.properties");

			if (!file.exists()) {
				file.createNewFile();
			}

			OutputStream outputStream = new FileOutputStream(file);
			
			store(outputStream, "# LWC configuration file "+NL+NL+"# + Github project page: https://github.com/darkdiplomat/LWC "+NL+"# + Canary thread link: http://forums.canarymod.net/?topic=22.0"+NL);
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
