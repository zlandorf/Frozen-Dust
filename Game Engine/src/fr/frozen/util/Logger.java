package fr.frozen.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Logger extends ILogger {
	
	protected static String ERROR_STR = "[ERROR] ";
	protected static String INFO_STR = "[INFO] ";
	
	protected boolean _logging = false;
	protected PrintStream _logFile = null;

	@Override
	public void log(String str) {
		if (!_logging) return;
		
		if (_logFile != null) {
			_logFile.println(str);
			_logFile.flush();
		}
		System.out.println(str);
	}
	
	@Override
	public void logError(String str) {
		log(ERROR_STR+str);
	}

	@Override
	public void logInfo(String str) {
		log(INFO_STR+str);
	}

	@Override
	public void setLogging(boolean b) {
		_logging = b;
	}

	@Override
	public void setLoggingFile(String fileName) throws IOException {
		setLoggingFile(new File(fileName));
	}

	@Override
	public void setLoggingFile(File f) throws IOException {
		if  (_logFile != null) {
			_logFile.close();
			_logFile = null;
		}
		
		if (f == null) 
			_logFile = null;
		else 
			_logFile = new PrintStream(new BufferedOutputStream(new FileOutputStream(f)));
	}
}
