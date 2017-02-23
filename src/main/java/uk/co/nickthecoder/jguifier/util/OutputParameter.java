package uk.co.nickthecoder.jguifier.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import uk.co.nickthecoder.jguifier.ExtraSpecialParameter;
import uk.co.nickthecoder.jguifier.FileParameter;

/**
 * A customised parameter, suitable as an "output" parameter, with a special value for System.out (with key
 * &quot;-&quot;), and a regular FileParameter otherwise.
 * <p>
 * Unlike other Parameters, this does not have a Builder, instead, use {@link #create(String)}.
 * </p>
 * If you want stdout to be the default value, then : <code>output.setValue( System.out )</code>
 * 
 * @param name
 *            The name of the parameter
 * @return The ExtraSpecialParameter
 */
public class OutputParameter extends ExtraSpecialParameter<PrintStream, FileParameter, File>
{
    private OutputParameter(String name)
    {
        super(name);
    }

    public static ExtraSpecialParameter<PrintStream, FileParameter, File> create(String name)
    {
        OutputParameter result = new OutputParameter(name);
        result.specialPrefix = "";
        result.specialSuffix = "";
        result.addChoice("", null, "Output to File");
        result.addChoice("-", System.out, "stdout");
        result.setRegularParameter(
            new FileParameter.Builder(name + "File").writable().file().parameter()
            );
        result.setRequired(true);

        return result;
    }

    private PrintStream printStream;

    /**
     * Don't forget to close the PrintStream using {@link #close()}
     * 
     * @return System.out, or a new PrintStream if sending to a file.
     * @throws IOException
     * @priority 1
     */
    public PrintStream getOutput()
        throws IOException
    {
        if (getValue() == null) {
            printStream = new PrintStream(getRegularValue());
            return printStream;
        }
        return getValue();
    }

    /**
     * Closes the PrintStream if a new PrintStream was created by {@link #getOutput()}.
     * If a special value was used (System.out), then the stream is NOT closed.
     * 
     * @priority 1
     */
    public void close()
    {
        if (printStream != null) {
            printStream.close();
        }
    }

}
