package screach.screachsdiscordbot.util;

public class FailedToLoadSettingsException extends Exception {
	private static final long serialVersionUID = 3549320871825799144L;
	
	private String notice;
	
	public FailedToLoadSettingsException(String notice) {
		this.notice = notice;
	}
	
	public String getNotice() {
		return notice;
	}
	
	
}
