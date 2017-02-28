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

import uk.co.nickthecoder.jguifier.util.Exec;
import uk.co.nickthecoder.jguifier.util.FileLister;
import uk.co.nickthecoder.jguifier.util.NullOutputStream;
import uk.co.nickthecoder.jguifier.util.Util;

/**
 * Tasks are at the center of jguifier. Your command line program should sub-cass Task, writing it logic in the
 * {@link #run()} method. Typically a Task defines one or more {@link Parameter}s as fields, and
 * initialise them in your Task's constructor, and then calling {@link #addParameters(Parameter...)}.
 * <p>
 * See {@link Example}, for an example Task written in Java.
 * </p>
 * <p>
 * When using a scripting language, such as groovy, you can make the script slightly smaller, by passing a closure to
 * {@link RunnableTask}, rather than sub-classing Task. Both choices have their pros and cons.
 * </p>
 * Consider reading about {@link Parameter}, and all of its sub-classes.
 * You should then look at {@link FileLister}, and {@link Exec}, because they are really handy for
 * many scripting tasks.
 */
public abstract class Task implements Runnable
{
    /**
     * The exit status when the task completes without error
     * 
     * @priority 4
     */
    public static final int EXIT_OK = 0;

    /**
     * The exit status when the task is still running, or has not even started.
     * 
     * @priority 4
     */
    public static final int EXIT_RUNNING = Integer.MIN_VALUE;

    /**
     * The exit status when the task throws an exception
     * 
     * @priority 4
     */
    public static final int EXIT_CANCELLED = 254;
    /**
     * The exit status when the task does not run due to missing/invalid parameter
     * 
     * @priority 4
     */
    public static final int EXIT_BAD_PARAMETERS = 253;

    /**
     * The exit status when the task throws an exception
     * 
     * @priority 4
     */
    public static final int EXIT_TASK_FAILED = 252;

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
     * By default, all output sent to debug is thrown away, but if the --debug parameter is set, then
     * debug becomes the same as System.out.
     * So, pepper your code with <code>debug.println(...)</code> statements, and the results
     * will only be seen when --debug is set.
     */
    public PrintStream debug = NullOutputStream.nullPrintStream;

    private BooleanParameter _helpParameter;
    private BooleanParameter _autoCompleteParameter;
    private BooleanParameter _debugParameter;
    private BooleanParameter _lookupDefaultsParameter;
    private BooleanParameter _promptParameter;

    /**
     * Should System.exit be allowed
     * 
     * @see #exit(int)
     */
    private boolean allowExit = true;

    /**
     * The exit status
     */
    private int exitStatus = EXIT_RUNNING;

    /**
     * Create a new Task, initially without any Parameters.
     * 
     * @priority 1
     */
    public Task()
    {
        _parameters = new LinkedList<Parameter>();
        _parametersMap = new HashMap<String, Parameter>();
        _metaParametersMap = new HashMap<String, Parameter>();

        _helpParameter = new BooleanParameter("help", false);
        _autoCompleteParameter = new BooleanParameter("autocomplete", false);
        _debugParameter = new BooleanParameter.Builder("debug").value(false).oppositeName("no-debug").parameter();
        _promptParameter = new BooleanParameter.Builder("prompt").oppositeName("no-prompt").parameter();
        _lookupDefaultsParameter = new BooleanParameter.Builder("userDefaults").value(true)
            .oppositeName("no-userDefaults")
            .parameter();

        addMetaParameters(_helpParameter, _autoCompleteParameter, _promptParameter, _debugParameter,
            _lookupDefaultsParameter);

    }

    /**
     * A simple setter: The Task's name is used as the command's name when displaying the command string in the
     * "More Details" section of the {@link TaskPrompter}. When using a groovy script, the name is automatically
     * guessed.
     * 
     * @priority 1
     * @param name
     */
    public void setName(String name)
    {
        _name = name;
    }

    /**
     * @return The name of this Task, the default implementation returns the classname with the package name removed.
     *         i.e. The {@link Example} task will return just "Example".
     * @priority 5
     */
    public String getName()
    {
        if (_name == null) {
            this.guessName();
        }
        return _name;
    }

