package uk.co.nickthecoder.jguifier.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.co.nickthecoder.jguifier.util.Exec;
import uk.co.nickthecoder.jguifier.util.ExecException;
import uk.co.nickthecoder.jguifier.util.Pipe;

public class ExecLinuxTest
{
	static final String baseDir = "/tmp/ExecTest";
	static final String resultsDir = baseDir + "/results";
	static final String tmpResults = resultsDir + "/tmp.txt";
	
	static final String lsResults = "bar\nfoo\nresults\n";
	
	
	@BeforeClass
	public static void setup()
	{
		new Exec( "rm", "-r", baseDir ).run();
		new Exec( "mkdir", baseDir ).run();
		new Exec( "touch", baseDir + "/foo" ).run();
		new Exec( "touch", baseDir + "/bar" ).run();
		new Exec( "mkdir", resultsDir ).run();
	}
	
	@AfterClass
	public static void teardown()
	{
		// new Exec( "rm", "-r", baseDir ).run();
	}
	
	@Test
    public void simpleExec()
    {
    	String ls = new Exec( "ls", baseDir ).stdout().run().getStdout().toString();
    	assertEquals( lsResults, ls );
    }
    
	@Test
	public void input()
		throws IOException
	{
		String head = new Exec( "head", "-n", "1" ).stdin( "Foo\nBar\n" ).stdout().run().getStdout().toString();
		assertEquals( "Foo\n", head );
		
		new Exec( "cat" ).stdin( "Hello World" ).stdout( new File( tmpResults ) ).run();
		String hello = new Exec( "cat", tmpResults ).stdout().run().getStdout().toString();
		assertEquals( "Hello World", hello );
		
	}
	
    @Test
    public void redirect()
    	throws IOException
	{
    	new Exec( "ls", baseDir ).stdout( new File(tmpResults) ).run();
    	String ls2 = new Exec( "cat", tmpResults ).stdout().run().getStdout().toString();
    	assertEquals( lsResults, ls2 );
    }
    
    @Test
    public void pipe()
    {
    	Exec ls = new Exec( "ls", baseDir );
    	Exec head = new Exec( "head", "-n", "1" ).stdout();
    	
    	ls.stdout( new Pipe( head ) ).run();
    	
    	assertEquals( "bar\n", head.getStdout() );
    }
    
    @Test
    public void buildArgs()
    {
    	Exec exec = new Exec( "echo", "Hello" ).add( "World" ).stdout().run();
    	assertEquals( "Hello World\n", exec.getStdout() );
    }
    
	@Test(expected=ExecException.class)
	public void nullArgument()
	{
    	Exec exec2 = new Exec( "echo" ).add( "Hello" ).add( null ).add( "World" ).stdout().run();
    	assertEquals( "Hello World\n", exec2.getStdout() );
    }
	
	@Test
	public void ignoreNullArgument()
	{
		Exec exec2 = new Exec( "echo" ).add( "Hello" ).add( null ).add( "World" ).removeNullArgs().stdout().run();
    	assertEquals( "Hello World\n", exec2.getStdout() );
    }
	
	@Test
	public void setDir()
	{
		String tmp = new Exec( "pwd" ).dir( new File( "/tmp" ) ).stdout().run().getStdout().toString();
		assertEquals( "/tmp\n", tmp );
		
		String root = new Exec( "pwd" ).dir( new File( baseDir ) ).stdout().run().getStdout().toString();
		assertEquals( baseDir + "\n", root );
		
	}
	
	@Test
	public void env()
	{
		String hello = new Exec( "bash", "-c", "echo ${FOO}" ).var( "FOO", "Hello" ).stdout().run().getStdoutLine();
		assertEquals( hello, "Hello" );
		
		String world = new Exec( "bash", "-c", "export" ).clearEnv().var( "FOO", "World" ).stdout().run().getStdoutLine();
		String expected =  "declare -x FOO=\"World\"";
		assertEquals( expected, world );
		assertTrue( expected.length() == world.length() );
		
		String allVars = new Exec( "bash", "-c", "export" ).var( "FOO", "World" ).stdout().run().getStdoutLine();
		assertTrue( expected.length() != allVars.length() );
	}
	
	@Test
	public void stderr()
	{
		String dir =  baseDir + "/XXX";
		String noSuch = new Exec( "ls", dir ).stderr().run().getStderrLine();
		assertEquals( "ls: cannot access " + dir + ": No such file or directory", noSuch );
	}
	
	@Test
	public void exitStatus()
	{
		int exit1 = new Exec( "ls", baseDir ).run().getExitStatus();
		assertEquals( 0, exit1 );
		
		int exit2 = new Exec( "ls", baseDir+ "/XXX" ).run().getExitStatus();
		assertEquals( 2, exit2 );
	}
	
	@Test
	public void stdoutArray()
	{
		String[] ls = new Exec( "ls", baseDir ).stdout().run().getStdoutAsArray();
		assertArrayEquals( new String[] { "bar", "foo", "results" }, ls );
	}
	
	@Test 
	public void throwNoError()
	{
		int exit1 = new Exec( "ls", baseDir ).throwOnError().run().getExitStatus();
		assertEquals( 0, exit1 );
	}
	
	@Test(expected=ExecException.class)
	public void throwOnError()
	{		
		int exit2 = new Exec( "ls", baseDir+ "/XXX" ).throwOnError().run().getExitStatus();
		assertEquals( 2, exit2 );
	}

	@Test
	public void timeout()
	{
		Exec.State state1 = new Exec( "sleep", "1" ).timeout( 500 ).run().getState();
		assertEquals( Exec.State.TIMED_OUT, state1 );

		Exec.State state2 = new Exec( "sleep", "1" ).timeout( 1500 ).run().getState();
		assertEquals( Exec.State.COMPLETED, state2 );

	}

}
