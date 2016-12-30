package uk.co.nickthecoder.jguifier.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class FileLister
	implements FileFilter
{
	public static final Comparator<File> ALPHABETICAL_ORDER = new Comparator<File>() {
		@Override
		public int compare(File a, File b) {
			return a.getPath().compareTo( b.getPath() );
		}
		
	};

	public static final Comparator<File> SIZE_ORDER = new Comparator<File>() {
		@Override
		public int compare(File a, File b) {
			long lena = a.length();
			long lenb = b.length();
			if ( lena == lenb ) {
				return 0;
			}
			if ( lena > lenb ) {
				return 1;
			} else {
				return -1;
			}
		}
		
	};
	
	public static final Comparator<File> DATE_ORDER = new Comparator<File>() {
		@Override
		public int compare(File a, File b) {
			long al = a.lastModified();
			long bl = b.lastModified();
			System.out.print( a + ":" + al + " vs " + b + ":" + bl );
			if ( al == bl ) {
				System.out.println( " same" );
				return 0;
			}
			if ( al > bl ) {
				System.out.println( " A" );
				return 1;
			} else {
				System.out.println( " B" );
				return -1;
			}
		}
		
	};
	
	
	public enum Sort { UNSORTED, DIRECTORY, ALL };

	/**
	 * The depth of recursion through the filesystem. 1 lists a single directory,
	 * 0 lists nothing.
	 */
	private int _depth = 1;
	
	/**
	 * If true, then the base directory to the listFile method is included in the results.
	 * Default is false.
	 */
	private boolean _includeBase = false;
	
	/**
	 * Sort a each directory individually, all together or not at all.
	 * The default is to sort each directory individually.
	 */
	private Sort _sort = Sort.DIRECTORY;
		
	private Comparator<File> _order = ALPHABETICAL_ORDER;
	
	private FileFilter _subDirectoryFilter = new SubDirectoryFilter();
	
	private boolean _includeFiles = true;
	
	private boolean _includeDirectories = false;
	
	private boolean _includeHidden = false;

	private boolean _enterHidden = false;
	
	private String[] _fileExtensions;
	
	private FileFilter _customFilter = null;
			
	private boolean _absolute = false;
	
	private boolean _canonical = false;
	
	
	public FileLister()
	{
	}
	
	public void unsorted()
	{
		setSort( Sort.UNSORTED );
	}
	public void setSort( Sort value )
	{
		_sort = value;
	}
	public Sort getSort()
	{
		return _sort;
	}
	
	public FileLister order( Comparator<File> order )
	{
		setOrder( order );
		return this;
	}
	public void setOrder( Comparator<File> order )
	{
		_order = order;
	}
	public Comparator<File> getOrder()
	{
		return _order;
	}
	public FileLister reverse()
	{
		_order = new ReverseComparator<File>( _order );
		return this;
	}
	
	/**
	 * When listing both files and directories, calling this will make the directories come
	 * before the files. When combined with the "reverse" method, the order is important.
	 * If directoriesFirst is called after "reverse", the directories will still appear first.
	 * Whereas is reverse is called after, then the directories will end up at the end.
	 * 
	 * Methods which set the order (such as order, setOrder) will override this call, so
	 * always call directoriesFirst after you have set the general order.
	 * 
	 * @return this
	 */
	public FileLister directoriesFirst()
	{
		_order = new DirectoriesFirstComparator( _order );
		return this;
	}
	
	public FileLister depth( int value )
	{
		setDepth( value );
		return this;
	}
	public void setDepth( int value )
	{
		_depth = value;
	}
	public int getDepth()
	{
		return _depth;
	}

	public FileLister absolute()
	{
		setAbsolute( true );
		return this;
	}
	public void setAbsolute( boolean value )
	{
		_absolute = value;
	}
	public boolean getAbsolute()
	{
		return _absolute;
	}
	
	public FileLister canonical()
	{
		setCanonical( true );
		return this;
	}
	public void setCanonical( boolean value )
	{
		_canonical = value;
	}
	public boolean getCanonical()
	{
		return _canonical;
	}
	
	
	
	public FileLister includeBase()
	{
		setIncludeBase( true );
		return this;
	}
	public void setIncludeBase( boolean value )
	{
		_includeBase = value;
	}
	public boolean getIncludeBase()
	{
		return _includeBase;
	}

	public FileLister excludeFiles()
	{
		setIncludeFiles( false );
		return this;
	}
	public void setIncludeFiles( boolean value )
	{
		_includeFiles = value;
	}
	public boolean getIncludeFiles()
	{
		return _includeFiles;
	}

	public FileLister onlyDirectories()
	{
		setIncludeDirectories( true );
		setIncludeFiles( false );
		return this;
	}
	public FileLister includeDirectories()
	{
		setIncludeDirectories( true );
		return this;
	}
	public void setIncludeDirectories( boolean value )
	{
		_includeDirectories = value;
	}
	public boolean getIncludeDirectories()
	{
		return _includeDirectories;
	}
	
	public FileLister includeHidden()
	{
		setIncludeHidden( true );
		return this;
	}
	public void setIncludeHidden( boolean value )
	{
		_includeHidden= value;
	}
	public boolean getIncludeHidden()
	{
		return _includeHidden;
	}


	public FileLister enterHidden()
	{
		setEnterHidden( true );
		return this;
	}
	public void setEnterHidden( boolean value )
	{
		_enterHidden= value;
	}
	public boolean getEnterHidden()
	{
		return _enterHidden;
	}
	

	/**
	 * A convenience method for beanshell, which doesn't currently handle varargs, so the extensions( String... ) isn't usable.
	 * @param filextension The single extension used to filter the results (files only). 
	 * @return this
	 */
	public FileLister extension( String fileExtension )
	{
		setFileExtensions( new String[] { fileExtension } );
		return this;
	}
	/**
	 * The same as extensions( String... ), but here just for beanshell which doesn't support varargs yet.
	 * @param fileExtensions
	 * @return
	 */
	public FileLister extensionArray( String[] fileExtensions )
	{
		setFileExtensions( fileExtensions );
		return this;
	}
	public FileLister extensions( String... fileExtensions )
	{
		setFileExtensions( fileExtensions );
		return this;
	}
	public void setFileExtensions( String[] fileExtensions )
	{
		_fileExtensions = fileExtensions;		
	}
	public String[] getFileExtensions()
	{
		return _fileExtensions;
	}
	
	public FileLister filter( FileFilter filter )
	{
		setCustomFilter( filter );
		return this;
	}
	public void setCustomFilter( FileFilter filter )
	{
		_customFilter = filter;
	}
	public FileFilter getCustomFilter()
	{
		return _customFilter;
	}
	
	public List<File> listFiles( String directoryPath )
		throws IOException
	{
		return listFiles( new File( directoryPath ) );
	}

	public List<File> listFiles( File directory )
		throws IOException
	{
		List<File> results = new ArrayList<File>();

		if ( _includeBase ) {
			results.add( directory );
		}
		
		if ( _depth > 0 ) {
			listFiles( results, directory, 1 );
		}
		
		if ( _sort == Sort.ALL ) {
			Collections.sort( results, _order );
		}
		return results;
	}
	
	private void listFiles( List<File> results, File directory, int depth )
		throws IOException
	{
		File[] files = directory.listFiles( this );
		if ( files == null ) {
			throw new IOException( "Failed to list directory " + directory );
		}

		if ( _sort == Sort.DIRECTORY ) {
			Arrays.sort( files, _order );
		}
		for( File file : files ) {
			if ( _canonical ) {
				try {
					file = file.getCanonicalFile();
				} catch (Exception e) {
					throw new RuntimeException( e );
				}
			} else if ( _absolute ) {
				file = file.getAbsoluteFile();
			}
			results.add( file );
			
			if ( (depth < _depth) && _includeDirectories && file.isDirectory() ) {
				listFiles( results, file, depth + 1 );
				
			}
		}
		
		if ( depth < _depth && (! _includeDirectories ) ) {
			File[] subDirs = directory.listFiles( _subDirectoryFilter );
			if ( _sort == Sort.DIRECTORY ) {
				Arrays.sort( subDirs, _order );
			}
			for ( File subDir : subDirs ) {
				listFiles( results, subDir, depth + 1 );
			}
		}
	}

	@Override
	public boolean accept( File file )
	{
		boolean isDirectory = file.isDirectory();
		
		if ( ! _includeFiles && ! isDirectory ) {
			return false;
		}
		
		if ( ! _includeDirectories && isDirectory ) {
			return false;
		}
		
		if ( isDirectory ) {
			if ( ! _enterHidden && ! _includeHidden && file.isHidden() ) {
				return false;
			}
			
		} else {
			if ( ! _includeHidden && file.isHidden() ) {
				return false;
			}
		}

		if ( _fileExtensions != null ) {
			int lastDot = file.getName().lastIndexOf( '.' );
			if ( lastDot < 0 ) {
				return false;
			}
			String fe = file.getName().substring( lastDot + 1 );
			for ( String allowed : _fileExtensions ) {
				if ( fe.equals( allowed ) ) {
					return true;
				}
			}
			return false;
		}
		
		if ( _customFilter != null ) {
			return _customFilter.accept( file );
		}
		return true;
	}
	
	class SubDirectoryFilter implements FileFilter
	{
		@Override
		public boolean accept( File file )
		{
			if ( ! file.isDirectory() ) {
				return false;
			}

			if ( ! _enterHidden && ! _includeHidden && file.isHidden() ) {
				return false;
			}

			if ( _customFilter != null ) {
				return _customFilter.accept( file );
			}
			
			return true;
		}
		
	}
	
}

