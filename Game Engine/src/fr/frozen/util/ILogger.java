package fr.frozen.util;

import java.io.File;
import java.io.IOException;

public abstract class ILogger {
	
	private static ILogger instance = null;
	
	public static ILogger getInstance() {
		if (instance == null) {
			instance = new Logger();
		}
		return instance;
	}
	
	public abstract void setLogging(boolean b);
	
	public abstract void setLoggingFile(File f) throws IOException;
	
	public abstract void setLoggingFile(String fileName) throws IOException;
	
	public abstract void log(String str);
	
	public abstract void logError(String str);
	
	public abstract void logInfo(String str);
	//maybe add other default loggings
}
