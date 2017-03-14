package uk.co.nickthecoder.jguifier;

import uk.co.nickthecoder.jguifier.parameter.Parameter;

public interface ParameterHolder
{
    public void setError(Parameter parameter, String message);

    public void clearError(Parameter parameter);

}
