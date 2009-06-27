package com.codahale.shore.util;

import java.io.IOException;
import java.io.Writer;

import org.mortbay.jetty.NCSARequestLog;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;

import com.codahale.jdbc.Stopwatch;

public class PerformanceRequestLog extends NCSARequestLog {
	public PerformanceRequestLog() {
		super();
		setLogLatency(true);
	}
	
	@Override
	protected void logExtended(Request request, Response response, Writer writer)
			throws IOException {
		
		super.logExtended(request, response, writer);
		
		writer.write(" ");
		if (Stopwatch.getInstance().wasCalled()) {
			writer.write(Long.toString(Stopwatch.getInstance().getElapsedTime()));
		} else {
			writer.write("-");
		}
		
		Stopwatch.getInstance().reset();
	}
}
