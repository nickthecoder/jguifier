package uk.co.nickthecoder.jguifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import uk.co.nickthecoder.jguifier.util.Util;

/**
 * Tasks are at the center of jguifier. Each program will define its parameters in the constructor,
 * override the run method, and have a main entry point.
 * 
 * <pre>
 * <code>
 *    public class MyTask extends Task {
 *    
 *       private StringParameter m_foo = new StringParameter( "foo" );
 *       private IntegetParameter m_bar = new IntegerParameter( "bar").setRange( 0, 10 );
 *       
 *       public MyTask() {
 *          addParameters( foo, bar );
 *       }
 *       
 *       protected void run() {
 *         int bar = m_bar.getValue();
 *         String foo = m_foo.getValue();
 *         // Perform the task's actions
 *       }
 *       
 *       public static void main( String[] argv ) {
 *         MyTask myTask = new MyTask();
 *         myTask.callFromMain( argv );
 *       }
 *    }
 *    </code>
 * </pre>
 * 
 */
public abstract class Task
{
    private String _description = "";

    /**
     * The list of all parameters is the order which they were added.
     */
    private List<Parameter> _parameters;

    /**
     * The hierarchical list of parameters (groups can have sub-groups).
     */
    private GroupParameter _root = new GroupParameter("__ROOT");

    /**
     * A map of the parameters keyed on their name.
     */
    private HashMap<String, Parameter> _parametersMap;

    /**
     * If true, then the GUI will be displayed regardless of whether the parameters have been entered
     * correctly. The user can override this value by adding the command line option --no-prompt.
     * 
     * Set this to true for Tasks which will always tend to be
     */
    private boolean _forcePrompt = false;

    /**
     * If true, then the GUI will not be displayed, even if the parameters are not valid, the command
     * will just output an error message and then exit.
     * 
     * Most Tasks should keep this value at the default (false).
     * 
     * The user can override this value by adding the command line option --prompt
     */
    private boolean _neverPrompt = false;

    /**
     * Along the debug method will do nothing when this is false, so you can sprinkle calls to
     * debug throughout your task, and while in development set debug to false, then once its ready
     * for production, set it to true.
     * 
     * This value can be overridden by the user with the command line option --debug and --no-debug
     */
    public boolean debug = false;

    public Task()
    {
        _parameters = new LinkedList<Parameter>();
        _parametersMap = new HashMap<String, Parameter>();
    }

    public GroupParameter getRootParameter()
    {
        return _root;
    }

    public Task parameters(Parameter... parameters)
    {
        addParameters(parameters);
        return this;
    }

    public void addParameters(Parameter... parameters)
    {
        for (Parameter parameter : parameters) {
            addParameter(parameter);
        }
    }

    private void addParameterToCollections(Parameter parameter)
    {
        assert !this._parametersMap.containsKey(parameter.getName()) : "Duplicate parameter name";

        if (parameter instanceof GroupParameter) {
            GroupParameter group = (GroupParameter) parameter;
            for (Parameter child : group.getChildren()) {
                addParameterToCollections(child);
            }
        } else {
            this._parameters.add(parameter);
            this._parametersMap.put(parameter.getName(), parameter);
        }

        if (parameter instanceof BooleanParameter) {
            BooleanParameter bp = (BooleanParameter) parameter;
            if (bp.getOppositeName() != null) {
                assert !this._parametersMap.containsKey(bp.getOppositeName()) : "Duplicate parameter opposite name";
                this._parametersMap.put(bp.getOppositeName(), parameter);
            }
        }
    }

    /**
     * Looks up the defaultValues in the default location. For linux this is ~/.local/jguifier/CLASSNAME.defaults
     * Currently, this is also the location that is tried for other operating systems too, which probably won't work!
     * 
     * @return this
     */
    public Task lookupDefaults()
    {
        File defaultsFile = Util.createFile(
            new File(System.getProperty("user.home")), ".local", "jguifier", getName() + ".defaults");

        return lookupDefaults(defaultsFile);
    }

    /**
     * 
     * @return The name of this Task, the default implementation returns the classname with the package name removed.
     *         i.e. The {@link Example} task will return just "Example".
     */
    public String getName()
    {
        return this.getClass().getName().replaceAll(".*\\.", "");
    }

    public Task lookupDefaults(File file)
    {
        try {
            FileInputStream fis = new FileInputStream(file);

            // Construct BufferedReader from InputStreamReader
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String line = null;
            while ((line = br.readLine()) != null) {
                parseDefault(line);
            }

            br.close();
        } catch (Exception e) {
            // Do nothing
        }
        return this;
    }

