package uk.co.nickthecoder.jguifier;

import java.io.PrintStream;

/**
 * An example Task, which doesn't do anything useful. See the source code to get a feel for how to create
 * {@link Parameter}s, use them within {@link Task}s.
 * 
 * @priority 5
 */
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
        example.runFromMain(argv);
    }
}
