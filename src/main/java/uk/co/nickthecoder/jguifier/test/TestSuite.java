package uk.co.nickthecoder.jguifier.test;

import org.junit.runners.Suite;
import org.junit.runner.RunWith;

@RunWith( Suite.class )
@Suite.SuiteClasses( { ExecLinuxTest.class, FileListerTest.class, SlowFileListerTest.class } )
public class TestSuite
{
  //nothing
}
