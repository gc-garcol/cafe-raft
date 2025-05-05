package gc.garcol.caferaft.core.log;

import gc.garcol.caferaft.core.client.Command;
import gc.garcol.caferaft.core.repository.LogRepository;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static gc.garcol.caferaft.core.constant.LogConstant.INITIAL_POSITION;

/**
 * The LogManager class is responsible for managing the Raft log entries and segments.
 * It acts as a high-level manager that coordinates log operations between the Raft consensus system
 * and the underlying LogRepository implementation.
 *
 * @author thaivc
 * @since 2025
 */
public class LogManager {

    /**
     * The set of all log segments, ordered by their terms.
     * Each segment contains log entries for a specific term.
     */
    public final List<Segment> segments;

    /**
     * The underlying repository that handles the actual storage of log entries and segments.
     */
    private final LogRepository logRepository;

    public LogManager(LogRepository logRepository) {
        this.logRepository = logRepository;
        segments = logRepository.allSortedSegments();
    }

    public long segmentSize(long term) {
        return logRepository.segmentSize(term);
    }

    public Segment lastSegment() {
        return segments.isEmpty() ? null : segments.getLast();
    }

    public Position lastPosition() {
        long lastSegment = Optional.ofNullable(this.lastSegment()).map(Segment::getTerm).orElse(0L);
        long lastIndex = lastSegment == 0 ? 0 : this.segmentSize(lastSegment) - 1;
        return new Position(lastSegment, lastIndex);
    }

    public Position nextPosition(Position position) {
        if (position.term() == 0 && position.index() == 0) {
            if (!segments.isEmpty()) {
                Segment segment = segments.getFirst();
                long segmentSize = segmentSize(segment.getTerm());
                return segmentSize > 0 ? new Position(segment.getTerm(), 0) : null;
            }
        }

        int currentSegmentIndex = Collections.binarySearch(segments, Segment.of(position.term()));
        if (currentSegmentIndex < 0) {
            return null;
        }

        long segmentSize = segmentSize(position.term());
        // index is in [0, size) - or not the last index of the segment
        if (position.index() < segmentSize - 1) {
            return new Position(
                position.term(),
                position.index() + 1
            );
        }

        // if the position is last log of segment, then move on to the start of the next segment
        int nextSegmentIndex = currentSegmentIndex + 1;

        // the arg position is the last position of the entry logs
        if (nextSegmentIndex == segments.size()) {
            return null;
        }

        var nextSegment = segments.get(nextSegmentIndex);
        return new Position(
            nextSegment.getTerm(),
            0
        );
    }

    public Position previousPosition(Position position) {
        int currentSegmentIndex = Collections.binarySearch(segments, Segment.of(position.term()));
        if (currentSegmentIndex < 0) {
            return null;
        }

        long segmentSize = segmentSize(position.term());
        if (position.index() >= segmentSize) {
            return null;
        }
        
        if (position.index() > 0) {
            return new Position(
                position.term(),
                position.index() - 1
            );
        }

        // case position is 0, then return the last-index of the previous-segment
        int previousSegmentIndex = currentSegmentIndex - 1;
        if (previousSegmentIndex < 0) {
            return INITIAL_POSITION.copy();
        }

        var previousSegment = segments.get(previousSegmentIndex);
        var previousSegmentSize = segmentSize(previousSegment.getTerm());
        return new Position(
            previousSegment.getTerm(),
            previousSegmentSize - 1
        );
    }

    /**
     * Truncates a specific segment identified by its term.
     * This method is only used by follower nodes.
     *
     * @param term The term of the segment to truncate
     */
    public void truncateSegment(long term) {
        var success = logRepository.truncateSegment(term);
        if (success) {
            var index = Collections.binarySearch(segments, Segment.of(term));
            segments.remove(index);
        }
    }

    /**
     * Appends a single log entry with the given command to the current term's log.
     * This method is only used by leader nodes.
     *
     * @param term    the segment
     * @param command The command to be logged
     * @return The newly created LogEntry
     */
    public LogEntry appendLog(long term, Command command) {
        if (this.segments.isEmpty() || this.segments.getLast().getTerm() < term) {
            this.segments.add(Segment.of(term));
        } else if (this.segments.getLast().getTerm() > term) {
            throw new IllegalArgumentException("Term is less than the last segment term");
        }

        return logRepository.appendLog(term, command);
    }

    /**
     * Truncates a range of log entries within a specific position to all its follow positions
     * If the truncation starts from index 0, it will also truncate the entire segment.
     * This method is only used by follower nodes.
     *
     * @param term      The term containing the logs to truncate
     * @param fromIndex The starting index of logs to truncate (inclusive)
     * @return true if the truncation was successful, false otherwise
     */
    public void truncateLogs(long term, long fromIndex) {
        if (!logRepository.truncateLogs(term, fromIndex)) {
            return;
        }

        int segmentIndex = Collections.binarySearch(segments, Segment.of(term));

        if (fromIndex == 0) {
            this.truncateSegment(term);
        }

        // [Docs]: If an existing entry conflicts with a new one (same index
        // but different terms), delete the existing entry and all that
        // follow it (§5.3)
        Iterator<Segment> segmentIterator = segments.listIterator(segmentIndex + 1);
        while (segmentIterator.hasNext()) {
            Segment segment = segmentIterator.next();
            if (logRepository.truncateSegment(segment.getTerm())) {
                segmentIterator.remove();
            }
        }
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
}
