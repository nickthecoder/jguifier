package uk.co.nickthecoder.jguifier;

import uk.co.nickthecoder.jguifier.parameter.Parameter;

/**
 * 
 * @priority 4
 */
public class ParameterException
    extends TaskException
{
    private static final long serialVersionUID = 1;

    public static final String REQUIRED_MESSAGE = "Required";

    private Parameter _parameter;

    public ParameterException(Parameter parameter, String message)
    {
        super(message);
        this._parameter = parameter;
    }

    public Parameter getParameter()
    {
        return _parameter;
    }

    @Override
    public String toString()
    {
        return "Parameter " + _parameter.getName() + " (" + _parameter.getLabel() + ") : " + getMessage();
    }
}