    private void parseDefault(String line)
    {
        line = line.trim();
        if ((line.startsWith("//")) || (line.startsWith("#"))) {
            // Do nothing - a comment
        } else {
            int eq = line.indexOf("=");
            if (eq > 0) {
                String name = line.substring(0, eq).trim();
                String value = line.substring(eq + 1).trim();

                value = Util.unescapeDoubleQuotes(value);

                Parameter parameter = this.findParameter(name);
                if (parameter instanceof ValueParameter<?>) {
                    try {
                        ((ValueParameter<?>) parameter).setStringValue(value);
                    } catch (ParameterException e) {
                        // Do nothing - illegal values are just ignored.
                    }
                }
            }
        }
    }

    public Task parameter(Parameter parameter)
    {
        addParameter(parameter);
        return this;
    }

    public void addParameter(Parameter parameter)
    {
        _root.addParameter(parameter);
        addParameterToCollections(parameter);
    }

    public Parameter findParameter(String name)
    {
        return _parametersMap.get(name);
    }

    public List<Parameter> getParameters()
    {
        return _parameters;
    }

    public Task prompt()
    {
        _forcePrompt = true;
        return this;
    }

    public Task prompt(boolean value)
    {
        _forcePrompt = value;
        _neverPrompt = !value;
        return this;
    }

    public void help()
    {
        taskHelp();
        guifierHelp();
    }

    public void taskHelp()
    {
        System.out.println();
        System.out.println(getName());
        System.out.println();

        if (!Util.empty(_description)) {
            System.out.println(_description);
            System.out.println();
        }

        for (Parameter parameter : _parameters) {
            System.out.println("    " + parameter.getHelp());
        }
        System.out.println();
    }

    public void guifierHelp()
    {
        System.out.println("guifier options :");
        System.out.println("    --help                  : Displays this text");
        System.out
            .println("    --prompt, --no-prompt   : Overrides whether the parameters should be prompted using a GUI");
        System.out
            .println("    --debug, --no-debug     : May be useful to turn on debugging while developing your script");
        System.out.println();
    }

    public void debug(Object message)
    {
        if (debug) {
            System.out.println(message.toString());
        }
    }

    private boolean parseArgs(String[] argv)
        throws TaskException
    {

        for (int i = 0; i < argv.length; i++) {
            String arg = argv[i];

            if ((i == 0) && "--autocomplete".equals(arg)) {

                autocomplete(argv);
                return false;

            } else if ("--help".equals(arg)) {
                if (i + 1 >= argv.length) {
                    help();
                } else {
                    Parameter parameter = findParameter(argv[i + 1]);
                    if (parameter == null) {
                        help();
                    } else {
                        System.out.println(parameter.getDescription());
                    }
                }

                return false;

            } else if ("--no-prompt".equals(arg)) {
                _forcePrompt = false;
                _neverPrompt = true;

            } else if ("--prompt".equals(arg)) {
                _forcePrompt = true;
                _neverPrompt = false;

            } else if ("--no-debug".equals(arg)) {
                debug = false;

            } else if ("--debug".equals(arg)) {
                debug = true;

            } else if (arg.startsWith("--")) {

                String name;
                String value;
                Parameter parameter;

                String nameValue = arg.substring(2);
                int eqPos = nameValue.indexOf("=");

                if (eqPos > 0) {
                    // Parameter in the form --name=value
                    name = nameValue.substring(0, eqPos);
                    value = nameValue.substring(eqPos + 1);
                    parameter = findParameter(name);
                    if (parameter == null) {
                        throw new TaskException("Unknown parameter : " + name);
                    }
                    if (parameter instanceof ValueParameter) {
                        ((ValueParameter<?>) parameter).setStringValue(value);
                    } else {
                        throw new TaskException("Parameter cannot hold a value : " + name);
                    }

                } else {
                    name = nameValue;
                    parameter = findParameter(name);
                    if (parameter == null) {
                        throw new TaskException("Unknown parameter : " + name);
                    }

                    if (parameter instanceof BooleanParameter) {
                        BooleanParameter booleanParameter = (BooleanParameter) parameter;

                        // Could be --name (which means true),
                        // or --name value (where value is 0,1,true or false)
                        // We can know which by looking at the next argument if there is one.
                        if (i + 1 >= argv.length) {
                            // Is --name (which means true)
                            booleanParameter.setValue(true);
                        } else {
                            value = argv[i + 1];
                            if (value.startsWith("--")) {
                                // Is --name (which means true)
                                booleanParameter.setValue(true);
                            } else {
                                // Is --name value, so parse value, and then skip over it.
                                booleanParameter.setStringValue(value);
                                i++;
                            }
                        }
                        // See BooleanParameter.setOppositeName for details
                        if (name.equals(booleanParameter.getOppositeName())) {
                            booleanParameter.setValue(!booleanParameter.getValue());
                        }

                    } else {
                        // Parameter in the form --name value

                        if (i + 1 >= argv.length) {
                            throw new ParameterException(parameter, "Value not given");
                        }
                        value = argv[i + 1];
                        i++;
                        if (parameter instanceof ValueParameter<?>) {
                            ((ValueParameter<?>) parameter).setStringValue(value);
                        } else {
                            throw new ParameterException(parameter, "Parameter cannot hold a value : " + name);
                        }
                    }
                }

            } else {
                throw new TaskException("Unexpected value : " + arg);
            }
        }

        return true;
    }

