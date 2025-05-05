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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
            return new ArrayList<>();
        }

        var dataFiles = baseDir.listFiles((dir, name) -> name.endsWith(".data.dat"));
        if (dataFiles == null) {
            return new ArrayList<>();
        }

        return Stream.of(dataFiles)
            .map(file -> {
                Segment segment = new Segment();
                segment.setName(file.getName());
                segment.setTerm(LogUtil.term(file.getName()));
                return segment;
            })
            .sorted()
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public long segmentSize(long term) {
        String indexFileName = clusterProperty.getBaseDisk() + File.separator + LogUtil.indexName(term);
        try (RandomAccessFile indexFile = new RandomAccessFile(indexFileName, "r")) {
            return indexFile.length() / INDEX_LENGTH;
        } catch (Exception e) {
            log.debug("Failed to get segment size for term {}", term, e);
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
            log.debug("Failed to truncate segment for term {}", term, e);
            return false;
        }
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

            long index = indexFile.length() / INDEX_LENGTH - 1;
            LogEntry logEntry = new LogEntry();
            logEntry.setCommand(command);
            logEntry.setPosition(new Position(term, index));

            return logEntry;
        } catch (Exception e) {
            log.debug("Failed to append log for term {}", term, e);
            throw new RuntimeException(e);
        }
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
            log.debug("Failed to truncate logs for term {} from index {}", term, fromIndex, e);
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
            log.debug("Failed to get log for term {} at index {}", term, index, e);
            return null;
        }
    }
}
