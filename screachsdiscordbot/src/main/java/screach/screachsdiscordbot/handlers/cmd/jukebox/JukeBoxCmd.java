package screach.screachsdiscordbot.handlers.cmd.jukebox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.io.FilenameUtils;

import screach.screachsdiscordbot.handlers.MessageHandler;
import screach.screachsdiscordbot.util.FailedToLoadSettingsException;
import screach.screachsdiscordbot.util.MessageUtils;
import screach.screachsdiscordbot.util.Settings;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.AudioPlayer.Track;

public class JukeBoxCmd  implements MessageHandler {
	private final static String[] SUPPPORTED_FORMAT = {"mp3", "ogg"};


	private boolean isEnabled;

	private IDiscordClient bot;
	private ArrayList<AudioPlayer> audioPlayers;
	
	private Executor executor;

	public JukeBoxCmd(IDiscordClient bot) {
		audioPlayers = new ArrayList<>();
		this.bot = bot;
		isEnabled = false;
		executor = Executors.newSingleThreadExecutor();
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

		if (args.length < 2)
			result += getJbError("Missing command (type \"!jb help\" the list of available commands)");
		else {
			cmd = args[1];

			switch (cmd) {
			case "help":
				result += getHelpAnswer();
				break;
			case "list":
				result += getListAnswer(event);
				break;
			case "enable":
				result += enableJukebox(event);
				break;
			case "disable":
				result += disableJukebox(event);
				break;
			case "skip":
				result += skip(event);
				break;
			case "play":
				result += play(event, args);
				break;
			case "pause":
				result += pause(event);
				break;
			case "vol":
				result += setVolume(event, args);
				break;
			case "summon":
				result += summon(event);
				break;
			case "revoke":
				result += revoke(event);
				break;
			case "add":
				result += addTrack(event, args);
				break;
			case "current":
				result += currentTrack(event);
				break;
			default:
				result += getJbError("Unkown command \"" + cmd + "\"(type \"!jb help\" the list of available commands)");
				break;
			}
		}

		try {
			MessageUtils.sendMessage(event.getMessage().getChannel(), result);
		} catch (MissingPermissionsException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method returns an error notice.
	 * @param notice Message to display.
	 * @return Error message.
	 */
	private String getJbError(String notice) {
		return "Jukebox error : " + notice;
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

	/**
	 * This method returns the list of music in playlist.
	 * @param event Command's event.
	 * @return List of playlist tracks.
	 */
	private String getListAnswer(MessageReceivedEvent event) {
		String result = "__**Jukebox music list**__\n";
		IGuild guild = event.getMessage().getGuild();
		AudioPlayer player = getAudioPlayer(guild);
		List<Track> pl = player.getPlaylist();

		int i = 0;
		for (Track track : pl) {
			if(i%2 == 0)
				result += i + "\t\t" + getTrackName(track) + "\n";
			else
				result += "**" + i + "\t\t" + getTrackName(track) + "**\n";
			i++;
		}
		return result;
	}

	/**
	 * This method loads tracks in playlist and make the bot join the jukebox voice channel.
	 * @param event Command's event.
	 * @return Command answer.
	 */
	private String enableJukebox(MessageReceivedEvent event) {
		//Check if the jukebox is already enable.
		if (isEnabled)
			return "The jukebox is already enabled.";
		
		String voiceChannelName = Settings.crtInstance.getValue("musicchannel");
		IVoiceChannel voiceChannel;
		String result = "";
		IGuild guild;
		AudioPlayer player = null;
		List<IVoiceChannel> voices;

		int localTracks = 0; //Track count.

		guild = event.getMessage().getGuild();
		voices = guild.getVoiceChannelsByName(voiceChannelName);
		
		//Check if the jukebox voice channel exists. Otherwise, it creates a new channel.
		if (voices.size() == 0) {
			try {
				voiceChannel = guild.createVoiceChannel(voiceChannelName);
			} catch (RateLimitException | DiscordException | MissingPermissionsException e1) {
				return getJbError("cannot create jukebox channel (" + e1.getMessage() + ").");
			}
		} else {
			voiceChannel = voices.get(0);
		}

		//Join the jukebox channel.
		try {
			voiceChannel.join();
		} catch (MissingPermissionsException e1) {
			return getJbError("cannot join the jukebox channel (" + e1.getMessage() + ").");
		}


		player = getAudioPlayer(guild);

		//Loads the local tracks.
		for (File f : getMusicList()) {
			try {
				player.queue(f);
				localTracks++;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			}
		}
		
		//Setup player.
		player.setPaused(true);
		player.setVolume(Float.parseFloat(Settings.crtInstance.getValue("vol")));
		player.setLoop(true);
		
		//Creates answer.
		result += "Jukebox enabled.\n";
		result += localTracks + " tracks in playlist.\n";
		result += "Current song : **" + getTrackName(player.getCurrentTrack()) + "**";

		isEnabled = true;

		return result;
	}

	/**
	 * This method disables the jukebox.
	 * @param event Command's event.
	 * @return Command answer.
	 */
	private String disableJukebox(MessageReceivedEvent event) {
		IGuild guild = event.getMessage().getGuild();
		AudioPlayer player = getAudioPlayer(guild);

		player.setPaused(true);
		player.clear();
		//Find the right voice channel.
		for (IVoiceChannel chan : bot.getConnectedVoiceChannels()) {
			if (chan.getGuild().equals(guild))
				chan.leave();
		}

		isEnabled = false;
		return "Jukebox is now disabled.";
	}

	/**
	 * This method skips the current track.
	 * @param event Command's event.
	 * @return Command answer.
	 */
	private String skip(MessageReceivedEvent event) {
		AudioPlayer player = getAudioPlayer(event.getMessage().getGuild());

		player.skip();

		return "Now playing : **" + getTrackName(player.getCurrentTrack()) + "**";
	}

	/**
	 * This method play the current song or skips to the given track.
	 * @param event Command's event.
	 * @param args Command's arguments.
	 * @return Command answer.
	 */
	private String play(MessageReceivedEvent event, String[] args) {
		//Check if the jukebox is enable.
		if (!isEnabled) {
			return getJbError(" use \"enable\" first.");
		}

		String result = "";
		AudioPlayer player = getAudioPlayer(event.getMessage().getGuild());
		int track;

		player.setPaused(false);

		//Check argument validity and switch to the given track.
		if (args.length >= 3) {
			try {
				track = Integer.parseInt(args[2]);
				player.skipTo(track%player.getPlaylistSize());
			} catch (NumberFormatException e) {
				result += getJbError("Unkown argument \"" + args[2] + "\"");
			}

		}

		result += "Now playing : **" + getTrackName(player.getCurrentTrack()) + "**";

		return result;
	}

	/**
	 * This method returns the given guild AudioPlayer.
	 * @param guild Guild
	 * @return Guild AudioPlayer.
	 */
	private AudioPlayer getAudioPlayer(IGuild guild) {
		AudioPlayer result = null;
		
		for (AudioPlayer audioPlayer : audioPlayers) {
			if (audioPlayer.getGuild().equals(guild))
				result = audioPlayer; 
		}

		if (result == null) {
			result = AudioPlayer.getAudioPlayerForGuild(guild);
			audioPlayers.add(result);
		}

		return result;
	}

	/**
	 * This method returns all the supported audio files specified in the music folder.
	 * @return List of supported audio files.
	 */
	private File[] getMusicList() {
		ArrayList<File> tmp = new ArrayList<>();
		File[] result;
		File directory = new File(Settings.crtInstance.getValue("musicpath"));
		String fExt;


		for (File file : directory.listFiles()) {
			if (!file.isDirectory()) {
				fExt = FilenameUtils.getExtension(file.getName());

				if(isFormatSupported(fExt)) 
					tmp.add(file);
			}
		}

		result = new File[tmp.size()];

		for (int i = 0; i < result.length; i++) {
			result[i] = tmp.get(i);
		}

		return result;
	}

	/**
	 * This method check if the given extension is known as a supported audio format.
	 * @param format File extension.
	 * @return true = supported, false = unsupported.
	 */
	private boolean isFormatSupported(String format) {
		boolean result = false;


		for (String string : SUPPPORTED_FORMAT) {
			if(format.equals(string))
				return true;
		}
		return result;
	}

	/**
	 * This method looks the track metadata and returns a formated track name.
	 * @param track Track who needs a name.
	 * @return Track name.
	 */
	private String getTrackName(Track track) {
		String result;
		String filename = null;

		if (track.getMetadata().get("file") != null)
			filename = track.getMetadata().get("file").toString();
		else if (track.getMetadata().get("url") != null)
			filename = track.getMetadata().get("url").toString();
		else 
			return "Unkown";

		result = FilenameUtils.getBaseName(filename);

		return result;
	}

	/**
	 * This method set the given guild AudioPlayer volume.
	 * @param event Command's event.
	 * @param args Command's arguments.
	 * @return Command answer.
	 */
	private String setVolume(MessageReceivedEvent event, String[] args) {
		String result = "";
		AudioPlayer player = getAudioPlayer(event.getMessage().getGuild());
		int volume;

		if (args.length >= 3) {
			try {
				volume = Integer.parseInt(args[2]);
				if (volume >= 0 && volume <= 100) {
					player.setVolume((float)(volume/100.0));
					result += "Jukebox volume is now : " + volume;
				} else {
					result += "Jukebox error : invalid volume value \"" + volume + "\". This value must be between 0 and 100.";
				}
			} catch (NumberFormatException e) {
				result += getJbError("Unkown argument \"" + args[2] + "\"");
			}

		} else {
			result += getJbError("Desired volume missing.");
		}


		return result;
	}

	/**
	 * Pause the given guild jukebox.
	 * @param event Command's event.
	 * @return Command answer.
	 */
	private String pause(MessageReceivedEvent event) {
		getAudioPlayer(event.getMessage().getGuild()).setPaused(true);
		return "The jukebox is now paused.";
	}

	/**
	 * This method switches the bot to the client's voice channel that requested that command.
	 * @param event Command's envent.
	 * @return Command answer.
	 */
	private String summon(MessageReceivedEvent event) {
		IGuild guild = event.getMessage().getGuild();
		List<IVoiceChannel> voices;
		IVoiceChannel voice = null;

		voices = event.getMessage().getAuthor().getConnectedVoiceChannels();

		for (IVoiceChannel iVoiceChannel : voices) {
			if (iVoiceChannel.getGuild().equals(guild)) {
				voice = iVoiceChannel;
				break;
			}
		}

		if (voice == null)
			return getJbError("You must be connected to a voice channel to do that.");


		try {
			voice.join();
		} catch (MissingPermissionsException e) {
			return getJbError("I don't have the permission to do that.");
		}

		return "The jukebox is moving...";

	}

	/**
	 * This method switch back the bot to the jukebox channel.
	 * @param event Command event.
	 * @return Command answer.
	 */
	private String revoke(MessageReceivedEvent event) {
		String voiceChannelName = Settings.crtInstance.getValue("musicchannel");
		IVoiceChannel voiceChannel;
		IGuild guild = event.getMessage().getGuild();
		List<IVoiceChannel>voices = guild.getVoiceChannelsByName(voiceChannelName);

		if (voices.size() == 0) {
			try {
				voiceChannel = guild.createVoiceChannel(voiceChannelName);
			} catch (RateLimitException | DiscordException | MissingPermissionsException e1) {
				return getJbError("Cannot create jukebox channel (" + e1.getMessage() + ").");
			}
		} else {
			voiceChannel = voices.get(0);
		}

		try {
			voiceChannel.join();
		} catch (MissingPermissionsException e1) {
			return getJbError("Cannot join the jukebox channel (" + e1.getMessage() + ").");
		}
		return "The jukebox is going back to " + voiceChannelName + ".";
	}

	/**
	 * This method loads external tracks urls.
	 * @return List of Url in the external track file.
	 * @throws FailedToLoadSettingsException Can't load external track file.
	 */
	private ArrayList<String> loadExtTracksUrls() throws FailedToLoadSettingsException {
		ArrayList<String> result = new ArrayList<>();
		File trackFile;
		Scanner sc;

		try {
			trackFile = new File(Settings.crtInstance.getValue("songlistfile"));

			sc = new Scanner(trackFile);

			while (sc.hasNextLine()) {
				result.add(sc.nextLine());
			}

			sc.close();

		} catch (FileNotFoundException e) {
			throw new FailedToLoadSettingsException(getJbError("Failed to load external track list file"));
		}


		return result;
	}
	
	/**
	 * This method adds a track by it's given Youtube url.
	 * @param event Command's event.
	 * @param args Command's arguments.
	 * @return Command answer.
	 */
	private String addTrack(MessageReceivedEvent event, String[] args) {
		if (!isEnabled)
			return getJbError("Use !enable first.");
		
		String result = "";
		AudioPlayer player = getAudioPlayer(event.getMessage().getGuild());
		URL trackUrl;
		YoutubeAudioTrack converter;
		
		
		if (args.length >= 3) {
			try {
				trackUrl = new URL(args[2]);
				
				converter = new YoutubeAudioTrack(trackUrl, player);
				
				executor.execute(converter);
				
				result += "This track will be added as soon as the video is downloaded and converted.";
				
			} catch (MalformedURLException e) {
				return getJbError("Malformed URL.");
			} catch (InvalidYoutubeURL e) {
				return getJbError("This is not a correct Youtube url.");
			}
		} else {
			result += getJbError("No argument.");
		}


		return result;
	}

	/**
	 * This method returns the current playing track.
	 * @param event Command's event.
	 * @return Command answer.
	 */
	private String currentTrack(MessageReceivedEvent event) {
		AudioPlayer player = getAudioPlayer(event.getMessage().getGuild());
		String result = "";
		Track track = null;
		
		
		if (!isEnabled) {
			return getJbError("Use \"enable\" command first.");
		}
		
		track = player.getCurrentTrack();
		
		if (track == null) {
			result += "No selected track.";
		} else {
			result += "Now playing **" + getTrackName(track) + "**";
		}
		
		return result;
	}
	
}
