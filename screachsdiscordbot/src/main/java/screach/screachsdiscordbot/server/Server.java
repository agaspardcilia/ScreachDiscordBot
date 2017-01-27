package screach.screachsdiscordbot.server;

import java.util.ArrayList;

import screach.screachsdiscordbot.handlers.ActiveMessageHandler;
import screach.screachsdiscordbot.handlers.PassiveMessageHandler;
import screach.screachsdiscordbot.handlers.PresenceUpdateHandler;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.PresenceUpdateEvent;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class Server {
	private final static char cmdSymbole = '!';
	
	private IGuild guild;
	private ArrayList<ActiveMessageHandler> activeMessageEvents; //Used when the message is considered as a command.
	private ArrayList<PassiveMessageHandler> passiveMessageEvents;
	private ArrayList<PresenceUpdateHandler> presenceEvents;
	
	
	public Server(IGuild guild) {
		this.guild = guild;
		activeMessageEvents = new ArrayList<>();
		passiveMessageEvents = new ArrayList<>();
		presenceEvents = new ArrayList<>();
	}
	

	
	public void handleMessageEvent(MessageReceivedEvent event) throws RateLimitException, MissingPermissionsException, DiscordException {
		IMessage message;
		String content;
		String[] args;

		message = event.getMessage();
		content = message.getContent();

		if (isCommand(content)) {
			args = content.split(" ");


			if (args.length > 0) {
				String command = args[0];


				for (ActiveMessageHandler mh : activeMessageEvents) {
					if (command.toUpperCase().equals("!" + mh.getCommand().toUpperCase())) {
						mh.handleMessage(event, args);
						return;
					}

				}

				answerMessageError(event, "Unknown command : \"" + command + "\"");

			} else { //invalid command line
				answerMessageError(event, "Invalid command line");
			}

		} else {
			for (PassiveMessageHandler handler : passiveMessageEvents) {
				handler.handleMessage(event);
			}
		}

	}
	
	public void handlePresence(PresenceUpdateEvent event) {
		for (PresenceUpdateHandler handler : presenceEvents) {
			handler.handle(event);
		}
	}
	

	public static boolean isCommand(String text) {
		return text.charAt(0) == cmdSymbole;
	}
	
	public static void answerMessageError(MessageReceivedEvent event, String notice) throws RateLimitException, MissingPermissionsException, DiscordException {
		event.getMessage().getChannel().sendMessage(notice);
	}
	
	public void addActiveMessageHandler(ActiveMessageHandler toAdd) {
		activeMessageEvents.add(toAdd);
	}
	
	public void addPassiveMessageHandler(PassiveMessageHandler toAdd) {
		passiveMessageEvents.add(toAdd);
	}
	
	public void addPressenceUpdateEventHandler(PresenceUpdateHandler toAdd) {
		presenceEvents.add(toAdd);
	}
	
	public void removeActiveMessageHandler(ActiveMessageHandler toRemove) {
		activeMessageEvents.remove(toRemove);
	}
	
	public void removePassiveMessageHandler(PassiveMessageHandler toRemove) {
		passiveMessageEvents.remove(toRemove);
	}
	
	public void removePressenceUpdateEventHandler(PresenceUpdateHandler toRemove) {
		presenceEvents.remove(toRemove);
	}
	
	public IGuild getGuild() {
		return guild;
	}
} 