    private void debug(String s)
    {
        // System.err.println(s);
    }

    /**
     * This is used in conjunction with command line tab auto-complete.
     * To prompt for valid command arguments, and their values, press tab (and/or tab twice) while typing a command.
     * 
     * For this to take effect add something like this to ~/.bash_completion : <code><pre>
     *     _JGuifierComplete ()
     *     {
     *         COMPREPLY=( $(${COMP_WORDS[0]} --autocomplete "${COMP_CWORD}" "${COMP_WORDS[@]}" ) )
     *         return 0
     *     }
     *     JGUIFIER_SCRIPTS=`cd ~/bin;echo *.bsh *.groovy`
     *     complete -F _JGuifierComplete -o filenames ${JGUIFIER_SCRIPTS}
     * </pre></code>
     *
     * @param argv
     *            The command line arguments sent from the tab complete shell function.
     */
    void autocomplete(String[] argv)
    {
        int index = Integer.parseInt(argv[1]) - 1;
        String[] params = new String[argv.length - 3];
        for (int i = 3; i < argv.length; i++) {
            params[i - 3] = argv[i];
            debug("Params[" + (i - 3) + "] = " + argv[i]);
        }

        debug("Index = " + index);

        String cur = index >= params.length ? "" : params[index];
        String prev = (index > 0) ? params[index - 1] : "";

        debug("Autocomplete arguments");
        for (String arg : argv) {
            debug("   '" + arg + "'");
        }
        debug("Current : " + cur);
        debug("Prev : " + prev);
        debug("");

        /*
         * If the user has edited a command, deleting a parameter value, then cur will be the
         * next parameter's name. We don't want that, we want a blank.
         * If example if the command currently reads :
         * mycommand --foo --bar barValue
         * and the cursor is after --foo, we want to prompt for parameter foo's choices, and ignore "--bar".
         */
        if ((index != params.length) && (prev.startsWith("--")) && (cur.startsWith("--"))) {
            cur = "";
            debug("Setting cur to  blank");
        }

        if (prev.startsWith("--")) {
            String name = prev.substring(2);
            Parameter parameter = findParameter(name);
            if (parameter != null) {
                parameter.autocomplete(cur);
                // System.err.println( parameter.getHelp() );
            }
        } else {
            for (Parameter parameter : getParameters()) {
                autocompleteFilter("--" + parameter.getName(), cur);
            }
        }

    }

    public static void autocompleteFilter(String value, String prefix)
    {
        if (value.startsWith(prefix)) {
            System.out.println(value);
        }
    }

    public void runFromMain(String[] argv)
    {
        try {
            if (!parseArgs(argv)) {
                return;
            }

            if (debug) {
                debug("Parameters : ");
                for (Parameter parameter : _parameters) {
                    debug(parameter);
                }
            }

            // Abort if any parameters are invalid.
            try {
                for (Parameter parameter : _parameters) {
                    parameter.check();
                }
                check();
            } catch (ParameterException e) {
                if (_neverPrompt) {
                    System.out.println(e);
                    System.exit(1);
                }
                promptTask();
                return;
            }

            if (_forcePrompt) {
                promptTask();
            } else {
                run();
            }

        } catch (TaskException e) {
            System.out.println(e);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void promptTask()
    {
        TaskPrompter taskPrompter = new TaskPrompter(this);
        taskPrompter.prompt();
    }

    /**
     * If your task needs to compare one parameter with another to ensure that the
     * parameters are valid, this is where you should do it.
     * Parameter.check should only check its own value, not the value of other parameters.
     */
    public void check() throws ParameterException
    {
        // Default does nothing
    }

    protected abstract void run() throws Exception;
}
