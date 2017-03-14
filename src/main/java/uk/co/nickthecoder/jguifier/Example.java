package uk.co.nickthecoder.jguifier;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;

/**
 * Here's example code, showing how to create a simple Task, with a different types of parameters.
 * 
 * <code><pre>

public class Example extends Task
{
    private BooleanParameter _boolean = new BooleanParameter.Builder("boolean").value(true).required()
        .description("Tick for goodies").parameter();

    private IntegerParameter _integer = new IntegerParameter.Builder("integer").range(1, 10).optional()
        .description("One to ten").parameter();

    private DoubleParameter _double = new DoubleParameter.Builder("double").range(1.0, 10.0).optional()
        .parameter();

    private StringParameter _shortString = new StringParameter.Builder("shortString")
        .maxLength(10).description("A short string").parameter();

    private StringParameter _longString = new StringParameter.Builder("longString").stretch().parameter();

    private StringParameter _multiLineString = new StringParameter.Builder("multiLineString").stretch().multiLine()
        .parameter();

    private StringChoiceParameter _greeting = new StringChoiceParameter.Builder("greeting")
        .choices("Hello", "Hi", "Watcha").description("Pick a greeting").parameter();

    private PatternParameter _regex = new PatternParameter.Builder("Regex")
        .description("A regular expression (or a glob)").parameter();

    private ChoiceParameter<PrintStream> _output = new ChoiceParameter.Builder<PrintStream>("output")
        .choice("stdout", System.out, "Normal")
        .choice("stderr", System.err, "Error")
        .value(System.err).parameter();

    private FileParameter _file = new FileParameter.Builder("file").file().mustExist().parameter();

    // Choose one of the given date formats, or type your own format string
    private SpecialParameter<StringParameter, String> _dateFormat =
        new SpecialParameter.Builder<StringParameter, String>("dateFormat")
            .prefix("")
            .choice("dd-MM-yyyy hh:mm")
            .choice("dd-MMM-yyyy hh:mm")
            .choice("yyyy-MMM-dd hh:mm")
            .choice("yyyy-MM-dd hh:mm")
            .regular(new StringParameter.Builder("dateFormat")
                .value(new SimpleDateFormat().toPattern()).optional()
                .parameter()
            )
            .parameter();

    // Choose System.out or System.err, or enter any File
    private ExtraSpecialParameter<PrintStream, FileParameter, File> _special =
        new ExtraSpecialParameter.Builder<PrintStream, FileParameter, File>("special")
            .choice("file", null, "Output to File")
            .choice("out", System.out, "Standard Output")
            .choice("err", System.err, "Standard Error")
            .value(System.out)
            .regular(
                new FileParameter.Builder("specialFile").writable().parameter()
            )
            .parameter();

    public Example()
    {
        super();
        addParameters(_boolean, _integer, _double,
            _shortString, _longString, _multiLineString, _greeting, _regex, _output, _file,
            _dateFormat, _special);
    }

    public void body()
    {
        // Note, this isn't good style, because out is not guaranteed to be closed correctly.
        // For a better solution, use OutputFile, and call OutputFile.close() in Task.post()
        PrintStream out = _output.getValue();

        out.println(_greeting.getValue() + " " + _shortString.getValue());
        out.println(_longString.getValue());

        out.close();
    }

    public static void main(String[] argv)
    {
        Example example = new Example();
        // When using a groovy script, the task's name will be automatically assigned. See Task.guessName()
        // However, if you are calling java code from a shell script, then you should explicitly set the name of the
        // shell script like so :
        example.setName("example.sh");
        new TaskCommand(example).go(argv);
    }
}

 * </pre></code>
 */

public class Example extends Task
{
    private BooleanParameter _boolean = new BooleanParameter.Builder("boolean").value(true).required()
        .description("Tick for goodies").parameter();

    private IntegerParameter _integer = new IntegerParameter.Builder("integer").range(1, 10).optional()
        .description("One to ten").parameter();

    private DoubleParameter _double = new DoubleParameter.Builder("double").range(1.0, 10.0).optional()
        .parameter();

    private StringParameter _shortString = new StringParameter.Builder("shortString")
        .maxLength(10).description("A short string").parameter();

    private StringParameter _longString = new StringParameter.Builder("longString").stretch().parameter();

    private StringParameter _multiLineString = new StringParameter.Builder("multiLineString").stretch().multiLine()
        .parameter();

    private StringChoiceParameter _greeting = new StringChoiceParameter.Builder("greeting")
        .choices("Hello", "Hi", "Watcha").description("Pick a greeting").parameter();

    private PatternParameter _regex = new PatternParameter.Builder("Regex")
        .description("A regular expression (or a glob)").parameter();

    private ChoiceParameter<PrintStream> _output = new ChoiceParameter.Builder<PrintStream>("output")
        .choice("stdout", System.out, "Normal")
        .choice("stderr", System.err, "Error")
        .value(System.err).parameter();

    private FileParameter _file = new FileParameter.Builder("file").file().mustExist().parameter();

    // Choose one of the given date formats, or type your own format string
    private SpecialParameter<StringParameter, String> _dateFormat = new SpecialParameter.Builder<StringParameter, String>(
        "dateFormat")
            .prefix("")
            .choice("dd-MM-yyyy hh:mm")
            .choice("dd-MMM-yyyy hh:mm")
            .choice("yyyy-MMM-dd hh:mm")
            .choice("yyyy-MM-dd hh:mm")
            .regular(new StringParameter.Builder("dateFormat")
                .value(new SimpleDateFormat().toPattern()).optional()
                .parameter())
            .parameter();

    // Choose System.out or System.err, or enter any File
    private ExtraSpecialParameter<PrintStream, FileParameter, File> _special = new ExtraSpecialParameter.Builder<PrintStream, FileParameter, File>(
        "special")
            .choice("file", null, "Output to File")
            .choice("out", System.out, "Standard Output")
            .choice("err", System.err, "Standard Error")
            .value(System.out)
            .regular(
                new FileParameter.Builder("specialFile").writable().parameter())
            .parameter();

    public Example()
    {
        super();
        addParameters(_boolean, _integer, _double,
            _shortString, _longString, _multiLineString, _greeting, _regex, _output, _file,
            _dateFormat, _special);
    }

    @Override
    public void body()
    {
        // Note, this isn't good style, because out is not guaranteed to be closed correctly.
        // For a better solution, use OutputFile, and call OutputFile.close() in Task.post()
        PrintStream out = _output.getValue();

        out.println(_greeting.getValue() + " " + _shortString.getValue());
        out.println(_longString.getValue());

        out.close();
    }

    public static void main(String[] argv)
    {
        Example example = new Example();
        // When using a groovy script, the task's name will be automatically assigned. See Task.guessName()
        // However, if you are calling java code from a shell script, then you should explicitly set the name of the
        // shell script like so :
        example.setName("example.sh");
        new TaskCommand(example).go(argv);
    }
}
