package screach.screachsdiscordbot.console.cmd;

import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import screach.screachsdiscordbot.console.ConsoleCommand;

public class CodecListCmd implements ConsoleCommand {

	@Override
	public String getName() {
		return "Codec List";
	}

	@Override
	public String getDescription() {
		return "Displays a list of available codecs.";
	}

	@Override
	public String getUsage() {
		return "codec";
	}

	@Override
	public String getCommand() {
		return "codec";
	}

	@Override
	public void exec(String[] args) {
		String result = "Available codecs";
		Encoder e = new Encoder();
		int i = 0;
		try {
			
			for (String s : e.getSupportedDecodingFormats()) {
				if (i % 5 == 0)
					result += s + "\n";
				else
					result += s + ", ";
				
				i++;
			}
			
		} catch (EncoderException e1) {
			e1.printStackTrace();
		}
		
		System.out.println(result);
	}

}
