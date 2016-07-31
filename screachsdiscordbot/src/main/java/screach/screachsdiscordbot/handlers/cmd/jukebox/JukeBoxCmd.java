package screach.screachsdiscordbot.handlers.cmd.jukebox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.ls.LSInput;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

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
	private ArrayList<IVoiceChannel> voiceChannels;
	private ArrayList<String> extTrack;

	public JukeBoxCmd(IDiscordClient bot) {
		audioPlayers = new ArrayList<>();
		voiceChannels= new ArrayList<>();
		extTrack = new ArrayList<>();
		this.bot = bot;
		isEnabled = false;
	}

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


	private String getJbError(String notice) {
		return "Jukebox error : " + notice;
	}

	private String getAck() {
		return "Jukebox Ack.";
	}

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
		result += "**add <track's url>**, add a track.\n";

		return result;
	}

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

	private String enableJukebox(MessageReceivedEvent event) {
		if (isEnabled)
			return "The jukebox is already enabled.";
		String voiceChannelName = Settings.crtInstance.getValue("musicchannel");
		IVoiceChannel voiceChannel;
		String result = "";
		IGuild guild;
		AudioPlayer player = null;
		List<IVoiceChannel> voices;

		int extTrack = 0, localTracks = 0;

		guild = event.getMessage().getGuild();
		voices = guild.getVoiceChannelsByName(voiceChannelName);

		if (voices.size() == 0) {
			try {
				voiceChannel = guild.createVoiceChannel(voiceChannelName);
			} catch (RateLimitException | DiscordException | MissingPermissionsException e1) {
				return "Jukebox error : cannot create jukebox channel (" + e1.getMessage() + ").";
			}
		} else {
			voiceChannel = voices.get(0);
		}

		try {
			voiceChannel.join();
		} catch (MissingPermissionsException e1) {
			return "Jukebox error : cannot join the jukebox channel (" + e1.getMessage() + ").";
		}


		player = getAudioPlayer(guild);


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

		try {
			ArrayList<String> extTracks = loadExtTracksUrls();

			for (String crt : extTracks) {
				try {
					player.queue(new URL(crt));
					extTrack++;
				} catch (IOException | UnsupportedAudioFileException e) {
					System.err.println("Can't load " + crt + ".");
					e.printStackTrace();
				}
			}

		} catch (FailedToLoadSettingsException e) {
			System.err.println("Can't load external tracks file.");
			e.printStackTrace();
		}

		player.setPaused(true);
		player.setVolume(Float.parseFloat(Settings.crtInstance.getValue("vol")));
		player.setLoop(true);
		result += "Jukebox enabled.\n";
		result += (extTrack + localTracks) + " tracks in playlist (" + localTracks + " locals, " + extTrack + " externals).\n";
		result += "Now playing : **" + getTrackName(player.getCurrentTrack()) + "**";

		isEnabled = true;

		return result;
	}

	private String disableJukebox(MessageReceivedEvent event) {
		IGuild guild = event.getMessage().getGuild();
		AudioPlayer player = getAudioPlayer(guild);

		player.setPaused(true);
		player.clear();
		for (IVoiceChannel chan : bot.getConnectedVoiceChannels()) {
			if (chan.getGuild().equals(guild))
				chan.leave();
		}

		isEnabled = false;
		return "Jukebox is now disabled.";
	}


	private String skip(MessageReceivedEvent event) {
		AudioPlayer player = getAudioPlayer(event.getMessage().getGuild());

		player.skip();

		return "Now playing : **" + getTrackName(player.getCurrentTrack()) + "**";
	}

	private String play(MessageReceivedEvent event, String[] args) {
		if (!isEnabled) {
			return "Jukebox error : use \"enable\" first.";
		}

		String result = "";
		AudioPlayer player = getAudioPlayer(event.getMessage().getGuild());
		int track;

		player.setPaused(false);


		if (args.length >= 3) {
			try {
				track = Integer.parseInt(args[2]);
				player.skipTo(track%player.getPlaylistSize());
			} catch (NumberFormatException e) {
				result += "Jukebox error : Unkown argument \"" + args[2] + "\"";
			}

		}

		result += "Now playing : **" + getTrackName(player.getCurrentTrack()) + "**";

		return result;
	}

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

	private boolean isFormatSupported(String format) {
		boolean result = false;


		for (String string : SUPPPORTED_FORMAT) {
			if(format.equals(string))
				return true;
		}
		return result;
	}

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
				result += "Jukebox error : Unkown argument \"" + args[2] + "\"";
			}

		} else {
			result += "Jukebox error : Desired volume missing.";
		}


		return result;
	}

	private String pause(MessageReceivedEvent event) {
		getAudioPlayer(event.getMessage().getGuild()).setPaused(true);
		return "The jukebox is now paused.";
	}

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
			return "Jukebox error : You must be connected to a voice channel to do that.";


		try {
			voice.join();
		} catch (MissingPermissionsException e) {
			return "Jukebox error : I don't have the permission to do that.";
		}

		return "The jukebox is moving...";

	}

	private String revoke(MessageReceivedEvent event) {
		String voiceChannelName = Settings.crtInstance.getValue("musicchannel");
		IVoiceChannel voiceChannel;
		IGuild guild = event.getMessage().getGuild();
		List<IVoiceChannel>voices = guild.getVoiceChannelsByName(voiceChannelName);

		if (voices.size() == 0) {
			try {
				voiceChannel = guild.createVoiceChannel(voiceChannelName);
			} catch (RateLimitException | DiscordException | MissingPermissionsException e1) {
				return "Jukebox error : cannot create jukebox channel (" + e1.getMessage() + ").";
			}
		} else {
			voiceChannel = voices.get(0);
		}

		try {
			voiceChannel.join();
		} catch (MissingPermissionsException e1) {
			return "Jukebox error : cannot join the jukebox channel (" + e1.getMessage() + ").";
		}
		return "The jukebox is going back to " + voiceChannelName + ".";
	}

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
			throw new FailedToLoadSettingsException("Jukebox error : Failed to load external track list file");
		}


		return result;
	}

	private String addTrack(MessageReceivedEvent event, String[] args) {
		String result = "";
		AudioPlayer player = getAudioPlayer(event.getMessage().getGuild());
		URL trackUrl;
		Track track;
		
		if (args.length >= 3) {
			try {
				trackUrl = new URL(args[2]);
				
				try {
					track = player.queue(trackUrl);
					addUrlToExtTrackFile(trackUrl.toString());
					result += getTrackName(track) + "has been added to playlist";
				} catch (IOException | UnsupportedAudioFileException e) {
					return "Jukebox error : problem when adding track to playlist. " + e.getMessage();
				}
				
			} catch (MalformedURLException e) {
				return "Jukebox error : Malformed URL.";
			}
		} else {
			result += "Jukebox error : no argument.";
		}


		return result;
	}

	private void addUrlToExtTrackFile(String url) {
		File trackFile;
		PrintWriter printer;
		
		trackFile = new File(Settings.crtInstance.getValue("songlistfile"));
		try {
			printer = new PrintWriter(trackFile);
			printer.println(url);
			printer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	//		public static void main(String[] args) {
	//			JukeBoxCmd jb = new JukeBoxCmd(null);
	//			try {
	//				Settings.init();
	//			} catch (FailedToLoadSettingsException e1) {
	//				// TODO Auto-generated catch block
	//				e1.printStackTrace();
	//			}
	//			try {
	//				ArrayList<String> tracks = jb.loadExtTracksUrls();
	//				
	//				for (String string : tracks) {
	//					System.out.println(string);
	//				}
	//				
	//				
	//			} catch (FailedToLoadSettingsException e) {
	//				// TODO Auto-generated catch block
	//				e.printStackTrace();
	//			}
	//		
	//		}

}
