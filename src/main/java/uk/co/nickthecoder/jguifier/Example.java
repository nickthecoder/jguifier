package uk.co.nickthecoder.jguifier;

import java.io.PrintStream;

/**
 * Here's example code, showing how to create a simple Task, with a couple of parameters.
 * 
 * <code><pre>
 * 

public class Example extends Task
{
    private IntegerParameter _number = new IntegerParameter("number", "Very Long Label")
        .range(1, 10);

    private StringChoiceParameter _greeting = new StringChoiceParameter("greeting", "Greeting")
        .choices("Hello", "Hi", "Watcha");

    private StringParameter _message = new StringParameter("message", "Message")
        .stretch();

    private ChoiceParameter<PrintStream> _output = new ChoiceParameter<PrintStream>("output", "Output")
        .choice("stdout", System.out, "Normal")
        .choice("stderr", System.err, "Error").value(System.err);

    private FileParameter _file = new FileParameter("file", "File");

    public Example()
    {
        super("Example");
        addParameters(_number, _greeting, _message, _output, _file);
    }

    private PrintStream getStream()
    {
        return _output.getValue();
    }

    protected void run() throws Exception
    {
        String greeting = _greeting.getValue();
        String message = _message.getValue();

        getStream().println(greeting);
        getStream().println(message);
    }

    public static void main(String[] argv)
    {
        Example example = new Example();
        example.lookupDefaults();
        example.runFromMain(argv);
    }
}

 * </pre></code>
 */

public class Example extends Task
{
    private IntegerParameter _integer = new IntegerParameter("integer", "Integer")
        .range(1, 10);

    private DoubleParameter _double = new DoubleParameter("double", "Double")
        .range(1.0, 10.0);

    private StringChoiceParameter _greeting = new StringChoiceParameter("greeting", "Greeting")
        .choices("Hello", "Hi", "Watcha");

    private StringParameter _message = new StringParameter("message", "Message")
        .stretch();

    private ChoiceParameter<PrintStream> _output = new ChoiceParameter<PrintStream>("output", "Output")
        .choice("stdout", System.out, "Normal")
        .choice("stderr", System.err, "Error").value(System.err);

    private FileParameter _file = new FileParameter("file", "File");

    public Example()
    {
        super();
        addParameters(_integer, _double, _greeting, _message, _output, _file);
    }

    private PrintStream getStream()
    {
        return _output.getValue();
    }

    @Override
    protected void run() throws Exception
    {
        String greeting = _greeting.getValue();
        String message = _message.getValue();

        getStream().println(greeting);
        getStream().println(message);
    }

    public static void main(String[] argv)
    {
        Example example = new Example();
        example.lookupDefaults();
        example.runFromMain(argv);
    }
}
