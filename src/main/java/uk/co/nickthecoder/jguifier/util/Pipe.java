package uk.co.nickthecoder.jguifier.util;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Acts as both a Source and a Sink, allowing the output from one command to be piped
 * into the input to another.
 * To use a Pipe, create the two commands, and then : <code><pre>
 *     command1.stdout( new Pipe( command2 ) ).run();
 * </pre></code> This will run command1, and pipe its output to command2. There is no need to call run on
 * command2, this is done automatically by the Pipe.
 * 
 * Note, it is possible to pipe stdout to one command, and stderr to another. i.e. three
 * processes in total.
 * 
 * @priority 4
 */
public class Pipe extends CopySink
{
    private Exec _target;

    public Pipe(Exec target)
    {
        _target = target;
    }

    @Override
    public void setStream(InputStream is)
    {
        super.setStream(is);
        _target.stdin(new Source()
        {

            @Override
            public void setStream(OutputStream os)
            {
                Pipe.this.setStream(os);
            }

            @Override
            public void run()
            {
                // Get CopySink to copy the data from one process to another
                Pipe.super.run();
                try {
                    _out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
    }

    @Override
    public void run()
    {
        // Run the command that is being pipe TO, it will be up to it's stdin Source
        // to copy the data (which is in the annon inner class above).
        _target.run();
    }

}