    /**
     * A fluent version of {@link #setName(String)}.
     * 
     * @param name
     *            The new name for the Task
     * @return this
     * @priority 5
     */
    public Task name(String name)
    {
        setName(name);
        return this;
    }

    /**
     * Sets the name of the task based on the filename of the script containing this Task class.
     * Advanced scenarios may need to explicitly use {@link #setName(String)} or {@link #guessName(Object)}.
     * 
     * @priority 5
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
     * 
     * @priority 5
     */
    public void guessName(Object obj)
    {
        URL url = obj.getClass().getProtectionDomain().getCodeSource().getLocation();
        File file = new File(url.getPath());
        String name = file.getName();

        // A jar file, or a directory containing .class files
        if (name.endsWith(".jar") || file.isDirectory()) {
            setName(obj.getClass().getSimpleName());
        } else {
            setName(file.getName());
        }
    }

    /**
     * A simple getter
     * 
     * @return The description of the Task, as shown when using the <code>--help</code> command line argument.
     * @priority 3
     */
    public String getDescription()
    {
        return _description;
    }

    /**
     * A simple setter.
     * 
     * @param value
     *            The description of the Task shown when using the <code>--help</code> command line argument.
     * @priority 1
     */
    public void setDescription(String value)
    {
        _description = value;
    }

    /**
     * The Task's parameters are grouped together using a {@link GroupParameter}. It is a hierarchical structure,
     * as a GroupPramater may contain other GroupParameters.
     * 
     * @return The top-level GroupParameter.
     * @priority 5
     */
    GroupParameter getRootParameter()
    {
        return _root;
    }

    /**
     * A fluent API for {@link #addParameters(Parameter...)}.
     * 
     * @param parameters
     *            The parameters for this Task.
     * @return this
     * @priority 1
     */
    public final Task parameters(Parameter... parameters)
    {
        addParameters(parameters);
        return this;
    }

    /**
     * Adds a list of Parameters. Usually called from the Task's constructor.
     * 
     * @param parameters
     *            The parameters for this task.
     * @priority 1
     */
    public final void addParameters(Parameter... parameters)
    {
        for (Parameter parameter : parameters) {
            addParameter(parameter);
        }
    }

    /**
     * Adds a single parameter. Usually called from the Task's constructor.
     * 
     * @param parameter
     */
    public final void addParameter(Parameter parameter)
    {
        _root.addParameter(parameter);
        addParameterToCollections(parameter);
    }

    /**
     * A fluent API for {@link #addParameter(Parameter)}.
     * 
     * @param parameter
     * @return this
     */
    public final Task parameter(Parameter parameter)
    {
        addParameter(parameter);
        return this;
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

    /**
     * Every command can have user defined default values. Each command stores these values in a separate file.
     * On Linux, this is <code>~/.local/jguifier/TASK-NAME.defaults</code>, where TASK-NAME is the name returned by
     * {@link #getName()}.
     * 
     * @return The defaults file.
     * @see #saveDefaults()
     * @priority 4
     */
    public File getDefaultsFile()
    {
        return Util.createFile(
            new File(System.getProperty("user.home")), ".local", "jguifier", getName() + ".defaults");
    }

    /**
     * Ignores the user defaults file. A fluent API, which returns this.
     * 
     * @see #readDefaults()
     * @return this
     * @priority 3
     */
    public Task ignoreUserDefaults()
    {
        _lookupDefaultsParameter.setValue(false);
        return this;
    }

    /**
     * Reads the user defined default values. This is done automatically unless the command line includes the argument
     * <code>--no-userDefaults</code>, or if you call {@link #ignoreUserDefaults()}.
     * 
     * @see #readDefaults(File)
     * @see #ignoreUserDefaults()
     * @priority 4
     */
    public void readDefaults()
    {
        File defaultsFile = getDefaultsFile();
        debug.println("Looking up parameter defaults in : " + defaultsFile);

        readDefaults(defaultsFile);
    }

    /**
     * Reads default values from the specified file, and sets the value for each of the Task's Parameters.
     * 
     * @param file
     * @priority 4
     */
    public void readDefaults(File file)
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
    }

    /**
     * Saves the current parameter values to the user defaults file, in the standard location.
     * 
     * @throws IOException
     * @see {@link #getDefaultsFile()}
     */
    public void saveDefaults()
        throws IOException
    {
        saveDefaults(getDefaultsFile());
    }

