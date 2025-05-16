package gc.garcol.caferaft.core.service;

import gc.garcol.caferaft.core.client.ClientResponse;
import gc.garcol.caferaft.core.client.Command;
import gc.garcol.caferaft.core.client.CommandResponse;
import gc.garcol.caferaft.core.client.Query;

/**
 * @author thaivc
 * @since 2025
 */
public interface StateMachine {
    void load();

    void snapshot();

    CommandResponse accept(Command command);

    ClientResponse apply(Query query);
}
