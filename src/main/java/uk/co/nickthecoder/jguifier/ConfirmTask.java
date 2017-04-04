package uk.co.nickthecoder.jguifier;

/**
 * A simple Task designed to be used as a confirmation dialog within a GUI application.
 */
public class ConfirmTask extends Task
{
    private Runnable runnable;

    private String title;

    public ConfirmTask(Runnable runnable)
    {
        this.runnable = runnable;
    }

    @Override
    public void body() throws Exception
    {
        this.runnable.run();
    }

    @Override
    public String getTitle()
    {
        return title == null ? super.getTitle() : title;
    }

    public ConfirmTask title(String title)
    {
        this.title = title;
        return this;
    }

    public ConfirmTask description(String desc)
    {
        setDescription(desc);
        return this;
    }
}
