package uk.co.nickthecoder.jguifier.util;

import java.io.File;
import java.util.Comparator;

public class DirectoriesFirstComparator implements Comparator<File>
{
	private Comparator<File> _comparator;
	
	public DirectoriesFirstComparator( Comparator<File> comparator )
	{
		_comparator = comparator;
	}
	
	@Override
	public int compare( File a, File b ) {
		if ( a.isFile() ) {
			if ( b.isDirectory() ) {
				return -1;
			}
		} else {
			if ( b.isFile() ) {
				return 1;
			}
		}
		
		return _comparator.compare( a, b );
	}

}
