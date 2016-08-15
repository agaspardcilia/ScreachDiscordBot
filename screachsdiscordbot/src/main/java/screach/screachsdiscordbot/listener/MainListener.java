package screach.screachsdiscordbot.listener;

import java.util.ArrayList;

import screach.screachsdiscordbot.handlers.MessageHandler;
import screach.screachsdiscordbot.handlers.PresenceUpdateHandler;
import screach.screachsdiscordbot.handlers.ReadyHandler;
import screach.screachsdiscordbot.util.Debug;
import sx.blah.discord.api.events.Event;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.PresenceUpdateEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class MainListener implements IListener<Event> {
	private final static char cmdSymbole = '!';

	private ArrayList<MessageHandler> msgHandlers;
	private ArrayList<PresenceUpdateHandler> precenseHandlers;
	private ReadyHandler readyHandler;
	
	public MainListener() {
		msgHandlers = new ArrayList<>();
		precenseHandlers = new ArrayList<>();
		readyHandler = new ReadyHandler(this);
	}


	public void addMessageHandler(MessageHandler mh) {
		msgHandlers.add(mh);
	}
	
	public void addPresenceUpdateHandler(PresenceUpdateHandler h) {
		precenseHandlers.add(h);
	}

	public void handle(Event event) {
		try {
			if (event instanceof MessageReceivedEvent)
				handleMessage((MessageReceivedEvent) event);
			else if (event instanceof ReadyEvent)
				readyHandler.setup((ReadyEvent) event);
			else if (event instanceof PresenceUpdateEvent)
				handlePresence((PresenceUpdateEvent) event);
//			else
//				Debug.println("Unhandled event :" + event.toString());
				
		} catch (RateLimitException e) {
			e.printStackTrace();
		} catch (MissingPermissionsException e) {
			e.printStackTrace();
		} catch (DiscordException e) {
			e.printStackTrace();
		}
	}

	private void handleMessage(MessageReceivedEvent event) throws RateLimitException, MissingPermissionsException, DiscordException {
		IMessage message;
		String content;
		String[] args;

		message = event.getMessage();

		content = message.getContent();

		if (isCommand(content)) {
			args = content.split(" ");


			if (args.length > 0) {
				String command = args[0];


				for (MessageHandler mh : msgHandlers) {
					if (command.toUpperCase().equals("!" + mh.getCommand().toUpperCase())) {
						mh.handleMessage(event, args);
						return;
					}

				}

				answerMessageError(event, "Unknown command : \"" + command + "\"");

			} else { //invalid command line
				answerMessageError(event, "Invalid command line");
			}

		}

	}

	private void handlePresence(PresenceUpdateEvent event) {
		for (PresenceUpdateHandler presenceChangeHandler : precenseHandlers) {
			presenceChangeHandler.handle(event);
		}
	}
	
	public static boolean isCommand(String text) {
		return text.charAt(0) == cmdSymbole;
	}

	public static void answerMessageError(MessageReceivedEvent event, String notice) throws RateLimitException, MissingPermissionsException, DiscordException {
		event.getMessage().getChannel().sendMessage(notice);
	}

	public ArrayList<MessageHandler> getMsgHandlers() {
		return msgHandlers;
	}
	
}
