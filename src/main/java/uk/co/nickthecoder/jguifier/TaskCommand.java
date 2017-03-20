package uk.co.nickthecoder.jguifier;

import java.util.HashMap;

import uk.co.nickthecoder.jguifier.parameter.BooleanParameter;
import uk.co.nickthecoder.jguifier.parameter.MultipleParameter;
import uk.co.nickthecoder.jguifier.parameter.Parameter;

public class TaskCommand implements TaskListener
{
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

    public Task task;

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
     * Parameters used by all Tasks, such as "--prompt", "--help", "--description", "--taskName" etc.
     */
    private HashMap<String, Parameter> _metaParametersMap;

    public TaskCommand(Task task)
    {
        this.task = task;
        this.task.fromCommandLine = true;

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

        task.addTaskListener(this);
    }

    private void addMetaParameters(Parameter... parameters)
    {
        for (Parameter parameter : parameters) {
            _metaParametersMap.put(parameter.getName(), parameter);

            if (parameter instanceof BooleanParameter) {
                BooleanParameter bp = (BooleanParameter) parameter;
                if (bp.getOppositeName() != null) {
                    assert !this._metaParametersMap
                        .containsKey(bp.getOppositeName()) : "Duplicate parameter opposite name";
                    this._metaParametersMap.put(bp.getOppositeName(), parameter);
                }
            }

        }
    }

    /**
     * Prevent {@link System#exit(int)} being called. Use this if you create tasks which are NOT command line tasks,
     * i.e., the task is part of a larger application.
     * 
     * @return this
     * @priority 3
     */
    public TaskCommand neverExit()
    {
        allowExit = false;
        return this;
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
        Parameter result = task.findParameter(name);

        if (result != null) {
            return result;
        }
        return _metaParametersMap.get(name);
    }

    /**
     * Ignores the user defaults file. A fluent API, which returns this.
     * 
     * @see #readDefaults()
     * @return this
     * @priority 3
     */
    public TaskCommand ignoreUserDefaults()
    {
        _lookupDefaultsParameter.setValue(false);
        return this;
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
     * @param args
     *            The command line arguments
     * @priority 1
     */
    public void go(String[] args)
    {
        try {
            // Parse all parameters, but only record the meta-parameters, such as debug, help, etc
            if (!parseArgs(args, true)) {
                return;
            }

            // If --no-userDefaults, then the user defined defaults won't be read.
            if (_lookupDefaultsParameter.getValue()) {
                task.readDefaults();
            }

            // Parse all the parameters, recording all their values.
            if (!parseArgs(args, false)) {
                return;
            }

            task.debug.println("Parameters : ");
            for (Parameter parameter : task._parameters) {
                task.debug.println(parameter);
            }

            try {
                // Check that if all the parameters are present and correct.
                for (Parameter parameter : task._parameters) {
                    parameter.check();
                }
                task.check();

            } catch (ParameterException e) {
                // If a parameter is missing or invalid, then either end the program, or prompt the command
                if (_promptParameter.getValue() == Boolean.FALSE) {
                    System.err.println(e);
                    exit(EXIT_BAD_PARAMETERS);
                }
                prompt();
                return;
            }

            // Either prompt the command, or run it.
            if (_promptParameter.getValue() == Boolean.TRUE) {
                prompt();
            } else {
                task.run();
            }

        } catch (Exception e) {
            e.printStackTrace();
            exit(EXIT_TASK_FAILED);
        }
    }

    public void prompt(boolean showDetails)
    {
        TaskPrompter prompter = new TaskPrompter(task);
        prompter.prompt(true);
    }

    public void prompt()
    {
        TaskPrompter prompter = new TaskPrompter(task);
        prompter.prompt(true);
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

                            if (parameter instanceof MultipleParameter) {

                                ((MultipleParameter<?, ?>) parameter).setSingleStringValue(value);

                            } else {

                                ((ValueParameter<?>) parameter).setStringValue(value);
                            }
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
            task.debug = System.out;
        }

        if (_helpParameter.getValue()) {
            task.help();
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
            task.debug.println("Params[" + (i - 3) + "] = " + argv[i]);
        }

        task.debug.println("Index = " + index);

        String cur = index >= params.length ? "" : params[index];
        String prev = (index > 0) ? params[index - 1] : "";

        task.debug.println("Autocomplete arguments");
        for (String arg : argv) {
            task.debug.println("   '" + arg + "'");
        }
        task.debug.println("Current : " + cur);
        task.debug.println("Prev : " + prev);
        task.debug.println("");

        /*
         * If the user has edited a command, deleting a parameter value, then cur will be the
         * next parameter's name. We don't want that, we want a blank.
         * If example if the command currently reads :
         * mycommand --foo --bar barValue
         * and the cursor is after --foo, we want to prompt for parameter foo's choices, and ignore "--bar".
         */
        if ((index != params.length) && (prev.startsWith("--")) && (cur.startsWith("--"))) {
            cur = "";
            task.debug.println("Setting cur to  blank");
        }

        if (prev.startsWith("--")) {
            String name = prev.substring(2);
            Parameter parameter = findParameter(name);
            if (parameter != null) {
                parameter.autocomplete(cur);
            }
        } else {
            for (Parameter parameter : task._parameters) {
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

    @Override
    public void ended(Task task, boolean normally)
    {
    }

    @Override
    public void aborted(Task task)
    {
        exit(EXIT_CANCELLED);
    }

    public void exit(int status)
    {
        if (allowExit) {
            System.exit(status);
        }
    }
}
