package uk.co.nickthecoder.jguifier.test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.*;

import uk.co.nickthecoder.jguifier.util.FileLister;
import uk.co.nickthecoder.jguifier.util.Exec;

public class SlowFileListerTest {

	public static final File baseDir = new File( "/tmp/SlowFileListerTest" );
	
	static File foo;
	static File bar;
	static File early;
	static File earliest;
	static File late;
	
	static File dir1;
	static File dir2;
	
	static File dir1_a;
	static File dir1_b;

	static File dir1_dira;
	static File dir1_dirb;
	static File dir1_dirc;
	
	@BeforeClass
	public static void setup()
	{
		// Optional. Either we assert our test directory doesn't exist, or we delete it.
		new Exec( "rm", "-r", baseDir.getPath() ).run();
		
		assertEquals( false, baseDir.exists() );
		new Exec( "mkdir", baseDir.getPath() ).run();
		
		earliest = touch( "earliest" );
		early = touch( "early" );
		
		dir1 = mkdir( "dir1" );
		dir2 = mkdir( "dir2" );

		dir1_a = touch( "dir1/a" );

		foo = touch( "foo.txt" );
		bar = touch( "bar.txt" );
		
		dir1_b = touch( "dir1/b" );
		
		dir1_dirb = mkdir( "dir1/b" );
		dir1_dira = mkdir( "dir1/a" );
		dir1_dirc = mkdir( "dir1/c" );
		
		late = touch( "late" );
	}
	

	@AfterClass
	public static void teardown()
	{
		//new Exec( "rm", "-r", baseDir.getPath() ).run();	
		//assertEquals( false, baseDir.exists() );
	}
	
	private static File touch( String name )
	{
		name = baseDir + "/" + name;
		
		new Exec( "touch", name ).run();
		// Ensure that each file has a different time stamp by waiting 1 second
		// which is the resolution of the timestampts on most linux systems.
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		return new File( name );
	}
	
	private static File mkdir( String name )
	{
		name = baseDir + "/" + name;
		new Exec( "mkdir", name ).run();
		return new File( name );
	}
	

	private void assertSameFiles( List<File> results, File... expected )
	{
		assertEquals( expected.length, results.size() );
		for ( int i = 0; i < expected.length; i ++ ) {
			assertEquals( expected[i], results.get(i) );
		}
	}

	@Test
	public void sortByDate()
		throws IOException
	{
		List<File> list = new FileLister().order( FileLister.DATE_ORDER ).listFiles( baseDir );
		assertSameFiles( list, earliest, early, foo, bar, late );
	}
	
	@Test
	public void sortBySize()
		throws IOException
	{
	}
	
	//MORE Sort by name,date and size globally rather than once per directory.

	//MORE test reverse orders - including by name, which needs coding???

	//Directories first using default order, date order
	//   globally and per directory
	//   before reverse is called and after it.
}
