package uk.co.nickthecoder.jguifier.util;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import uk.co.nickthecoder.jguifier.BooleanParameter;
import uk.co.nickthecoder.jguifier.ChoiceParameter;
import uk.co.nickthecoder.jguifier.EnumParameter;
import uk.co.nickthecoder.jguifier.FileParameter;
import uk.co.nickthecoder.jguifier.IntegerParameter;
import uk.co.nickthecoder.jguifier.StringParameter;
import uk.co.nickthecoder.jguifier.Task;

public class FileListerTask extends Task
{
    public List<File> results = null;

    public FileParameter directory = new FileParameter.Builder("directory").directory().mustExist()
        .description("Starting directory")
        .parameter();

    public BooleanParameter includeFiles = new BooleanParameter.Builder("includeFiles").value(true)
        .description("Include files in the results")
        .parameter();

    public BooleanParameter includeDirectories = new BooleanParameter.Builder("includeDirectories").value(false)
        .description("Include directories in the results")
        .parameter();

    public BooleanParameter includeHidden = new BooleanParameter.Builder("includeHidden").value(false)
        .description("Include hidden files/directories in the results")
        .parameter();

    public BooleanParameter enterHidden = new BooleanParameter.Builder("enterHidden").value(false)
        .description("Enter hidden directories?")
        .parameter();

    public IntegerParameter depth = new IntegerParameter.Builder("depth").value(1)
        .description("How deep down the tree; 1 = a single directlry")
        .parameter();

    public StringParameter fileExtensions = new StringParameter.Builder("fileExtensions")
        .description("List only files with matching file extensions (comma separated, without the dot)")
        .optional().parameter();

    public ChoiceParameter<Comparator<File>> order = new ChoiceParameter.Builder<Comparator<File>>("order")
        .description("Ordering")
        .choice("name", FileLister.NAME_ORDER, "By Filename")
        .choice("path", FileLister.PATH_ORDER, "By Pathname")
        .choice("lastModified", FileLister.LAST_MODIFIED_ORDER, "By Last Modified Date")
        .choice("size", FileLister.SIZE_ORDER, "By File Size")
        .value(FileLister.NAME_ORDER)
        .parameter();

    public BooleanParameter reverse = new BooleanParameter.Builder("reverse").value(false)
        .description("Reverse order")
        .parameter();

    public EnumParameter<FileLister.Sort> sort = new EnumParameter.Builder<FileLister.Sort>(FileLister.Sort.class, "sort")
        .description("How to sort a recursive tree")
        .value(FileLister.Sort.ALL)
        .parameter();

    public BooleanParameter absolute = new BooleanParameter.Builder("absolute")
        .description("Return absolute filenames?")
        .value(false).parameter();

    public BooleanParameter canonical = new BooleanParameter.Builder("canonical")
        .description("Return canonical filenames?")
        .value(false).parameter();

    public FileListerTask()
    {
        addParameters(directory,
            includeFiles, includeDirectories, includeHidden, enterHidden, depth, fileExtensions,
            order, reverse, sort, absolute, canonical);
    }

    public void body()
    {
        FileLister fileLister = createFileLister();

        results = fileLister.listFiles(directory.getValue());
    }

    public FileLister createFileLister()
    {
        FileLister lister = new FileLister();
        
        lister.setIncludeFiles(includeFiles.getValue());
        lister.setIncludeDirectories(includeDirectories.getValue());
        lister.setIncludeHidden(includeHidden.getValue());
        lister.setEnterHidden(enterHidden.getValue());

        lister.setDepth(depth.getValue());

        if (! Util.empty(fileExtensions.getValue())) {
            lister.setFileExtensions(fileExtensions.getValue().split(","));
        }

        if (reverse.getValue()) {
            lister.order(new ReverseComparator<File>(order.getValue()));
        } else {
            lister.order(order.getValue());
        }
        lister.setSort(sort.getValue());

        lister.setAbsolute(absolute.getValue());
        lister.setCanonical(canonical.getValue());

        return lister;
    }
}
