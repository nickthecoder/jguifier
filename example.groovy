#!/usr/bin/env jguifier

import uk.co.nickthecoder.jguifier.*

class Example extends Task {
    def number = new IntegerParameter("number", "Very Long Label")
        .range(1, 10)

    def greeting = new StringChoiceParameter<String>( "greeting", "Greeting")
        .choices( "Hello", "Hi", "Watcha" )

    def message = new StringParameter("message", "Message", "Nice to meet you.")
        .stretch()

    def output = new ChoiceParameter<String, PrintStream>("output", "Output")
        .choice("stdout", System.out, "Normal")
        .choice("stderr", System.err, "Error").value(System.err)

    def file = new FileParameter( "file", "File" )

    public Example() {
        super("Example");
        addParameters( number, greeting, message, output, file)
    }

    public void run() {
        output.getValue().println( greeting.getValue() )
        output.getValue().println( message.getValue() )
        output.getValue().println( number.getValue() )
        output.getValue().println( file.getValue() )
    }
}

new Example().runFromMain( args )

