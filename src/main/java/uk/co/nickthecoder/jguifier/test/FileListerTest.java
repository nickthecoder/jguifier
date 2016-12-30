package uk.co.nickthecoder.jguifier.test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.*;

import uk.co.nickthecoder.jguifier.util.FileLister;
import uk.co.nickthecoder.jguifier.util.Exec;

public class FileListerTest {

	public static final File baseDir = new File( "/tmp/FileList" );
	
	static File foo;
	static File bar;
	static File early;
	static File earliest;
	static File late;
	
	static File hidden;
	static File hiddenDir;
	static File hiddenDir_a;
	
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
		new Exec( "rm", "-r", baseDir.getPath() ).run();	

		assertEquals( false, baseDir.exists() );
		new Exec( "mkdir", baseDir.getPath() ).run();
		
		earliest = touch( "earliest.png" );
		early = touch( "early.jpg" );
		foo = touch( "foo.txt" );
		bar = touch( "bar.txt" );
		late = touch( "late.jpg" );
		hidden = touch( ".hidden" );
		
		dir1 = mkdir( "dir1" );
		dir2 = mkdir( "dir2" );
		hiddenDir = mkdir( ".hiddenDir" );

		hiddenDir_a = touch( ".hiddenDir/a" );
		dir1_a = touch( "dir1/a" );
		dir1_b = touch( "dir1/b" );
		
		dir1_dirb = mkdir( "dir1/dirb" );
		dir1_dira = mkdir( "dir1/dira" );
		dir1_dirc = mkdir( "dir1/dirc" );
		
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
	public void simple()
		throws IOException
	{
		List<File> list = new FileLister().listFiles( baseDir );
		
		assertSameFiles( list, bar, earliest, early, foo, late );
		
	}
	
	@Test
	public void includeBase()
		throws IOException
	{
		List<File> list = new FileLister().includeBase().listFiles( baseDir );
		
		assertSameFiles( list, baseDir, bar, earliest, early, foo, late );
		
	}
	
	@Test
	public void includeDirectories()
		throws IOException
	{
		List<File> list = new FileLister().includeDirectories().listFiles( baseDir );
		
		assertSameFiles( list, bar, dir1, dir2, earliest, early, foo, late );		
	}

	@Test
	public void onlyDirectories()
		throws IOException
	{
		List<File> list1 = new FileLister().onlyDirectories().listFiles( baseDir );
		
		assertSameFiles( list1, dir1, dir2 );		

		List<File> list2 = new FileLister().includeDirectories().excludeFiles().listFiles( baseDir );
		
		assertSameFiles( list2, dir1, dir2 );		
	}
	
	@Test
	public void includeHidden()
		throws IOException
	{
		List<File> list1 = new FileLister().includeHidden().listFiles( baseDir );
		
		assertSameFiles( list1, hidden, bar, earliest, early, foo, late );
		
		List<File> list2 = new FileLister().includeHidden().includeDirectories().listFiles( baseDir );
		
		assertSameFiles( list2, hidden, hiddenDir, bar, dir1, dir2, earliest, early, foo, late );
		
	}
	
	
	@Test
	public void depth0()
		throws IOException
	{
		List<File> list = new FileLister().depth( 0 ).listFiles( baseDir );
		
		assertSameFiles( list );		
	}
		
	@Test
	public void depth2()
		throws IOException
	{
		List<File> list = new FileLister().depth( 2 ).listFiles( baseDir );
		
		assertSameFiles( list, bar, earliest, early, foo, late, dir1_a, dir1_b );
	}

	@Test
	public void enterHidden()
		throws IOException
	{
		List<File> list1 = new FileLister().depth( 2 ).enterHidden().listFiles( baseDir );
		assertSameFiles( list1, bar, earliest, early, foo, late, hiddenDir_a, dir1_a, dir1_b );

		List<File> list2 = new FileLister().depth( 2 ).enterHidden().includeHidden().includeDirectories().listFiles( baseDir );
		assertSameFiles( list2, hidden, hiddenDir, hiddenDir_a, bar, dir1, dir1_a, dir1_b, dir1_dira, dir1_dirb, dir1_dirc, dir2, earliest, early, foo, late );

		List<File> list3 = new FileLister().depth( 2 ).enterHidden().includeDirectories().listFiles( baseDir );
		assertSameFiles( list3, hiddenDir, hiddenDir_a, bar, dir1, dir1_a, dir1_b, dir1_dira, dir1_dirb, dir1_dirc, dir2, earliest, early, foo, late );
	}
	
	@Test
	public void absolute()
		throws IOException
	{
		List<File> relatives = new FileLister().listFiles( new File( "." ) );
		List<File> absolutes = new FileLister().absolute().listFiles( new File( "." ) );
		
		int i = 0;
		for ( File relative : relatives ) {
			File absolute = absolutes.get(i);
			i ++;
			
			assertFalse( relative.equals( absolute ) );
			assertFalse( relative.isAbsolute() );
			assertTrue( absolute.isAbsolute() );
			assert relative.getAbsoluteFile().equals( absolute );
		}
	}

	@Test
	public void extensions()
		throws IOException
	{
		List<File> txt = new FileLister().extension( "txt" ).listFiles( baseDir );
		assertSameFiles( txt, bar, foo );
		
		List<File> images = new FileLister().extensions( "jpg", "png" ).listFiles( baseDir );
		assertSameFiles( images, earliest, early, late );
	}
	
	@Test
	public void canonical()
		throws IOException
	{
		List<File> relatives = new FileLister().listFiles( new File( "." ) );
		List<File> canonicals = new FileLister().canonical().listFiles( new File( "." ) );
		
		int i = 0;
		for ( File relative : relatives ) {
			File canonical = canonicals.get(i);
			i ++;

			assertFalse( relative.equals( canonical ) );
			assertFalse( relative.isAbsolute() );
			assertTrue( canonical.isAbsolute() );
			assert relative.getCanonicalFile().equals( canonical );
		}
	}

	@Test
	public void customFilter()
		throws IOException
	{
		// Contains an "e" after the first character
		FileFilter filter = new FileFilter() {

			@Override
			public boolean accept(File file) {
				return file.getName().indexOf( 'e' ) > 0;
			}
			
		};
		List<File> list = new FileLister().depth( 2 ).filter( filter ).listFiles( baseDir );
		assertSameFiles( list, late );

	}
	
}
