#!/usr/bin/env jguifier

import uk.co.nickthecoder.jguifier.*
import uk.co.nickthecoder.jguifier.util.*

def mustExist = new FileParameter.Builder("mustExist").mustExist().parameter()
def mustNotExist = new FileParameter.Builder("mustNotExist").mustNotExist().parameter()
def mayExist = new FileParameter.Builder("mayExist").mayExist().parameter();

def directory = new FileParameter.Builder("directory").directory().parameter()
def file = new FileParameter.Builder("file").file().parameter()
def file2 = new FileParameter.Builder("file2").parameter() // Should be the default
def either = new FileParameter.Builder("either").fileOrDirectory().parameter()

def optional = new FileParameter.Builder("optional").optional().parameter()
def writable = new FileParameter.Builder("writable").writable(true).parameter()

new RunnableTask()
.parameters( mustExist, mustNotExist, mayExist,  directory, file, file2, either,  optional, writable )
.action {
    println( "Done." )
}
.go(args)


