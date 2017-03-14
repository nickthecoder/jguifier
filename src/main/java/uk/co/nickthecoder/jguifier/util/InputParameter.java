package uk.co.nickthecoder.jguifier.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import uk.co.nickthecoder.jguifier.parameter.ExtraSpecialParameter;
import uk.co.nickthecoder.jguifier.parameter.FileParameter;

/**
 * A customised parameter, suitable as an "input" parameter, with a special value for System.in (with key
 * &quot;-&quot;), and a regular FileParameter otherwise.
 * <p>
 * Unlike other Parameters, this does not have a Builder, instead, use {@link #create(String)}.
 * </p>
 * If you want stdin to be the default value, then : <code>output.setValue( System.in )</code>
 * 
 * @param name
 *            The name of the parameter
 * @return The ExtraSpecialParameter
 */
public class InputParameter extends ExtraSpecialParameter<InputStream, FileParameter, File>
{
    private InputParameter(String name)
    {
        super(name);
    }

    public static ExtraSpecialParameter<InputStream, FileParameter, File> create(String name)
    {
        InputParameter result = new InputParameter(name);
        result.specialPrefix = "";
        result.specialSuffix = "";
        result.addChoice("", null, "Input from File");
        result.addChoice("-", System.in, "stdin");
        result.setRegularParameter(
            new FileParameter.Builder(name + "File").file().mustExist().parameter()
            );
        result.setRequired(true);

        return result;
    }

    private InputStream inputStream;

    /**
     * Don't forget to close the PrintStream using {@link #close()}
     * 
     * @return System.out, or a new PrintStream if sending to a file.
     * @throws IOException
     * @priority 1
     */
    public InputStream getInput()
        throws IOException
    {
        if (getValue() == null) {
            inputStream = new FileInputStream(getRegularValue());
            return inputStream;
        }
        return getValue();
    }

    /**
     * Closes the InputStream if a new InputStream was created by {@link #getInput()}.
     * If a special value was used (System.in), then the stream is NOT closed.
     * 
     * @priority 1
     */
    public void close()
        throws IOException
    {
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
