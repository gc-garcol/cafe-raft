package gc.garcol.caferaft.application.repository;

import gc.garcol.caferaft.core.client.Command;
import gc.garcol.caferaft.core.client.CommandSerdes;
import gc.garcol.caferaft.core.constant.ClusterProperty;
import gc.garcol.caferaft.core.log.LogEntry;
import gc.garcol.caferaft.core.log.Position;
import gc.garcol.caferaft.core.log.Segment;
import gc.garcol.caferaft.core.repository.LogRepository;
import gc.garcol.caferaft.core.util.LogUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author thaivc
 * @since 2025
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SimpleLogRepository implements LogRepository {

    private final ClusterProperty clusterProperty;
    private final CommandSerdes commandSerdes;

    private final int INDEX_LENGTH = Long.BYTES + Integer.BYTES + Integer.BYTES; // offset + length + type

    @Override
    public List<Segment> allSortedSegments() {
        File baseDir = new File(clusterProperty.getBaseDisk());
        if (!baseDir.exists()) {
            return List.of();
        }

        var dataFiles = baseDir.listFiles((dir, name) -> name.endsWith(".data.dat"));
        if (dataFiles == null) {
            return List.of();
        }

        return Stream.of(dataFiles)
            .map(file -> {
                Segment segment = new Segment();
                segment.setName(file.getName());
                segment.setTerm(LogUtil.term(file.getName()));
                return segment;
            })
            .sorted()
            .toList();
    }

    @Override
    public Segment newSegment(long term) {
        String fileName = clusterProperty.getBaseDisk() + File.separator + LogUtil.segmentName(term);
        String indexFileName = clusterProperty.getBaseDisk() + File.separator + LogUtil.indexName(term);

        try (
            RandomAccessFile file = new RandomAccessFile(fileName, "rw");
            RandomAccessFile indexFile = new RandomAccessFile(indexFileName, "rw")
        ) {
            Segment segment = new Segment();
            segment.setTerm(term);
            segment.setName(LogUtil.segmentName(term));
            return segment;
        } catch (Exception e) {
            log.error("Failed to create segment for term {}", term, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public long segmentSize(long term) {
        String indexFileName = clusterProperty.getBaseDisk() + File.separator + LogUtil.indexName(term);
        try (RandomAccessFile indexFile = new RandomAccessFile(indexFileName, "r")) {
            return indexFile.length() / INDEX_LENGTH;
        } catch (Exception e) {
            log.error("Failed to get segment size for term {}", term, e);
            return 0;
        }
    }

    @Override
    public boolean truncateSegment(long term) {
        String fileName = clusterProperty.getBaseDisk() + File.separator + LogUtil.segmentName(term);
        String indexFileName = clusterProperty.getBaseDisk() + File.separator + LogUtil.indexName(term);

        try {
            File dataFile = new File(fileName);
            File indexFile = new File(indexFileName);

            boolean dataDeleted = !dataFile.exists() || dataFile.delete();
            boolean indexDeleted = !indexFile.exists() || indexFile.delete();

            return dataDeleted && indexDeleted;
        } catch (Exception e) {
            log.error("Failed to truncate segment for term {}", term, e);
            return false;
        }
    }

    @Override
    public List<Long> truncateSegments(List<Long> terms) {
        return terms.stream()
            .filter(term -> truncateSegment(term))
            .toList();
    }

    @Override
    public LogEntry appendLog(long term, Command command) {
        String fileName = clusterProperty.getBaseDisk() + File.separator + LogUtil.segmentName(term);
        String fileIndexName = clusterProperty.getBaseDisk() + File.separator + LogUtil.indexName(term);
        try (
            RandomAccessFile file = new RandomAccessFile(fileName, "rw");
            RandomAccessFile indexFile = new RandomAccessFile(fileIndexName, "rw")
        ) {
            byte[] data = commandSerdes.toBytes(command);

            long fileLength = file.length();
            int dataLength = data.length;

            file.seek(file.length());
            file.write(data);

            indexFile.seek(indexFile.length());
            indexFile.writeLong(fileLength);
            indexFile.writeInt(dataLength);
            indexFile.writeInt(commandSerdes.type(command));

            long totalRecords = fileLength / INDEX_LENGTH;

            LogEntry logEntry = new LogEntry();
            logEntry.setCommand(command);
            logEntry.setPosition(new Position(term, totalRecords));

            return logEntry;
        } catch (Exception e) {
            log.error("Failed to append log for term {}", term, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<LogEntry> appendLogs(long term, List<Command> commands) {
        return commands.stream()
            .map(command -> this.appendLog(term, command))
            .toList();
    }

    @Override
    public void replicateLogs(List<LogEntry> logEntries) {
        logEntries.forEach(logEntry -> this.appendLog(logEntry.getPosition().term(), logEntry.getCommand()));
    }

    @Override
    public boolean truncateLogs(long term, long fromIndex) {

        if (fromIndex == 0) {
            return truncateSegment(term);
        }

        String fileName = clusterProperty.getBaseDisk() + File.separator + LogUtil.segmentName(term);
        String indexFileName = clusterProperty.getBaseDisk() + File.separator + LogUtil.indexName(term);

        try (
            RandomAccessFile file = new RandomAccessFile(fileName, "rw");
            RandomAccessFile indexFile = new RandomAccessFile(indexFileName, "rw")
        ) {
            // Calculate the offset in the data file
            indexFile.seek(fromIndex * INDEX_LENGTH);
            long offset = indexFile.readLong();

            // Truncate both files
            file.setLength(offset);
            indexFile.setLength(fromIndex * INDEX_LENGTH);
            return true;
        } catch (Exception e) {
            log.error("Failed to truncate logs for term {} from index {}", term, fromIndex, e);
            return false;
        }
    }

    @Override
    public LogEntry getLog(long term, long index) {
        String fileName = clusterProperty.getBaseDisk() + File.separator + LogUtil.segmentName(term);
        String indexFileName = clusterProperty.getBaseDisk() + File.separator + LogUtil.indexName(term);

        try (
            RandomAccessFile file = new RandomAccessFile(fileName, "r");
            RandomAccessFile indexFile = new RandomAccessFile(indexFileName, "r")
        ) {
            // Read offset and length from index file
            indexFile.seek(index * INDEX_LENGTH);
            long offset = indexFile.readLong();
            int length = indexFile.readInt();
            int type = indexFile.readInt();

            // Read data from data file
            file.seek(offset);
            byte[] data = new byte[length];
            file.readFully(data);

            Command command = commandSerdes.fromBytes(type, data);

            LogEntry logEntry = new LogEntry();
            logEntry.setCommand(command);
            logEntry.setPosition(new Position(term, index));
            return logEntry;
        } catch (Exception e) {
            log.error("Failed to get log for term {} at index {}", term, index, e);
            return null;
        }
    }

    @Override
    public List<LogEntry> getLogs(long term, long fromIndex, long toIndex) {
        return Stream.iterate(fromIndex, i -> i <= toIndex, i -> i + 1)
            .map(index -> getLog(term, index))
            .toList();
    }
}
