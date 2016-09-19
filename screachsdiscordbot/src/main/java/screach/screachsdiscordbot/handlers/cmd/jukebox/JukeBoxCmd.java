package screach.screachsdiscordbot.handlers.cmd.jukebox;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import screach.screachsdiscordbot.handlers.MessageHandler;
import screach.screachsdiscordbot.util.MessageUtils;
import screach.screachsdiscordbot.util.Settings;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.MissingPermissionsException;

public class JukeBoxCmd  implements MessageHandler {
	//Arbitrary list, not all actually supported formats are listed/
	public final static String[] SUPPPORTED_FORMAT = {"mp3", "ogg"};

	private final static String PLAYLIST_FILE_VAR = "playlistpath";

	private IDiscordClient bot;
	private ArrayList<JukeBox> audioPlayers;
	private ArrayList<JsonPlaylist> playlists;


	private boolean isAvailable;

	public JukeBoxCmd(IDiscordClient bot) {
		audioPlayers = new ArrayList<>();
		this.bot = bot;
		isAvailable = true;
		try {
			playlists = JsonPlaylists.loadPlaylistFile(new File(Settings.crtInstance.getValue(PLAYLIST_FILE_VAR)));
		} catch (FileNotFoundException | NullPointerException e) {
			e.printStackTrace();
			isAvailable = false;
		}

	}

	@Override
	public String getName() {
		return "Jukebox";
	}

	@Override
	public String getDescription() {
		return "Jukebox controls.";
	}

	@Override
	public String getUsage() {
		return "jb <command> [args]";
	}

	@Override
	public String getCommand() {
		return "jb";
	}

	@Override
	public void handleMessage(MessageReceivedEvent event, String[] args) {
		String result = "";
		String cmd;
		JukeBox jb = null;

		if (args.length < 2)
			result += getJbError("Missing command (type \"!jb help\" the list of available commands)");
		else {
			cmd = args[1];
			jb = getJukeBox(event.getMessage().getGuild());

			if (isAvailable) {
				switch (cmd) {
				case "help":
					result += getHelpAnswer();
					break;
				case "list":
					result += jb.getListAnswer(event);
					break;
				case "enable":
					result += jb.enableJukebox(event);
					break;
				case "disable":
					result += jb.disableJukebox(event);
					break;
				case "skip":
					result += jb.skip(event);
					break;
				case "play":
					result += jb.play(event, args);
					break;
				case "pause":
					result += jb.pause(event);
					break;
				case "vol":
					result += jb.setVolume(event, args);
					break;
				case "summon":
					result += jb.summon(event);
					break;
				case "revoke":
					result += jb.revoke(event);
					break;
				case "add":
					result += jb.addTrack(event, args);
					break;
				case "current":
					result += jb.currentTrack(event);
					break;
				default:
					result += getJbError("Unkown command \"" + cmd + "\"(type \"!jb help\" the list of available commands)");
					break;
				}
			} else {
				result += "Can't start the jukebox. You can report this to **a.gaspardcilia@gmail.com** or by twitter to **@Screach_FR**";
			}
		}

		try {
			MessageUtils.sendMessage(event.getMessage().getChannel(), result);
		} catch (MissingPermissionsException e) {
			e.printStackTrace();
		}
	}


	/**
	 * This method returns the help.
	 * @return Help string.
	 */
	private String getHelpAnswer() {
		String result = "__**Jukebox help**__\n";
		result += "<argument> : required, [argument] : optionnal\n\n";

		result += "**play [song number]**, plays a song. If a song is paused, this command will resume playing. If no song is selected, a random song will be played.\n";
		result += "**pause**, pause the current playing song.\n";
		result += "**list**, displays a list of available songs.\n";
		result += "**enable**, enables jukebox.\n";
		result += "**disable**, disables jukebox.\n";
		result += "**summon**, summons the bot to your channel.\n";
		result += "**revoke**, sends the bot back to the channel Jukebox.\n";
		result += "**skip**, skips current song.\n";
		result += "**current**, display current song.\n";
		result += "**next**, displays next song.\n";
		result += "**vol <0-100>**, sets volume.\n";
		result += "**add <track's url>**, add a track to playlist.\n";

		return result;
	}

	private JukeBox getJukeBox(IGuild guild) {
		JukeBox result = null;

		for (JukeBox jukeBox : audioPlayers) {
			if (jukeBox.getGuild().equals(guild)) {
				result = jukeBox;
				break;
			}
		}

		if (result == null) {
			result = new JukeBox(guild, bot);
			audioPlayers.add(result);
		}

		return result;

	}

	/**
	 * This method returns an error notice.
	 * @param notice Message to display.
	 * @return Error message.
	 */
	public static String getJbError(String notice) {
		return "Jukebox error : " + notice;
	}
}
