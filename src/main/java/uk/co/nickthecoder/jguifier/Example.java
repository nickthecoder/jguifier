package uk.co.nickthecoder.jguifier;

import java.awt.Color;
import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;

import javax.swing.SwingUtilities;

import uk.co.nickthecoder.jguifier.parameter.BooleanParameter;
import uk.co.nickthecoder.jguifier.parameter.ChoiceParameter;
import uk.co.nickthecoder.jguifier.parameter.DoubleParameter;
import uk.co.nickthecoder.jguifier.parameter.ExtraSpecialParameter;
import uk.co.nickthecoder.jguifier.parameter.FileParameter;
import uk.co.nickthecoder.jguifier.parameter.GroupParameter;
import uk.co.nickthecoder.jguifier.parameter.IntegerParameter;
import uk.co.nickthecoder.jguifier.parameter.ListItem;
import uk.co.nickthecoder.jguifier.parameter.ListParameter;
import uk.co.nickthecoder.jguifier.parameter.MultipleParameter;
import uk.co.nickthecoder.jguifier.parameter.PatternParameter;
import uk.co.nickthecoder.jguifier.parameter.SpecialParameter;
import uk.co.nickthecoder.jguifier.parameter.StringChoiceParameter;
import uk.co.nickthecoder.jguifier.parameter.StringParameter;

/**
 * Here's example code, showing how to create a Task, with a many different types of parameters.
 * 
 * You can see the latest source code on
 * <a href=
 * "https://github.com/nickthecoder/jguifier/blob/master/src/main/java/uk/co/nickthecoder/jguifier/Example.java">GitHub</a>
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

    private StringChoiceParameter _fruit = new StringChoiceParameter.Builder("fruit")
        .choices("Apple", "Raspberry", "Orange").description("Pick a fruit").radio().parameter();

    private PatternParameter _regex = new PatternParameter.Builder("Regex")
        .description("A regular expression (or a glob)").parameter();

    private GroupParameter _extras = new GroupParameter("extras");

    private ChoiceParameter<PrintStream> _output = new ChoiceParameter.Builder<PrintStream>("output")
        .choice("stdout", System.out, "Normal")
        .choice("stderr", System.err, "Error")
        .value(System.err).parameter();

    private FileParameter _file = new FileParameter.Builder("file").file().mustExist().parameter();

    private MultipleParameter<FileParameter, File> _files = new FileParameter.Builder("").file().mustExist()
        .multipleParameter("files");

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

    private MultipleParameter<IntegerParameter, Integer> manyInts = new IntegerParameter.Builder("")
        .value(1).range(0, 100).description("lots of integers")
        .multipleParameter("manyInts");

    private ListParameter<ExampleItem> _colors = new ListParameter.Builder<ExampleItem>("colors")
        .add(new ExampleItem("Red", Color.RED))
        .add(new ExampleItem("Green", Color.GREEN))
        .add(new ExampleItem("Yellow", Color.YELLOW))
        .add(new ExampleItem("Blue", Color.BLUE))
        .add(new ExampleItem("Magenta", Color.MAGENTA))
        .add(new ExampleItem("Cyan", Color.CYAN))
        .add(new ExampleItem("White", Color.WHITE))
        .parameter();

    public Example()
    {
        super();

        setDescription("This is an example Task, to show how to build each type of Parameter\nHave fun!");

        _extras.addChildren(_output, _file, _files, _dateFormat, _special);
        addParameters(_boolean, _integer, _double, _shortString, _longString, _greeting, _fruit,
            _regex, _extras, manyInts, _multiLineString, _colors);

        _colors.add("Green");
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

    public static void main(final String[] argv)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                Example example = new Example();
                // When using a groovy script, the task's name will be automatically assigned. See Task.guessName()
                // However, if you are calling java code from a shell script, then you should explicitly set the name of
                // the
                // shell script like so :
                example.setName("example.sh");
                new TaskCommand(example).go(argv);

            }
        });
    }

    static public class ExampleItem implements ListItem<Color>
    {
        public String name;

        public Color color;

        /**
         * Needed for Serializable
         */
        public ExampleItem()
        {
        }

        public ExampleItem(String name, Color color)
        {
            this.name = name;
            this.color = color;
        }

        @Override
        public Color getValue()
        {
            return color;
        }

        @Override
        public String getStringValue()
        {
            return name;
        }

        @Override
        public Color parse(String stringValue)
        {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public String toString()
        {
            return name;
        }
    }
}
