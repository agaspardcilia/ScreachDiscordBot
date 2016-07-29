package screach.screachsdiscordbot.handlers;

import sx.blah.discord.handle.impl.events.MessageReceivedEvent;

public class JukeBoxCmd  implements MessageHandler {

	public String getName() {
		return "Jukebox";
	}

	public String getDescription() {
		return "Used to control the jukebox.";
	}

	public String getUsage() {
		return "jb <command> [args]";
	}

	public String getCommand() {
		return "jb";
	}

	public void handleMessage(MessageReceivedEvent event, String[] args) {
		//TODO
	}
	
	private String getHelpAnswer() {
		String result = "Jukebox help\n---------------\n";
		result += "<argument> : required, [argument] : optionnal\n\n";
		
		result += "play [song number], plays a song. If a song is paused, this command will resume playing. If no song is selected, a random song will be played.";
		result += "pause, pause the current playing song.";
		result += "list, displays a list of available songs.";
		return result;
	}

}
