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
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A simple implementation of LogRepository that stores log entries in files.
 * Each term has two files:
 * - A data file (.data.dat) containing the actual log entries
 * - An index file (.index.dat) containing metadata about the entries
 *
 * @author thaivc
 * @since 2025
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class SimpleLogRepository implements LogRepository {

    private static final String READ_MODE        = "r";
    private static final String READ_WRITE_MODE  = "rw";
    private static final String DATA_FILE_SUFFIX = ".data.dat";
    private static final int    INDEX_LENGTH     = Long.BYTES + Integer.BYTES + Integer.BYTES; // offset + length + type

    private final ClusterProperty clusterProperty;
    private final CommandSerdes   commandSerdes;

    @Override
    public List<Segment> allSortedSegments() {
        File baseDir = new File(clusterProperty.getBaseDisk());
        if (!baseDir.exists()) {
            return new ArrayList<>();
        }

        var dataFiles = baseDir.listFiles((dir, name) -> name.endsWith(DATA_FILE_SUFFIX));
        if (dataFiles == null) {
            return new ArrayList<>();
        }

        return Stream.of(dataFiles).map(file -> {
            var term = LogUtil.term(file.getName());
            return generateSegment(term);
        }).sorted().collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    @SneakyThrows
    public Segment generateSegment(long term) {
        Segment segment = new Segment();
        segment.setTerm(term);
        segment.setRwFile(rwFile(term));
        segment.setIndexFile(indexFile(term));
        segment.setSize(segment.getIndexFile().length() / INDEX_LENGTH);
        return segment;
    }

    @SneakyThrows
    private RandomAccessFile rwFile(long term) {
        return new RandomAccessFile(getDataFilePath(term), READ_WRITE_MODE);
    }

    @SneakyThrows
    private RandomAccessFile indexFile(long term) {
        return new RandomAccessFile(getIndexFilePath(term), READ_WRITE_MODE);
    }

    private String getDataFilePath(long term) {
        return clusterProperty.getBaseDisk() + File.separator + LogUtil.segmentName(term);
    }

    private String getIndexFilePath(long term) {
        return clusterProperty.getBaseDisk() + File.separator + LogUtil.indexName(term);
    }

    @Override
    public long segmentSize(long term) {
        try (RandomAccessFile indexFile = new RandomAccessFile(getIndexFilePath(term), READ_MODE)) {
            return indexFile.length() / INDEX_LENGTH;
        } catch (IOException e) {
            log.error("Failed to get segment size for term {}", term, e);
            return 0;
        }
    }

    @Override
    public boolean truncateSegment(long term) {
        File dataFile = new File(getDataFilePath(term));
        File indexFile = new File(getIndexFilePath(term));

        try {
            boolean dataDeleted = !dataFile.exists() || dataFile.delete();
            boolean indexDeleted = !indexFile.exists() || indexFile.delete();
            return dataDeleted && indexDeleted;
        } catch (Exception e) {
            log.error("Failed to truncate segment for term {}", term, e);
            return false;
        }
    }

    @Override
    public LogEntry appendLog(long term, Command command) {
        try (RandomAccessFile file = new RandomAccessFile(getDataFilePath(term),
                READ_WRITE_MODE); RandomAccessFile indexFile = new RandomAccessFile(getIndexFilePath(term),
                READ_WRITE_MODE)) {
            return appendLogInternal(file, indexFile, term, command);
        } catch (Exception e) {
            log.error("Failed to append log for term {}", term, e);
            throw new RuntimeException("Failed to append log", e);
        }
    }

    @Override
    public LogEntry appendLog(Segment segment, Command command) {
        try {
            return appendLogInternal(segment.getRwFile(), segment.getIndexFile(), segment.getTerm(), command);
        } catch (Exception e) {
            log.error("Failed to append log for term {}", segment.getTerm(), e);
            throw new RuntimeException("Failed to append log", e);
        }
    }

    private LogEntry appendLogInternal(RandomAccessFile file, RandomAccessFile indexFile, long term, Command command)
            throws IOException {
        byte[] data = commandSerdes.toBytes(command);
        long fileLength = file.length();
        int dataLength = data.length;

        file.seek(file.length());
        file.write(data);

        indexFile.seek(indexFile.length());
        indexFile.writeLong(fileLength);
        indexFile.writeInt(dataLength);
        indexFile.writeInt(commandSerdes.type(command));

        long index = indexFile.length() / INDEX_LENGTH - 1;
        LogEntry logEntry = new LogEntry();
        logEntry.setCommand(command);
        logEntry.setPosition(new Position(term, index));

        return logEntry;
    }

    @Override
    public boolean truncateLogs(long term, long fromIndex) {
        if (fromIndex == 0) {
            return truncateSegment(term);
        }

        try (RandomAccessFile file = new RandomAccessFile(getDataFilePath(term),
                READ_WRITE_MODE); RandomAccessFile indexFile = new RandomAccessFile(getIndexFilePath(term),
                READ_WRITE_MODE)) {
            indexFile.seek(fromIndex * INDEX_LENGTH);
            long offset = indexFile.readLong();

            file.setLength(offset);
            indexFile.setLength(fromIndex * INDEX_LENGTH);
            return true;
        } catch (Exception e) {
            log.error("Failed to truncate logs for term {} from index {}", term, fromIndex, e);
            return false;
        }
    }

    @Override
    public boolean truncateLogs(Segment segment, long fromIndex) {
        if (fromIndex == 0) {
            return truncateSegment(segment.getTerm());
        }

        try {
            segment.getIndexFile().seek(fromIndex * INDEX_LENGTH);
            long offset = segment.getIndexFile().readLong();

            segment.getRwFile().setLength(offset);
            segment.getIndexFile().setLength(fromIndex * INDEX_LENGTH);
            return true;
        } catch (Exception e) {
            log.error("Failed to truncate logs for term {} from index {}", segment.getTerm(), fromIndex, e);
            return false;
        }
    }

    @Override
    public LogEntry getLog(long term, long index) {
        try (RandomAccessFile file = new RandomAccessFile(getDataFilePath(term),
                READ_MODE); RandomAccessFile indexFile = new RandomAccessFile(getIndexFilePath(term), READ_MODE)) {
            return getLogInternal(file, indexFile, term, index);
        } catch (Exception e) {
            log.error("Failed to get log for term {} at index {}", term, index, e);
            return null;
        }
    }

    @Override
    public LogEntry getLog(Segment segment, long index) {
        try {
            return getLogInternal(segment.getRwFile(), segment.getIndexFile(), segment.getTerm(), index);
        } catch (Exception e) {
            log.error("Failed to get log for term {} at index {}", segment.getTerm(), index, e);
            return null;
        }
    }

    private LogEntry getLogInternal(RandomAccessFile file, RandomAccessFile indexFile, long term, long index)
            throws IOException {
        indexFile.seek(index * INDEX_LENGTH);
        long offset = indexFile.readLong();
        int length = indexFile.readInt();
        int type = indexFile.readInt();

        file.seek(offset);
        byte[] data = new byte[length];
        file.readFully(data);

        Command command = commandSerdes.fromBytes(type, data);

        LogEntry logEntry = new LogEntry();
        logEntry.setCommand(command);
        logEntry.setPosition(new Position(term, index));
        return logEntry;
    }
}
