package gc.garcol.raftcore.core.state;

import gc.garcol.raftcore.core.attribute.Attribute;

import java.util.List;

/**
 * @author thaivc
 * @since 2025
 */
public interface RaftState
{
    /**
     * Components
     *
     * @return attributes
     */
    List<Attribute> attributes();
}
