package screach.screachsdiscordbot.util;

public class Debug {
	private final static String DEBUG = "debug";
	
	public static boolean debug = false;
	
	public static void init() {
		debug = Boolean.parseBoolean(Settings.crtInstance.getValue(DEBUG));
		
		if (debug)
			System.out.println("Debug mode enabled.");
	}
	
	public static String getDebug() {
		return DEBUG;
	}
	
	public static void print(String s) {
		if (debug)
			System.out.print("DEBUG : " + s);
	}
	
	public static void println(String s) {
		print(s + "\n");
	}
	
	
}
