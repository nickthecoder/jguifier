package uk.co.nickthecoder.jguifier;

import java.io.PrintStream;

/**
 * Here's example code, showing how to create a simple Task, with a couple of parameters.
 * 
 * <code><pre>
public class Example extends Task
{
    private BooleanParameter _boolean = new BooleanParameter.Builder("boolean").value(true).required().parameter();

    private IntegerParameter _integer = new IntegerParameter.Builder("integer").range(1, 10).description("One to ten")
        .parameter();

    private DoubleParameter _double = new DoubleParameter.Builder("double").range(1.0, 10.0).parameter();

    private StringParameter _shortString = new StringParameter.Builder("shortString")
        .maxLength(10).description( "A short string" ).parameter();

    private StringParameter _longString = new StringParameter.Builder("longString").stretch().parameter();
    
    private StringChoiceParameter _greeting = new StringChoiceParameter.Builder("greeting")
        .choices("Hello", "Hi", "Watcha").description( "Pick a greeting" ).parameter();

    private ChoiceParameter<PrintStream> _output = new ChoiceParameter.Builder<PrintStream>("output")
        .choice("stdout", System.out, "Normal")
        .choice("stderr", System.err, "Error")
        .value(System.err).parameter();

    private FileParameter _file = new FileParameter.Builder("file").file().mustExist().parameter();

    public Example()
    {
        super();
        addParameters(_boolean, _integer, _double, _shortString, _longString, _greeting, _output, _file);
    }

    public void run()
    {
        PrintStream out = _output.getValue();

        out.println(_greeting.getValue() + " " + _shortString.getValue());
        out.println(_longString.getValue());
    }

    public static void main(String[] argv)
    {
        Example example = new Example();
        // When using a groovy script, the task's name will be automatically assigned. See Task.guessName()
        // However, if you are calling java code from a shell script, then you should explicitly set the name of the
        // shell script like so :
        example.setName("example.sh");
        example.go(argv);
    }
}
 * </pre></code>
 */

public class Example extends Task
{
    private BooleanParameter _boolean = new BooleanParameter.Builder("boolean").value(true).required().parameter();

    private IntegerParameter _integer = new IntegerParameter.Builder("integer").range(1, 10).description("One to ten")
        .parameter();

    private DoubleParameter _double = new DoubleParameter.Builder("double").range(1.0, 10.0).parameter();

    private StringParameter _shortString = new StringParameter.Builder("shortString")
        .maxLength(10).description("A short string").parameter();

    private StringParameter _longString = new StringParameter.Builder("longString").stretch().parameter();

    private StringChoiceParameter _greeting = new StringChoiceParameter.Builder("greeting")
        .choices("Hello", "Hi", "Watcha").description("Pick a greeting").parameter();

    private ChoiceParameter<PrintStream> _output = new ChoiceParameter.Builder<PrintStream>("output")
        .choice("stdout", System.out, "Normal")
        .choice("stderr", System.err, "Error")
        .value(System.err).parameter();

    private FileParameter _file = new FileParameter.Builder("file").file().mustExist().parameter();

    public Example()
    {
        super();
        addParameters(_boolean, _integer, _double, _shortString, _longString, _greeting, _output, _file);
    }

    @Override
    public void run()
    {
        PrintStream out = _output.getValue();

        out.println(_greeting.getValue() + " " + _shortString.getValue());
        out.println(_longString.getValue());
    }

    public static void main(String[] argv)
    {
        Example example = new Example();
        // When using a groovy script, the task's name will be automatically assigned. See Task.guessName()
        // However, if you are calling java code from a shell script, then you should explicitly set the name of the
        // shell script like so :
        example.setName("example.sh");
        example.go(argv);
    }
}
