package junit;

import static org.junit.Assert.*;

import org.junit.Test;

import client.ClientControl;

/**
 * testing full synchronisation functionality
 * to see in action add or remove files from the client syncbox
 * and launch this test.
 * @author John
 *
 */
public class SyncronisationTests {

	@Test
	public void testSynchronise() throws Exception {
		ClientControl cc = new ClientControl();
		cc.synchronise("password".toCharArray());
	}
}
