package screach.screachsdiscordbot.handlers.cmd.jukebox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.io.FilenameUtils;

import screach.screachsdiscordbot.util.FailedToLoadSettingsException;
import screach.screachsdiscordbot.util.Settings;
import screach.screachsdiscordbot.util.ThreadPool;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.AudioPlayer.Track;

public class JukeBox {
	private IGuild guild;
	private boolean isEnabled;
	private IDiscordClient bot;
	private AudioPlayer audioPlayer;
	private JsonPlaylist playlist;
	
	public JukeBox(IGuild guild, IDiscordClient bot) {
		this.guild = guild;
		this.bot = bot;
		isEnabled = false;
		audioPlayer = new AudioPlayer(guild);
		
		
	}
	
	/**
	 * This method returns the list of music in playlist.
	 * @param event Command's event.
	 * @return List of playlist tracks.
	 */
	public String getListAnswer(MessageReceivedEvent event) {
		String result = "__**Jukebox music list**__\n";
		AudioPlayer player = audioPlayer;
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
	public String enableJukebox(MessageReceivedEvent event) {
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
				return JukeBoxCmd.getJbError("cannot create jukebox channel (" + e1.getMessage() + ").");
			}
		} else {
			voiceChannel = voices.get(0);
		}

		//Join the jukebox channel.
		try {
			voiceChannel.join();
		} catch (MissingPermissionsException e1) {
			return JukeBoxCmd.getJbError("cannot join the jukebox channel (" + e1.getMessage() + ").");
		}


		player = audioPlayer;

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
	public String disableJukebox(MessageReceivedEvent event) {
		IGuild guild = event.getMessage().getGuild();
		AudioPlayer player = audioPlayer;

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
	public String skip(MessageReceivedEvent event) {
		AudioPlayer player = audioPlayer;

		player.skip();

		return "Now playing : **" + getTrackName(player.getCurrentTrack()) + "**";
	}

	/**
	 * This method play the current song or skips to the given track.
	 * @param event Command's event.
	 * @param args Command's arguments.
	 * @return Command answer.
	 */
	public String play(MessageReceivedEvent event, String[] args) {
		//Check if the jukebox is enable.
		if (!isEnabled) {
			return JukeBoxCmd.getJbError(" use \"enable\" first.");
		}

		String result = "";
		AudioPlayer player = audioPlayer;
		int track;

		player.setPaused(false);

		//Check argument validity and switch to the given track.
		if (args.length >= 3) {
			try {
				track = Integer.parseInt(args[2]);
				player.skipTo(track%player.getPlaylistSize());
			} catch (NumberFormatException e) {
				result += JukeBoxCmd.getJbError("Unkown argument \"" + args[2] + "\"");
			}

		}

		result += "Now playing : **" + getTrackName(player.getCurrentTrack()) + "**";

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


		for (String string : JukeBoxCmd.SUPPPORTED_FORMAT) {
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
	public String setVolume(MessageReceivedEvent event, String[] args) {
		String result = "";
		AudioPlayer player = audioPlayer;
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
				result += JukeBoxCmd.getJbError("Unkown argument \"" + args[2] + "\"");
			}

		} else {
			result += JukeBoxCmd.getJbError("Desired volume missing.");
		}


		return result;
	}

	/**
	 * Pause the given guild jukebox.
	 * @param event Command's event.
	 * @return Command answer.
	 */
	public String pause(MessageReceivedEvent event) {
		audioPlayer.setPaused(true);
		return "The jukebox is now paused.";
	}

	/**
	 * This method switches the bot to the client's voice channel that requested that command.
	 * @param event Command's envent.
	 * @return Command answer.
	 */
	public String summon(MessageReceivedEvent event) {
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
			return JukeBoxCmd.getJbError("You must be connected to a voice channel to do that.");


		try {
			voice.join();
		} catch (MissingPermissionsException e) {
			return JukeBoxCmd.getJbError("I don't have the permission to do that.");
		}

		return "The jukebox is moving...";

	}

	/**
	 * This method switch back the bot to the jukebox channel.
	 * @param event Command event.
	 * @return Command answer.
	 */
	public String revoke(MessageReceivedEvent event) {
		String voiceChannelName = Settings.crtInstance.getValue("musicchannel");
		IVoiceChannel voiceChannel;
		IGuild guild = event.getMessage().getGuild();
		List<IVoiceChannel>voices = guild.getVoiceChannelsByName(voiceChannelName);

		if (voices.size() == 0) {
			try {
				voiceChannel = guild.createVoiceChannel(voiceChannelName);
			} catch (RateLimitException | DiscordException | MissingPermissionsException e1) {
				return JukeBoxCmd.getJbError("Cannot create jukebox channel (" + e1.getMessage() + ").");
			}
		} else {
			voiceChannel = voices.get(0);
		}

		try {
			voiceChannel.join();
		} catch (MissingPermissionsException e1) {
			return JukeBoxCmd.getJbError("Cannot join the jukebox channel (" + e1.getMessage() + ").");
		}
		return "The jukebox is going back to " + voiceChannelName + ".";
	}

	/**
	 * This method loads external tracks urls.
	 * @return List of Url in the external track file.
	 * @throws FailedToLoadSettingsException Can't load external track file.
	 */
	public ArrayList<String> loadExtTracksUrls() throws FailedToLoadSettingsException {
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
			throw new FailedToLoadSettingsException(JukeBoxCmd.getJbError("Failed to load external track list file"));
		}


		return result;
	}
	
	/**
	 * This method adds a track by it's given Youtube url.
	 * @param event Command's event.
	 * @param args Command's arguments.
	 * @return Command answer.
	 */
	public String addTrack(MessageReceivedEvent event, String[] args) {
		if (!isEnabled)
			return JukeBoxCmd.getJbError("Use !enable first.");
		
		String result = "";
		AudioPlayer player = audioPlayer;
		URL trackUrl;
		YoutubeAudioTrack converter;
		
		
		if (args.length >= 3) {
			try {
				trackUrl = new URL(args[2]);
				
				converter = new YoutubeAudioTrack(trackUrl, player);
				
				ThreadPool.getCrtInst().getExecutor().execute(converter);
				
				result += "This track will be added as soon as the video is downloaded and converted.";
				
			} catch (MalformedURLException e) {
				return JukeBoxCmd.getJbError("Malformed URL.");
			} catch (InvalidYoutubeURL e) {
				return JukeBoxCmd.getJbError("This is not a correct Youtube url.");
			}
		} else {
			result += JukeBoxCmd.getJbError("No argument.");
		}


		return result;
	}

	/**
	 * This method returns the current playing track.
	 * @param event Command's event.
	 * @return Command answer.
	 */
	public String currentTrack(MessageReceivedEvent event) {
		AudioPlayer player = audioPlayer;
		String result = "";
		Track track = null;
		
		
		if (!isEnabled) {
			return JukeBoxCmd.getJbError("Use \"enable\" command first.");
		}
		
		track = player.getCurrentTrack();
		
		if (track == null) {
			result += "No selected track.";
		} else {
			result += "Now playing **" + getTrackName(track) + "**";
		}
		
		return result;
	}
	
	public IGuild getGuild() {
		return guild;
	}
	
	public JsonPlaylist getPlaylist() {
		return playlist;
	}
}
