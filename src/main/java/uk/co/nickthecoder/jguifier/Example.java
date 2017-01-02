package uk.co.nickthecoder.jguifier;

import java.io.PrintStream;

public class Example extends Task
{
    private IntegerParameter _number = new IntegerParameter("number", "Very Long Label")
        .range(1, 10);
    
    private ChoiceParameter<String> _greeting = new ChoiceParameter<String>(
        "greeting", "Greeting").choices(new String[] { "Hello", "Hi", "Watcha" });

    private StringParameter _message = new StringParameter("message",
        "Message", "Nice to meet you.").stretch();

    private MapChoiceParameter<String, PrintStream> _output = new MapChoiceParameter<String, PrintStream>(
        "output", "Output").choice("stdout", System.out, "Normal")
        .choice("stderr", System.err, "Error").value(System.err);

    private FileParameter _file = new FileParameter( "file", "File" );
    
    public Example()
    {
        super("Example");
        addParameters(_number, _greeting, _message, _output, _file);
    }

    private PrintStream getStream()
    {
        return _output.getMappedValue();
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
