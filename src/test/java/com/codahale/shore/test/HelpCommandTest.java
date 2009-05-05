package com.codahale.shore.test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.codahale.shore.HelpCommand;

@RunWith(Enclosed.class)
public class HelpCommandTest {
	public static class A_Help_Command {
		private HelpCommand cmd;
		private ByteArrayOutputStream output;
		
		@Before
		public void setup() throws Exception {
			this.output = new ByteArrayOutputStream();
			this.cmd = new HelpCommand("Usage: woo", output);
		}
		
		@Test
		public void itHasText() throws Exception {
			assertThat(cmd.getText(), is("Usage: woo"));
		}
		
		@Test
		public void itHasAnOutputStream() throws Exception {
			assertThat((ByteArrayOutputStream) cmd.getOutputStream(), is(sameInstance(output)));
		}
		
		@Test
		public void itPrintsTheTextToTheOutputStream() throws Exception {
			cmd.run();
			
			assertThat(output.toString(), is(
				"Usage: woo" +
				"\n"
			));
		}
	}
}