    /**
     * Saves the current parameter values to the file specified, clearing it's contents.
     * Each parameter is on a separate line, in the form :
     * 
     * <code><pre>
     * NAME=VALUE
     * </pre></code>
     * 
     * @param file
     * @throws IOException
     * @priority 4
     */
    public void saveDefaults(File file)
        throws IOException
    {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
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
        for (Parameter parameter : _parameters) {
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

    /**
     * @return The command line string for this command, including all of the parameter arguments.
     * @priority 4
     */
    public String getCommandString()
    {
        return getName() + _root.getCommandString();
    }

    /**
     * Finds a Parameter by its name. Looks in the Task's regular parameters, and also the meta-parameters, such as
     * "prompt", "debug" etc, which are common to all Tasks.
     * 
     * @param name
     * @return The parameter with the given name, or null if no Parameter has that name.
     * @priority 3
     */
    public Parameter findParameter(String name)
    {
        Parameter result = _parametersMap.get(name);
        if (result != null) {
            return result;
        }
        return _metaParametersMap.get(name);
    }

    /**
     *
     * @priority 4
     */
    public GroupParameter getParameters()
    {
        return _root;
    }

    /**
     * Prints a help message to stdout. The message will give the name of the task, a summary of each of its parameters,
     * and details of the meta-parameters common to all Tasks, such as "debug", and "prompt".
     * 
     * @priority 4
     */
    public void help()
    {
        String description = getDescription();

        System.out.println();

        if (!Util.empty(description)) {
            System.out.println(description);
            System.out.println();
        }

        System.out.println(getName());
        System.out.println();

        for (Parameter parameter : _parameters) {
            if (parameter.visible) {
                System.out.println("    " + parameter.getHelp());
            }
        }
        System.out.println();

        System.out.println("jguifier options :");
        System.out.println("    --help              : Displays this text");
        System.out.println("    --prompt            : Force the GUI Task Prompter to appear");
        System.out.println("    --no-prompt         : Runs the command without showing the GUI Task Prompter");
        System.out.println("    --userDefaults      : Looks up user defined default values");
        System.out.println("    --no-userDefaults   : Ignores user defined default values");
        System.out.println("    --debug             : Turn on debugging");
        System.out.println();

    }

    public boolean parseArgs(String[] argv, boolean metaOnly)
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
                        if (!metaOnly || _metaParametersMap.containsKey(name)) {
                            ((ValueParameter<?>) parameter).setStringValue(value);
                        }
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

                        if (!metaOnly || _metaParametersMap.containsKey(name)) {

                            // Don't allow boolean parameters in the form --name value
                            // Form --name=value has already been dealt with, so this must be form --name
                            // where the value is true by default.
                            booleanParameter.setValue(true);
                            // See BooleanParameter.setOppositeName for details
                            if (name.equals(booleanParameter.getOppositeName())) {
                                booleanParameter.setValue(!booleanParameter.getValue());
                            }
                        }

                    } else {
                        // Parameter in the form --name value

                        if (i + 1 >= argv.length) {
                            throw new ParameterException(parameter, "Value not given");
                        }
                        value = argv[i + 1];
                        i++;
                        if (parameter instanceof ValueParameter<?>) {
                            if (!metaOnly || _metaParametersMap.containsKey(name)) {
                                ((ValueParameter<?>) parameter).setStringValue(value);
                            }
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
     * @priority 5
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
            for (Parameter parameter : _parameters) {
                autocompleteFilter("--" + parameter.getName(), cur);
            }
        }

    }

    /**
     * Part of the Linux command line auto-completion mechanism.
     * Outputs <code>value</code> to stdout if it begins with <code>prefix</code>.
     * 
     * @param value
     *            The value to be considered for auto-completion
     * @param prefix
     *            The part of the argument when the tab key was pressed.
     * @priority 5
     */
    public static void autocompleteFilter(String value, String prefix)
    {
        if (value.startsWith(prefix)) {
            System.out.println(value);
        }
    }

    /**
     * Prevent {@link System#exit(int)} being called. Use this if you create tasks which are NOT command line tasks,
     * i.e., the task is part of a larger application.
     * 
     * @return this
     * @priority 3
     */
    public Task neverExit()
    {
        allowExit = false;
        return this;
    }

    /**
     * Has the Task finished running?
     */
    public boolean isFinished()
    {
        return this.exitStatus != EXIT_RUNNING;
    }

    /**
     * Either exits the JVM using {@link System#exit(int)}, or throws a {@link ExitException}.
     * 
     * @param exitStatus
     *            Use positive numbers, and avoid anything over 200, because negative numbers are used by jguifier uses
     *            these, such as {@link #EXIT_CANCELLED}.
     *            Note, under Linux exit status appear to be limited to 0..255, however the javadocs for System.exit
     *            has nothing to say on the allowed values.
     * @see #neverExit()
     * @priority 2
     */
    void exit(int exitStatus)
    {
        this.exitStatus = exitStatus;
        if (allowExit) {
            System.exit(exitStatus);
        } else {
            throw new ExitException(exitStatus);
        }
    }

    /**
     * 
     * @return The exit status or {@link #EXIT_RUNNING} if the Task has not finished.
     * @priority 3
     */
    public int getExitStatus()
    {
        return exitStatus;
    }

    public boolean checkParameters()
    {
        try {
            // Check that if all the parameters are present and correct.
            for (Parameter parameter : _parameters) {
                parameter.check();
            }
            check();

        } catch (ParameterException e) {
            return false;
        }

        return true;
    }

    /**
     * Parses the command line arguments, looks up any user defined values for parameters.
     * Then either prompt the command (using {@link TaskPrompter}, or runs the command.
     * <p>
     * <b style="color:red">WARNING</b>. This method is designed to be run from a command line script and contains
     * {@link System#exit(int)} calls. This will terminate the whole JVM. To prevent this, call {@link #neverExit()}
     * before calling the {@link #go(String[])} method.
     * </p>
     * 
     * @param argv
     *            The command line arguments
     * @priority 1
     */
    public void go(String[] argv)
    {
        try {
            // Parse all parameters, but only record the meta-parameters, such as debug, help, etc
            if (!parseArgs(argv, true)) {
                return;
            }

            // If --no-userDefaults, then the user defined defaults won't be read.
            if (_lookupDefaultsParameter.getValue()) {
                readDefaults();
            }

            // Parse all the parameters, recording all their values.
            if (!parseArgs(argv, false)) {
                return;
            }

            debug.println("Parameters : ");
            for (Parameter parameter : _parameters) {
                debug.println(parameter);
            }

            try {
                // Check that if all the parameters are present and correct.
                for (Parameter parameter : _parameters) {
                    parameter.check();
                }
                check();

            } catch (ParameterException e) {
                // If a parameter is missing or invalid, then either end the program, or prompt the command
                if (_promptParameter.getValue() == Boolean.FALSE) {
                    System.err.println(e);
                    exit(EXIT_BAD_PARAMETERS);
                }
                promptTask();
                return;
            }

            // Either prompt the command, or run it.
            if (_promptParameter.getValue() == Boolean.TRUE) {
                promptTask();
            } else {
                run();
            }

        } catch (Exception e) {
            e.printStackTrace();
            exit(EXIT_TASK_FAILED);
        }
    }

    /**
     * Opens the {@link TaskPrompter}, allowing the user to fill in the parameters, and then run the Task.
     * 
     * @priority 3
     */
    public void promptTask()
    {
        TaskPrompter taskPrompter = new TaskPrompter(this);
        taskPrompter.prompt();
    }

    /**
     * If your task needs to compare one parameter with another to ensure that the
     * parameters are valid, this is where you should do it.
     * Parameter.check should only check its own value, not the value of other parameters.
     * 
     * @priority 3
     */
    public void check() throws ParameterException
    {
        // Default does nothing
    }

    /**
     * This is where the Task's processing happens. Override this method.
     * 
     * @priority 1
     */
    public void run()
    {
        try {
            pre();
            body();
        } finally {
            post();
        }
    }

    public abstract void body();

    /**
     * Actions performed before {@link #body()}
     */
    public void pre()
    {
        // Default implementation does nothing
    }

    /**
     * Actions performed after {@link #body()} - called in a finally block. Useful to close resources
     * regardless of whether the body completed succeffuly or not.
     */
    public void post()
    {
        // Default implementation does nothing
    }

}
