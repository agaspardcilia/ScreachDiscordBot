package screach.screachsdiscordbot.util;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ThreadPool {
	private static ThreadPool crtInst;
	
	private Executor executor;
	
	public static void init() {
		crtInst = new ThreadPool();
	}
	
	public static ThreadPool getCrtInst() {
		return crtInst;
	}
	
	public ThreadPool() {
		executor = Executors.newSingleThreadExecutor();
	}
	
	public Executor getExecutor() {
		return executor;
	}
}
