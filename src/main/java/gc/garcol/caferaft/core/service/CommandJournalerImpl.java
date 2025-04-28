package gc.garcol.caferaft.core.service;

import gc.garcol.caferaft.core.client.Command;
import gc.garcol.caferaft.core.log.LogEntry;
import gc.garcol.caferaft.core.log.LogManager;
import lombok.RequiredArgsConstructor;

/**
 * @author thaivc
 * @since 2025
 */
@RequiredArgsConstructor
public class CommandJournalerImpl implements CommandJournaler {

    private final LogManager logManager;

    @Override
    public LogEntry acceptCommand(long currentTerm, Command command) {
        return logManager.appendLog(currentTerm, command);
    }
}
