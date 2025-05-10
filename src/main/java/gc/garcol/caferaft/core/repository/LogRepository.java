package gc.garcol.caferaft.core.repository;

import gc.garcol.caferaft.core.client.Command;
import gc.garcol.caferaft.core.log.LogEntry;
import gc.garcol.caferaft.core.log.Segment;

import java.util.List;

/**
 * The LogRepository interface defines the contract for managing the Raft log entries and segments.
 * It provides methods for both leader and follower nodes to handle log operations in a Raft consensus system.
 * <p>
 * The log is organized into segments, where each segment contains log entries for a specific term.
 * This interface supports the core Raft log operations including appending, replicating, and truncating logs.
 *
 * @author thaivc
 * @since 2025
 */
public interface LogRepository {

    /**
     * Retrieves all log segments in the repository.
     *
     * @return A List containing all sorted segments, ordered by their terms
     */
    List<Segment> allSortedSegments();

    Segment generateSegment(long term);

    /**
     * Get total number of logs in a segment
     *
     * @param term The term number of the segment
     * @return total logs in segment
     */
    long segmentSize(long term);

    /**
     * Truncates a specific segment identified by its term.
     * This method is only used by follower nodes.
     *
     * @param term The term of the segment to truncate
     * @return true if the truncation was successful, false otherwise
     */
    boolean truncateSegment(long term);

    /**
     * Appends a single log entry with the given command to the log.
     * This method is only used by leader nodes.
     *
     * @param term    The term number for the log entry
     * @param command The command to be logged
     * @return The newly created LogEntry
     */
    LogEntry appendLog(long term, Command command);

    LogEntry appendLog(Segment segment, Command command);

    /**
     * Truncates a range of log entries within a specific term.
     * This method is only used by follower nodes.
     *
     * @param term      The term containing the logs to truncate
     * @param fromIndex The starting index of logs to truncate (inclusive)
     * @return true if the truncation was successful, false otherwise
     */
    boolean truncateLogs(long term, long fromIndex);

    boolean truncateLogs(Segment segment, long fromIndex);

    /**
     * Retrieves a specific log entry by its term and index.
     *
     * @param term  The term number
     * @param index The index of the log entry
     * @return The requested LogEntry, or null if not found
     */
    LogEntry getLog(long term, long index);

    LogEntry getLog(Segment segment, long index);
}
