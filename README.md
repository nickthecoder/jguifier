Creates a GUI front end for your own tools.

I create many simple tools to help me, and most of them I run from a command line, or from the file manager.

One down side of command line tools, is the steep learning curve. An new, or infrequently used command can be tricky
to use, because the names of the command line switches aren't known.

jguifier bridges the gap between command line tools and GUIs. Define each parameter that your command expects,
and then you can run the command using a GUI or via the command line. I think it gives the best of both worlds.

From the example command (uk.co.nickthecoder.jguifier.Example) :

    private ChoiceParameter<String> _greeting = new ChoiceParameter<String>(
    "greeting", "Greeting").choices(new String[] { "Hello", "Hi", "Watcha" });


Auto Complete
=============

To enable tab-completion, add the following to ~/.bash_completion :

    _JGuifierComplete ()
    {
      COMPREPLY=( $(${COMP_WORDS[0]} --autocomplete "${COMP_CWORD}" "${COMP_WORDS[@]}" ) )
    
      return 0
    }

    JGUIFIER_SCRIPTS=`cd ~/bin;echo *.bsh *.groovy`

    complete -F _JGuifierComplete -o filenames ${JGUIFIER_SCRIPTS}


Compile
=======

gradle build

Run
===

java -classpath build/libs/jguifier-0.1.jar uk.co.nickthecoder.jguifier.Example

