package junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


/**
 * Run all junits tests
 * The Server must be up for them to succeed
 * @author John
 *
 */
@RunWith(Suite.class)
@SuiteClasses({ SyncronisationTests.class, ClientControlTest.class, MetadataToolTest.class, PBETest.class })
public class AllTests {

}
