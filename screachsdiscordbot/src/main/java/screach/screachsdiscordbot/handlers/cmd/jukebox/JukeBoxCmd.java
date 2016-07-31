package screach.screachsdiscordbot.handlers.cmd.jukebox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.io.FilenameUtils;

import screach.screachsdiscordbot.handlers.MessageHandler;
import screach.screachsdiscordbot.util.FailedToLoadSettingsException;
import screach.screachsdiscordbot.util.MessageUtils;
import screach.screachsdiscordbot.util.Settings;
import sx.blah.discord.handle.audio.impl.AudioManager;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.audio.AudioPlayer;

public class JukeBoxCmd  implements MessageHandler {
	private final static String[] SUPPPORTED_FORMAT = {"mp3", "ogg"};

	private ArrayList<AudioPlayer> audioPlayers;
	private ArrayList<IVoiceChannel> voiceChannels;

	public JukeBoxCmd() {
		audioPlayers = new ArrayList<>();
		voiceChannels= new ArrayList<>();
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
				result += getListAnswer();
				break;
			case "enable":
				result += enableJukebox(event);
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
		result += "**enable**, enable jukebox.\n";
		result += "**disable**, disable jukebox.\n";

		return result;
	}

	private String getListAnswer() {
		String result = "__**Jukebox music list**__\n";
		File[] files = getMusicList();
		

		for (int i = 0; i < files.length; i++) {
			if(i%2 == 0)
				result += i + "\t\t" + FilenameUtils.getBaseName(files[i].getName()) + "\n";
			else
				result += "**" + i + "\t\t" + FilenameUtils.getBaseName(files[i].getName()) + "**\n";
		}
		return result;
	}

	private String enableJukebox(MessageReceivedEvent event) {
		String result = "";
		IGuild guild;
		AudioPlayer player = null;
		IVoiceChannel voiceChannel;
		List<IVoiceChannel> voices;
		String voiceChannelName = Settings.crtInstance.getValue("musicchannel");
		
		guild = event.getMessage().getGuild();
		voices = guild.getVoiceChannelsByName(voiceChannelName);
		
		if (voices.size() == 0) {
			try {
				guild.createVoiceChannel("Jukebox");
			} catch (RateLimitException | DiscordException | MissingPermissionsException e1) {
				return "Jukebox error : cannot create jukebox channel (" + e1.getMessage() + ").";
			}
		}
		
		voices = guild.getVoiceChannelsByName(voiceChannelName);
		voiceChannel = voices.get(0);
		
		try {
			voiceChannel.join();
		} catch (MissingPermissionsException e1) {
			return "Jukebox error : cannot join the jukebox channel (" + e1.getMessage() + ").";
		}
		
		for (AudioPlayer audioPlayer : audioPlayers) {
			if (audioPlayer.getGuild().equals(guild))
				player = audioPlayer; 
		}

		if (player == null) {
			player = new AudioPlayer(guild.getAudioManager());
			audioPlayers.add(player);
		}
		
		for (File f : getMusicList()) {
			try {
				player.queue(f);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (UnsupportedAudioFileException e) {
				e.printStackTrace();
			}
		}
		
		player.setLoop(true);
		player.clean();
		player.setPaused(false);
		result += "Jukebox enabled.\n";
//		result += "Now playing : " + player.getCurrentTrack().getMetadata;
		
		return result;
	}

	private File getSong(int songNumber) {
		File[] songs = getMusicList();
		
		if (songs.length == 0)
			return null;
		else
			return songs[songNumber%songs.length];
	}

	private File[] getMusicList() {
		ArrayList<File> tmp = new ArrayList<>();
		File[] result;
		File directory = new File(Settings.crtInstance.getValue("musicpath"));
		String[] fNameSplit;
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
	
//	public static void main(String[] args) {
//		try {
//			Settings.init();
//		} catch (FailedToLoadSettingsException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		JukeBoxCmd jb = new JukeBoxCmd();
//		
//		System.out.println(jb.getMusicList());
//	}

}
