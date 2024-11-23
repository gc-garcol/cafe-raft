package gc.garcol.raftcore.core.state;

import gc.garcol.raftcore.core.attribute.Attribute;

import java.util.List;

/**
 * @author thaivc
 * @since 2025
 */
public class Leader implements RaftState
{
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Attribute> attributes()
    {
        return List.of();
    }
}
