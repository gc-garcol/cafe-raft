package gc.garcol.raftcore.domain.common;

/**
 * @author thaivc
 * @since 2024
 */
public class InternalException extends RuntimeException
{
    public InternalException(String message)
    {
        super(message);
    }

    public InternalException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public InternalException(Throwable cause)
    {
        super(cause);
    }
}
