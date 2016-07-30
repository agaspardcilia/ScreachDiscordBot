package screach.screachsdiscordbot.handlers.jukebox;

public class JukeBoxFile {
	private String path;
	private int nb;
	
	public JukeBoxFile(String path, int nb) {
		this.path = path;
		this.nb = nb;
	}
	
	public String getPath() {
		return path;
	}
	
	public int getNb() {
		return nb;
	}
}
