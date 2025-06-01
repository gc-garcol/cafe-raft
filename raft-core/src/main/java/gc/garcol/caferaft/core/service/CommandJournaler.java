package gc.garcol.caferaft.core.service;

import gc.garcol.caferaft.core.client.Command;
import gc.garcol.caferaft.core.log.LogEntry;

/**
 * @author thaivc
 * @since 2025
 */
public interface CommandJournaler {
    LogEntry acceptCommand(long currentTerm, Command command);
}
