package com.codahale.shore.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;

import org.apache.commons.cli.Options;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.codahale.shore.HelpCommand;

@RunWith(Enclosed.class)
public class HelpCommandTest {
	public static class A_Help_Command {
		private HelpCommand cmd;
		private Options options;
		private ByteArrayOutputStream output;
		
		@Before
		public void setup() throws Exception {
			this.output = new ByteArrayOutputStream();
			this.options = new Options().addOption("h", false, "Show this help message");
			
			this.cmd = new HelpCommand("woo", options, output);
		}
		
		@Test
		public void itHasAName() throws Exception {
			assertThat(cmd.getName(), is("woo"));
		}
		
		@Test
		public void itHasASetOfOptions() throws Exception {
			assertThat(cmd.getOptions(), is(sameInstance(options)));
		}
		
		@Test
		public void itHasAnOutputStream() throws Exception {
			assertThat((ByteArrayOutputStream) cmd.getOutputStream(), is(sameInstance(output)));
		}
		
		@Test
		public void itPrintsAHelpMessageForTheOptionsToTheOutputStream() throws Exception {
			cmd.run();
			
			assertThat(output.toString(), is(
				"usage: woo [-h]\n" +
				" -h   Show this help message\n"
			));
		}
	}
}
