Creates a GUI front end for your own tools, as well as making command line tools easy to parse arguments.

I create many simple tools, and if I didn't have something like jguifier, then they would probably be
low quility shell scripts. Each script having to parse their arguments manually.
Each with their own set of unique bugs and quirks. (e.g. should parameters be like  --name=foo or --name foo)

JGuifier solves these problems, as well as making the commands easier to use, by automatically including a GUI verion
as well as tab completion using the linux command line.

The commands will be self documenting, as a "--help" argument will automatically list the expected parameters,
including an optional short description for each.

I've only used jguifier under linux, however, I don't see why it wouldn't work under windows or MacOS.
Windows doesn't have the super-duper tab completion that linux does.
Also, windows needs to know in advance if your program is a GUI or TUI (text).
JGuifier is designed to be both, so that may cause you some grief (having to know when to use java and when
to use javaw). BTW, it's now 2017, is windows still this stupid?

[Javadocs](http://nickthecoder.co.uk/public/jguifier/docs/javadoc/)

How It Works
============

Rather than parse the command line arguments in each script, your scripts *define* the arguments, including an id,
a human readable label, their type, and optionally a short description.

For example, a simple integer with possible values between 1 and 10 is defined like so :

    countParameter = new IntegerParameter("count", "Count").range(1, 10);

A simple string

    myString = new StringParameter( "myString", "My String" );

    myFile = new FileParameter( "myFile", "My File" ).exists(true).isDirectory(false);

Extend the Task class, adding a suitable constructor and define the "run" method :

    public class MyTask extends Task
    {
        // Define each parameter as a class attribute here (i.e. countParameter, myString and myFile).
        
        public MyTask()
        {
            super( "My Task" );
            addParameters( countParameter, myString, myFile );
        }
        
        public void run()
        {
            // Your code goes here
        }
    }

Finally instantiate your class, and call the "runFromMain" method :

    new MyTask().runFromMain( argv );


Groovy Scripts
==============

To make launching scripts as easy as possible, create a shell script to launch your tools written in groovy,
with the appropriate path. For example :

    #!/bin/sh

    groovy -cp "/path/to/the/jar/file/jguifier-0.1.jar" "$@"

Then place a "shebang" at the top of each of your ".groovy" files like so :

    #!/usr/bin/env jguifier
    
Where "jguifier" is the name of the shell script. (The shell script has to be on your path, the groovy script doesn't).

You can of course, add additional jars to the classpath to suit your needs.
Dynamically adding jars to the classpath within your script is possible, but it's a PITA.


Bean Shell Scripts
==================

Before I found Groovy, I used to use Bean Shell, but it isn't anywhere as good as Groovy, so I don't recommend it
any more. However, if you do want to give it a try, you can make a similar shim to call the bean shell, or do
what I *used* to do, and use "exec" to launch the bean shell. At the top of each ".bsh" file :

    #!/bin/sh
    //bin/true; exec java -cp /usr/share/java/bsh.jar:jguifier.jar bsh.Interpreter "$0" "$@"

Note that the "//" is a java comment, but is also a valid alternative to just "/" (the root directory).


GUI vs TUI (--prompt and --noprompt)
====================================

When you run a script, the default behaviour is to display the GUI if one or more parameters are missing, or
otherwise incorrect. If all parameters are ok, then the GUI is not displayed, and your script's run method is called.
You can change this behviour, by using the "--prompt" and "--noprompt" options.

"--nopromp" prevents the GUI from appearing even when parameters are missing (in which case stderr will show an
error message, and the script exits without doing anything more).

"--prompt" forces the GUI to appear even if the parameter are all present and correct.


Auto Complete
=============

To enable tab-completion when using the linux command line, add the following to ~/.bash_completion :

    _JGuifierComplete ()
    {
      COMPREPLY=( $(${COMP_WORDS[0]} --autocomplete "${COMP_CWORD}" "${COMP_WORDS[@]}" ) )
    
      return 0
    }

    JGUIFIER_SCRIPTS=`cd ~/bin;echo *.bsh *.groovy`

    complete -F _JGuifierComplete -o filenames ${JGUIFIER_SCRIPTS}

Ensure that ~/.bash_completion is sourced (executed) from within your ~/.bashrc.
You will need to start a new bash shell for changes to ~/.bash_completion to take effect.

Compile
=======

I use gradle to build the project, like so :

    gradle build
    gradle javadoc

Note, I don't use the installApp target, as I only need the to build the jar file.

(I then manually copy build/docs to my public website for easy viewing).

To install it into my local repository (so that other projects can use its classes - FileLister and Exec are very useful!)

    gradle install


