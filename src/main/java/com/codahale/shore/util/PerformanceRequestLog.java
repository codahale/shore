package com.codahale.shore.util;

import java.io.IOException;
import java.io.Writer;

import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

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
