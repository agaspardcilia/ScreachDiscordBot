package screach.screachsdiscordbot.handlers.cmd.jukebox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

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

		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}


	public static void savePlaylist(File playlistFile, JsonPlaylist playlist) throws FileNotFoundException {
		JsonObject root;
		
		JsonParser parser = new JsonParser();
		FileReader reader = new FileReader(playlistFile);

		root = parser.parse(reader).getAsJsonObject();

		root.add(playlist.getName(), playlist.getJsonPlaylist());
		
		try {
			JsonWriter writer = new JsonWriter(new FileWriter(playlistFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//TOFO finish this (revmove try catch first).
		
	}
	

}
