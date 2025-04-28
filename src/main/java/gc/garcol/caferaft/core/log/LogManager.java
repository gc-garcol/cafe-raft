package gc.garcol.caferaft.core.log;

import gc.garcol.caferaft.core.client.Command;
import gc.garcol.caferaft.core.repository.LogRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static gc.garcol.caferaft.core.constant.LogConstant.EXCLUSIVE_UNDER_BOUND_TERM;

/**
 * The LogManager class is responsible for managing the Raft log entries and segments.
 * It acts as a high-level manager that coordinates log operations between the Raft consensus system
 * and the underlying LogRepository implementation.
 * <p>
 * The manager maintains a TreeSet of segments and delegates actual storage operations
 * to the LogRepository. It provides methods for both leader and follower nodes to handle
 * log operations in a Raft consensus system.
 *
 * @author thaivc
 * @since 2025
 */
public class LogManager {

    /**
     * The set of all log segments, ordered by their terms.
     * Each segment contains log entries for a specific term.
     */
    private final List<Segment> segments;

    /**
     * The underlying repository that handles the actual storage of log entries and segments.
     */
    private final LogRepository logRepository;

    public LogManager(LogRepository logRepository) {
        this.logRepository = logRepository;
        segments = logRepository.allSortedSegments();
    }

    /**
     * Creates a new segment with the specified term.
     *
     * @param term The term number for the new segment
     * @return The newly created Segment
     * @throws IllegalArgumentException if term is negative
     */
    Segment newSegment(long term) {
        if (term <= EXCLUSIVE_UNDER_BOUND_TERM) {
            throw new IllegalArgumentException("Term must be positive");
        }
        var segment = logRepository.newSegment(term);
        segments.add(segment);
        return segment;
    }

    public long segmentSize(long term) {
        return logRepository.segmentSize(term);
    }

    public Segment lastSegment() {
        return segments.isEmpty() ? null : segments.getLast();
    }

    public Position nextPosition(Position position) {
        long segmentSize = segmentSize(position.term());
        if (position.index() < segmentSize - 1) {
            return new Position(
                position.term(),
                position.index() + 1
            );
        }

        // if the position is last log of segment, then move on to the start of the next segment
        int currentSegmentIndex = Collections.binarySearch(segments, Segment.of(position.term()));
        int nextSegmentIndex = currentSegmentIndex + 1;
        if (nextSegmentIndex == segments.size()) {
            throw new IllegalArgumentException("No next segment found");
        }

        var nextSegment = segments.get(nextSegmentIndex);
        return new Position(
            nextSegment.getTerm(),
            0
        );
    }

    /**
     * Truncates a specific segment identified by its term.
     * This method is only used by follower nodes.
     *
     * @param term The term of the segment to truncate
     * @return true if the truncation was successful, false otherwise
     */
    public boolean truncateSegment(long term) {
        var success = logRepository.truncateSegment(term);
        if (success) {
            var index = Collections.binarySearch(segments, Segment.of(term));
            segments.remove(index);
        }
        return success;
    }

    /**
     * Truncates multiple segments identified by their terms.
     * This method is only used by follower nodes.
     *
     * @param terms List of terms identifying segments to truncate
     * @return List of terms that were successfully truncated
     * @throws NullPointerException if terms is null
     */
    public List<Long> truncateSegments(List<Long> terms) {
        Objects.requireNonNull(terms, "terms must not be null");
        if (terms.isEmpty()) {
            return Collections.emptyList();
        }

        var truncatedSegments = logRepository.truncateSegments(terms);
        truncatedSegments.forEach(term -> {
            var index = Collections.binarySearch(segments, Segment.of(term));
            segments.remove(index);
        });
        return truncatedSegments;
    }

    /**
     * Appends a single log entry with the given command to the current term's log.
     * This method is only used by leader nodes.
     *
     * @param term the segment
     * @param command The command to be logged
     * @return The newly created LogEntry
     */
    public LogEntry appendLog(long term, Command command) {
        return logRepository.appendLog(term, command);
    }

    /**
     * Appends multiple log entries with the given commands to the current term's log.
     * This method is only used by leader nodes.
     *
     * @param term     The term number for the log entries
     * @param commands List of commands to be logged
     * @return List of newly created LogEntries
     */
    public List<LogEntry> appendLogs(long term, List<Command> commands) {
        return logRepository.appendLogs(term, commands);
    }

    /**
     * Replicates log entries from the leader to the follower.
     * This method handles the creation of new segments if needed and appends the replicated entries.
     * This method is only used by follower nodes.
     *
     * @param logEntries List of log entries to be replicated
     * @return List of successfully replicated LogEntries
     */
    public List<LogEntry> replicateLogs(List<LogEntry> logEntries) {
        long currentTerm = -1;
        List<LogEntry> results = new ArrayList<>(logEntries.size());
        for (LogEntry logEntry : logEntries) {
            if (logEntry.getPosition().term() != currentTerm) {
                currentTerm = logEntry.getPosition().term();
                if (!segments.contains(Segment.of(currentTerm))) {
                    var newSegment = this.newSegment(currentTerm);
                    segments.add(newSegment);
                }
            }
            var appendLog = this.appendLog(currentTerm, logEntry.getCommand());
            results.add(appendLog);
        }
        return results;
    }

    /**
     * Truncates a range of log entries within a specific term.
     * If the truncation starts from index 0, it will also truncate the entire segment.
     * This method is only used by follower nodes.
     *
     * @param term      The term containing the logs to truncate
     * @param fromIndex The starting index of logs to truncate (inclusive)
     * @return true if the truncation was successful, false otherwise
     */
    public boolean truncateLogs(long term, long fromIndex) {
        var success = logRepository.truncateLogs(term, fromIndex);
        if (success && fromIndex == 0) {
            return this.truncateSegment(term);
        }
        return success;
    }

    /**
     * Retrieves a specific log entry by its term and index.
     *
     * @param term  The term number
     * @param index The index of the log entry
     * @return The requested LogEntry, or null if not found
     */
    public LogEntry getLog(long term, long index) {
        return logRepository.getLog(term, index);
    }

    /**
     * Retrieves a range of log entries within a specific term.
     *
     * @param term      The term number
     * @param fromIndex The starting index (inclusive)
     * @param toIndex   The ending index (inclusive)
     * @return List of LogEntries within the specified range
     */
    public List<LogEntry> getLogs(long term, long fromIndex, long toIndex) {
        return logRepository.getLogs(term, fromIndex, toIndex);
    }
}
