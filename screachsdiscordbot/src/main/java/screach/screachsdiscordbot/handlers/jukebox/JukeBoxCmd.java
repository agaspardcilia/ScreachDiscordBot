package screach.screachsdiscordbot.handlers.jukebox;

import screach.screachsdiscordbot.handlers.MessageHandler;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class JukeBoxCmd  implements MessageHandler {
	
	
	
	public String getName() {
		return "Jukebox";
	}

	public String getDescription() {
		return "Jukebox controls.";
	}

	public String getUsage() {
		return "jb <command> [args]";
	}

	public String getCommand() {
		return "jb";
	}

	public void handleMessage(MessageReceivedEvent event, String[] args) {
		String result = "";
		String cmd;
		
		if (args.length < 2)
			result += getJbError("Missing command (type \"!jb help\" the list of available commands)");
		else {
			cmd = args[1];
			
			switch (cmd) {
			case "help":
				result += getHelpAnswer();
				break;

			default:
				result += getJbError("Unkown command \"" + cmd + "\"(type \"!jb help\" the list of available commands)");
				break;
			}
		}
		
		try {
			event.getMessage().getChannel().sendMessage(result);
		} catch (RateLimitException | MissingPermissionsException | DiscordException e) {
			e.printStackTrace();
		}
	}
	
	
	private String getJbError(String notice) {
		return "Jukebox error : " + notice;
	}
	
	private String getAck() {
		return "Jukebox Ack.";
	}
	
	private String getHelpAnswer() {
		String result = "Jukebox help\n---------------\n";
		result += "<argument> : required, [argument] : optionnal\n\n";
		
		result += "play [song number], plays a song. If a song is paused, this command will resume playing. If no song is selected, a random song will be played.\n";
		result += "pause, pause the current playing song.\n";
		result += "list, displays a list of available songs.\n";
		result += "enable, enable jukebox.\n";
		result += "disable, disable jukebox.\n";
		result += "---------------";
		
		return result;
	}
	
	private String getListAnswer() {
		String result = "";
		
		//TODO
		
		return result;
	}

}
