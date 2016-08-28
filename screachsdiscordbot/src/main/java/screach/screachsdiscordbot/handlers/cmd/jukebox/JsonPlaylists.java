package screach.screachsdiscordbot.handlers.cmd.jukebox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class JsonPlaylists {
	
	public static ArrayList<JsonPlaylist> loadPlaylistFile(File playlistFile) throws FileNotFoundException {
		ArrayList<JsonPlaylist> result = new ArrayList<>();
		JsonObject root;
		
		JsonParser parser = new JsonParser();
		FileReader reader = new FileReader(playlistFile);
		
		root = parser.parse(reader).getAsJsonObject();
		
		for (Map.Entry<String, JsonElement> element : root.entrySet()) {
			 System.out.println("Adding : " + element.getKey() + " -> " + root.getAsJsonArray(element.getKey()));
			result.add(new JsonPlaylist(element.getKey(), root.getAsJsonArray(element.getKey())));
		}
		
		
		return result;
	}
	

}
