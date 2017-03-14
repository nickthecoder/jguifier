package uk.co.nickthecoder.jguifier;

public interface TaskListener
{
    public void ended(Task task, boolean normally);

    public void aborted(Task task);
}
