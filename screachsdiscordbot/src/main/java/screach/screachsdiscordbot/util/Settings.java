package screach.screachsdiscordbot.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class Settings {
	private final static String CFG_FILE_PATH = "botcfg.json";
	
	private final static String ERR_FILE_NOT_FOUND = "Error : File not found";
	
	
	public static Settings crtInstance;
	
	private Map<String, String> variables;
	

	public static void init() throws FailedToLoadSettingsException {
		crtInstance = new Settings();
		crtInstance.loadSettings();
	}
	
	public Settings() {
		variables = new HashMap<String, String>();
	}
	
	private void loadSettings() throws FailedToLoadSettingsException {
		JsonParser parser;
		FileReader fr;
		File cfgFile;
		JsonElement root;
		
		try {
			cfgFile = new File(CFG_FILE_PATH);
			fr = new FileReader(cfgFile);
			
			
			parser = new JsonParser();
			root = parser.parse(fr);
			
			for (Entry<String, JsonElement> crt : root.getAsJsonObject().entrySet()) {
				variables.put(crt.getKey(), crt.getValue().getAsString());
			}
			
			
		} catch (FileNotFoundException e) {
			throw new FailedToLoadSettingsException(ERR_FILE_NOT_FOUND);
		}
	}
	
	public String getValue(String name) {
		return variables.get(name);
	}
	
	
}














