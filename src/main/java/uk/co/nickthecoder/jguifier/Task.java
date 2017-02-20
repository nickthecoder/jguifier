package uk.co.nickthecoder.jguifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import uk.co.nickthecoder.jguifier.util.NullOutputStream;
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
    private String _name = null;

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
     * Parameters used by all Tasks, such as "--prompt", "--help", "--description", "--taskName" etc.
     */
    private HashMap<String, Parameter> _metaParametersMap;

    /**
     * The by default, all output is thrown away, but if the --debug parameter is set, then
     * debug becomes System.out.
     * So, pepper your code with <code>debug.println(...)</code> statements, and most the results
     * will only be seen when --debug is set.
     */
    public PrintStream debug = NullOutputStream.nullPrintStream;

    private StringParameter _taskNameParameter;
    private BooleanParameter _helpParameter;
    private BooleanParameter _autoCompleteParameter;
    private BooleanParameter _debugParameter;
    private BooleanParameter _lookupDefaultsParameter;
    private BooleanParameter _promptParameter;

    public Task()
    {
        _parameters = new LinkedList<Parameter>();
        _parametersMap = new HashMap<String, Parameter>();
        _metaParametersMap = new HashMap<String, Parameter>();

        _taskNameParameter = new StringParameter("taskName", null);
        _helpParameter = new BooleanParameter("help", false);
        _autoCompleteParameter = new BooleanParameter("autocomplete", false);
        _debugParameter = new BooleanParameter("debug", false).oppositeName("no-debug");
        _lookupDefaultsParameter = new BooleanParameter("lookupDefaults", true);
        _promptParameter = new BooleanParameter("prompt", null).oppositeName("no-prompt");

        addMetaParameters(_helpParameter, _autoCompleteParameter, _promptParameter, _debugParameter,
            _lookupDefaultsParameter);

    }

    public void setName(String name)
    {
        _name = name;
    }

    /**
     * Sets the name of the task based on the filename of the script containing this Task class.
     * Advanced scenarios may need to explicitly use {@link #setName(String)} or {@link #guessName(Object)}.
     */
    public void guessName()
    {
        guessName(this);
    }

    /**
     * Sets the name of the task based on the filename of the source code containing the definition of <code>obj</code>.
     * This is useful if you want to create many script files, which all use the exact same Task subclass,
     * but the name of the Task should be based on the script's filename, not the filename containing the
     * Task definition.
     * <p>
     * This is especially true, because default values are stored based on a Task's name. So having multiple tasks, with
     * different names, but sharing a common Task class can be useful, because each can have their own default values.
     * </p>
     */
    public void guessName(Object obj)
    {
        URL url = obj.getClass().getProtectionDomain().getCodeSource().getLocation();
        File file = new File(url.getPath());
        String name = file.getName();
        if (name.endsWith(".jar")) {
            setName(obj.getClass().getSimpleName());
        } else {
            setName(file.getName());
        }
    }

    /**
     * 
     * @return The name of this Task, the default implementation returns the classname with the package name removed.
     *         i.e. The {@link Example} task will return just "Example".
     */
    public String getName()
    {
        if (_name == null) {
            this.guessName();
        }
        return _name;
    }

    public Task name(String name)
    {
        setName(name);
        return this;
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

    private void addMetaParameters(Parameter... parameters)
    {
        for (Parameter parameter : parameters) {
            _metaParametersMap.put(parameter.getName(), parameter);
            
            if (parameter instanceof BooleanParameter) {
                BooleanParameter bp = (BooleanParameter) parameter;
                if (bp.getOppositeName() != null) {
                    assert !this._metaParametersMap.containsKey(bp.getOppositeName()) : "Duplicate parameter opposite name";
                    this._metaParametersMap.put(bp.getOppositeName(), parameter);
                }
            }

        }
    }

    public File getDefaultsFile()
    {
        return Util.createFile(
            new File(System.getProperty("user.home")), ".local", "jguifier", getName() + ".defaults");
    }

    /**
     * Looks up the defaultValues in the default location. For linux this is ~/.local/jguifier/CLASSNAME.defaults
     * Currently, this is also the location that is tried for other operating systems too, which probably won't work!
     * 
     * @return this
     */
    public Task lookupDefaults()
    {
        File defaultsFile = getDefaultsFile();
        debug.println("Looking up parameter defaults in : " + defaultsFile);

        return lookupDefaults(defaultsFile);
    }

    public String getCommandString()
    {
        return getName() + _root.getCommandString();
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

    public void saveDefaults()
        throws IOException
    {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(getDefaultsFile());
            PrintWriter out = new PrintWriter(fos);

            saveDefaults(out, getRootParameter());

            out.flush();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }

    }

    private void saveDefaults(PrintWriter out, GroupParameter gp)
    {
        for (Parameter parameter : getParameters()) {
            if (parameter instanceof ValueParameter) {
                ValueParameter<?> vp = (ValueParameter<?>) parameter;
                String value = vp.getStringValue();
                if (!Util.empty(value)) {
                    out.println(vp.getName() + "=" + vp.getStringValue());
                }

            } else if (parameter instanceof GroupParameter) {
                saveDefaults(out, (GroupParameter) parameter);
            }
        }
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

                value = Util.undoubleQuote(value);

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
        Parameter result = _parametersMap.get(name);
        if (result != null) {
            return result;
        }
        return _metaParametersMap.get(name);
    }

    public List<Parameter> getParameters()
    {
        return _parameters;
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
        // Replace with help from meta parameters
        /*
         * System.out.println("guifier options :");
         * System.out.println("    --help                  : Displays this text");
         * System.out
         * .println("    --prompt, --no-prompt   : Overrides whether the parameters should be prompted using a GUI");
         * System.out
         * .println("    --debug, --no-debug     : May be useful to turn on debugging while developing your script");
         * System.out.println();
         */
    }

    private boolean parseArgs(String[] argv)
        throws TaskException
    {

        for (int i = 0; i < argv.length; i++) {
            String arg = argv[i];

            if (arg.startsWith("--")) {

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
                if ((parameter == _autoCompleteParameter) && _autoCompleteParameter.getValue()) {
                    autocomplete(argv);
                    return false;
                }

            } else {
                throw new TaskException("Unexpected value : " + arg);
            }

        }

        if (_debugParameter.getValue()) {
            debug = System.out;
        }

        if (_helpParameter.getValue()) {
            help();
            return false;
        }

        if (_taskNameParameter.getValue() != null) {
            setName(_taskNameParameter.getValue());
        }

        return true;
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
            debug.println("Params[" + (i - 3) + "] = " + argv[i]);
        }

        debug.println("Index = " + index);

        String cur = index >= params.length ? "" : params[index];
        String prev = (index > 0) ? params[index - 1] : "";

        debug.println("Autocomplete arguments");
        for (String arg : argv) {
            debug.println("   '" + arg + "'");
        }
        debug.println("Current : " + cur);
        debug.println("Prev : " + prev);
        debug.println("");

        /*
         * If the user has edited a command, deleting a parameter value, then cur will be the
         * next parameter's name. We don't want that, we want a blank.
         * If example if the command currently reads :
         * mycommand --foo --bar barValue
         * and the cursor is after --foo, we want to prompt for parameter foo's choices, and ignore "--bar".
         */
        if ((index != params.length) && (prev.startsWith("--")) && (cur.startsWith("--"))) {
            cur = "";
            debug.println("Setting cur to  blank");
        }

        if (prev.startsWith("--")) {
            String name = prev.substring(2);
            Parameter parameter = findParameter(name);
            if (parameter != null) {
                parameter.autocomplete(cur);
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

    public void go(String[] argv)
    {
        try {
            if (!parseArgs(argv)) {
                return;
            }

            if (_lookupDefaultsParameter.getValue()) {
                lookupDefaults();
            }

            debug.println("Parameters : ");
            for (Parameter parameter : _parameters) {
                debug.println(parameter);
            }

            // Abort if any parameters are invalid.
            try {
                for (Parameter parameter : _parameters) {
                    parameter.check();
                }
                check();
            } catch (ParameterException e) {
                if (_promptParameter.getValue() == Boolean.FALSE) {
                    System.out.println(e);
                    System.exit(1);
                }
                promptTask();
                return;
            }

            if (_promptParameter.getValue() == Boolean.TRUE) {
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
