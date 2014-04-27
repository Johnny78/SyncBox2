package junit;

import org.junit.Test;

import client.view.PassPrompt;
import client.view.SetupScreen;

public class guiTests {

	@Test
	public void test() {
		PassPrompt pp = new PassPrompt();
		char[] s = pp.getUserPassword();

	}

}
