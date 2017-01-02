package uk.co.nickthecoder.jguifier.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith( Suite.class )
@Suite.SuiteClasses( { ExecLinuxTest.class, FileListerTest.class, SlowFileListerTest.class } )
public class TestSuite
{
  //nothing
}
