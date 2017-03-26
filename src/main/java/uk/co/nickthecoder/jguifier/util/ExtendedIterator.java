package uk.co.nickthecoder.jguifier.util;

import java.util.Iterator;

/**
 * Gets around the pesky problem that Iterable<Foo> is not a compatible with Iterable<Bar> even when
 * Foo extends Bar.

 * @param <T> The base class you wish to appear to iterate over
 * @param <U> The extended class you will iterate over
 */
public class ExtendedIterator<T, U extends T> implements Iterator<T>
{
    private Iterator<U> i;

    public ExtendedIterator(Iterable<U> iterable)
    {
        i = iterable.iterator();
    }

    @Override
    public boolean hasNext()
    {
        return i.hasNext();
    }

    @Override
    public T next()
    {
        return i.next();
    }

    @Override
    public void remove()
    {
        i.remove();
    }

    public static <TT, UU extends TT> Iterable<TT> extendedIterable( final Iterable<UU> iterable )
    {
        return new Iterable<TT>() {

            @Override
            public Iterator<TT> iterator()
            {
                return new ExtendedIterator<TT,UU>( iterable );
            }
        };
    }
}
