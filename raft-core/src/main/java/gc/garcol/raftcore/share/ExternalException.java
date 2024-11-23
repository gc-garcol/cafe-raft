package gc.garcol.raftcore.share;

/**
 * @author thaivc
 * @since 2024
 */
public class ExternalException extends RuntimeException
{
    public ExternalException(String message)
    {
        super(message);
    }

    public ExternalException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ExternalException(Throwable cause)
    {
        super(cause);
    }
}
