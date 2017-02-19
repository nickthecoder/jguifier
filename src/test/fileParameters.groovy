#!/usr/bin/env jguifier

import uk.co.nickthecoder.jguifier.*
import uk.co.nickthecoder.jguifier.util.*

def mustExist = new FileParameter("mustExist").exists(true)
def mustNotExist = new FileParameter("mustNotExist").exists(false)
def mayExist = new FileParameter("mayExist")

def directory = new FileParameter("directory").directory()
def file = new FileParameter("file").directory(false)
def file2 = new FileParameter("file2") // Should be the default
def either = new FileParameter("either").directory(null)

def optional = new FileParameter("optional").optional()
def writable = new FileParameter("writable").writable(true)

new RunnableTask()
.parameters( mustExist, mustNotExist, mayExist,  directory, file, file2, either,  optional, writable )
.action {
    println( "Done." )
}
.go(args)


