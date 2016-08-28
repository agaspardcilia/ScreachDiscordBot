package screach.screachsdiscordbot.handlers.cmd.jukebox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.UnsupportedAudioFileException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import screach.screachsdiscordbot.util.FailedToLoadSettingsException;
import screach.screachsdiscordbot.util.Settings;
import sx.blah.discord.util.audio.AudioPlayer;

public class JsonPlaylist extends ArrayList<String> {
	private static final long serialVersionUID = -6826678388426397001L;

	
	private JsonArray jsonPlaylist;
	private String name;
	
	public JsonPlaylist(String name, JsonArray jsonPlaylist) {
		this.jsonPlaylist = jsonPlaylist;
		this.name = name;
		
		for (JsonElement jsonElement : jsonPlaylist) {
			this.add(jsonElement.getAsString());
		}
		
		
		
	}
	
	
	public void SetPlaylistToAudioPlayer(AudioPlayer player) {
		player.clear();
		
		for (String track : this) {
			try {
				player.queue(new File(track));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			}
		}
	}
	
	public JsonArray getJsonPlaylist() {
		return jsonPlaylist;
	}
	
	public JsonObject toJson() {
		JsonObject result = new JsonObject();
		result.add(name, jsonPlaylist);
		
		return result;
	}
	
	public String toJsonString() {
		
		
		return toJson().toString();
	}
	
	@Override
	public String toString() {
		return toJsonString();
	}
	
	
	
	public static void main(String[] args) {
		try {
			Settings.init();
		
			
			File plFile = new File(Settings.crtInstance.getValue("playlistpath"));
			
			ArrayList<JsonPlaylist> pl = JsonPlaylists.loadPlaylistFile(plFile);
			
			for (JsonPlaylist jsonPlaylist : pl) {
				for (String string : jsonPlaylist) {
					System.out.println(string);
				}
			}
		
		} catch (FailedToLoadSettingsException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
