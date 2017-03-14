package uk.co.nickthecoder.jguifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingUtilities;

import uk.co.nickthecoder.jguifier.parameter.BooleanParameter;
import uk.co.nickthecoder.jguifier.parameter.GroupParameter;
import uk.co.nickthecoder.jguifier.parameter.Parameter;
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
public abstract class Task implements Runnable, Cloneable
{
    /**
     * The exit status when the task is still running, or has not even started.
     * 
     * @priority 4
     */
    public static final int EXIT_RUNNING = Integer.MIN_VALUE;

    private String _name = null;

    private String _description = "";

    /**
     * The list of all parameters is the order which they were added.
     */
    protected List<Parameter> _parameters;

    /**
     * The hierarchical list of parameters (groups can have sub-groups).
     */
    protected GroupParameter _root = new GroupParameter("__ROOT");

    /**
     * A map of the parameters keyed on their name.
     */
    protected HashMap<String, Parameter> _parametersMap;

    /**
     * By default, all output sent to debug is thrown away, but if the --debug parameter is set, then
     * debug becomes the same as System.out.
     * So, pepper your code with <code>debug.println(...)</code> statements, and the results
     * will only be seen when --debug is set.
     */
    public PrintStream debug = NullOutputStream.nullPrintStream;

    /**
     * The exit status
     */
    protected int exitStatus = EXIT_RUNNING;

    protected List<TaskListener> listeners = new ArrayList<TaskListener>();

    /**
     * Create a new Task, initially without any Parameters.
     * 
     * @priority 1
     */
    public Task()
    {
        _parameters = new ArrayList<Parameter>();
        _parametersMap = new HashMap<String, Parameter>();
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
            new File(System.getProperty("user.home")), ".config", "jguifier", getName() + ".defaults");
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
        return getCommandString(true);
    }

    /**
     * @return The command line string for this command, including all of the parameter arguments.
     * @priority 4
     */

    public String getCommandString(boolean includeHidden)
    {
        return getName() + _root.getCommandString(includeHidden);
    }

    public Parameter findParameter(String name)
    {
        return _parametersMap.get(name);
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

    /**
     * Has the Task finished running?
     */
    public boolean isFinished()
    {
        return this.exitStatus != EXIT_RUNNING;
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
     * Opens the {@link TaskPrompter}, allowing the user to fill in the parameters, and then run the Task.
     * 
     * @priority 3
     */
    public void promptTask()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                TaskPrompter taskPrompter = new TaskPrompter(Task.this);
                taskPrompter.prompt();
            }
        });
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
     * This is where the Task's processing happens. Calls {@link #pre(), {@link #body()} and the {@link #post()}.
     * {@link TaskListener}s will be notified at completion by {@link TaskListener#ended(Task, boolean)}.
     * 
     * @priority 1
     */
    public void run()
    {
        try {
            pre();
            body();
        } finally {
            try {
                post();
                fireEnded(true);
            } catch (Exception e) {
                fireEnded(false);
                throw e;
            }
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
     * regardless of whether the body completed successfully or not.
     */
    public void post()
    {
        // Default implementation does nothing
    }

    public void fireEnded(boolean normally)
    {
        for (TaskListener listener : listeners) {
            listener.ended(this, normally);
        }
    }

    public void fireAborted()
    {
        for (TaskListener listener : listeners) {
            listener.aborted(this);
        }
    }

    public void addTaskListener(TaskListener listener)
    {
        listeners.add(listener);
    }

    public void removeTaskListener(TaskListener listener)
    {
        listeners.remove(listener);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Task copy()
    {
        try {
            Class<?> klass = this.getClass();
            Constructor<?> init = klass.getConstructor(new Class<?>[] {});

            Task result = (Task) init.newInstance();
            for (Parameter parameter : _parameters) {
                if (parameter instanceof ValueParameter) {
                    ValueParameter vp = (ValueParameter) parameter;
                    ((ValueParameter) result.findParameter(vp.getName())).setValue(vp.getValue());
                }
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Task clone()
    {
        Task result = copy();
        return result;
    }
}
