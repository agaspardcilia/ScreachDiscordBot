package screach.screachsdiscordbot.handlers.cmd.jukebox;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.io.FilenameUtils;

import com.github.axet.vget.VGet;
import com.github.axet.vget.info.VideoFileInfo;
import com.github.axet.vget.vhs.YouTubeInfo;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;
import it.sauronsoftware.jave.InputFormatException;
import screach.screachsdiscordbot.util.Debug;
import screach.screachsdiscordbot.util.Settings;
import sx.blah.discord.util.audio.AudioPlayer;

public class YoutubeAudioTrack implements Runnable {
	private final static String TMP_PATH = "tmpmusicpath";
	private final static String MUSIC_PATH = "musicpath";
	private final static String YOUTUBE_REGEX = "https?:\\/\\/(?:[0-9A-Z-]+\\.)?(?:youtu\\.be\\/|youtube\\.com\\S*[^\\w\\-\\s])([\\w\\-]{11})(?=[^\\w\\-]|$)(?![?=&+%\\w]*(?:['\"][^<>]*>|<\\/a>))[?=&+%\\w]*";
	
	private URL url;
	private AudioPlayer player;
	
	public YoutubeAudioTrack(URL url, AudioPlayer player) throws InvalidYoutubeURL {
		if (!isYoutubeURLValid(url.toString()))
			throw new InvalidYoutubeURL();
			
		this.url = url;
		this.player = player;
	}



	@Override
	public void run() {
		File tmpDir = new File(Settings.crtInstance.getValue(TMP_PATH));
		File musicDir = new File(Settings.crtInstance.getValue(MUSIC_PATH));
		
		if (!tmpDir.exists() || !tmpDir.isDirectory()) {
			if (!tmpDir.mkdir()) {
				Debug.println("Error : Failed to create directory " + tmpDir);
				return;
			}
		}
		
		File audioFile;
		File videoFile = null;
		YouTubeInfo yi = new YouTubeInfo(url);
		VGet v = new VGet(yi, tmpDir);
		List<VideoFileInfo> info;
		
		
		Debug.println("Downloading " + url + "...");
		v.download();
		Debug.println("Downloading finished.");
		
		info = yi.getInfo();
		for (VideoFileInfo videoFileInfo : info) {
			if (videoFileInfo.targetFile != null) {
				videoFile = videoFileInfo.targetFile;
				break;
			}
		}
		
		audioFile = new File(Settings.crtInstance.getValue(MUSIC_PATH) + "/" + FilenameUtils.getBaseName(videoFile.getName()) + ".mp3");
		
		
		try {
			convertToAudio(videoFile, audioFile);
			player.queue(audioFile);
			videoFile.delete();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InputFormatException e) {
			e.printStackTrace();
		} catch (EncoderException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
		
	}

	public static  void convertToAudio(File video, File audio) throws IllegalArgumentException, InputFormatException, EncoderException {
		AudioAttributes audioAttr = new AudioAttributes();
		EncodingAttributes encodingAttr = new EncodingAttributes();
		Encoder encoder = new Encoder();

		audioAttr.setCodec("libmp3lame");
		audioAttr.setBitRate(new Integer(128000));
		audioAttr.setChannels(new Integer(2));
		audioAttr.setSamplingRate(new Integer(44100));

		encodingAttr.setFormat("mp3");
		encodingAttr.setAudioAttributes(audioAttr);

		encoder.encode(video, audio, encodingAttr);
	}

	public static boolean isYoutubeURLValid(String url) {
		Pattern compiledPattern = Pattern.compile(YOUTUBE_REGEX, Pattern.CASE_INSENSITIVE);
		Matcher matcher = compiledPattern.matcher(url);
		
		return matcher.matches();
	}
	
}
