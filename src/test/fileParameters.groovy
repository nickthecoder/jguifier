#!/usr/bin/env jguifier

import uk.co.nickthecoder.jguifier.*
import uk.co.nickthecoder.jguifier.util.*

public class FileParameters extends Task
{

    FileParameter mustExist = new FileParameter("mustExist").exists(true)
    FileParameter mustNotExist = new FileParameter("mustNotExist").exists(false)
    FileParameter mayExist = new FileParameter("mayExist")

    FileParameter directory = new FileParameter("directory").directory()
    FileParameter file = new FileParameter("file").directory(false)
    FileParameter file2 = new FileParameter("file2") // Should be the default
    FileParameter either = new FileParameter("either").directory(null)

    FileParameter optional = new FileParameter("optional").optional()
    FileParameter writable = new FileParameter("writable").writable(true)

    FileParameters()
    {
        super()
        addParameters( mustExist, mustNotExist, mayExist,  directory, file, file2, either,  optional, writable )
    }

    @Override
    void run() throws Exception
    {
    }

    static void main(String[] argv)
    {
        def task = new FileParameters();
        task.setName("fileParameters.groovy");
        task.runFromMain(argv);
    }
}

