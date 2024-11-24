package gc.garcol.raftcore.domain.repository;

import gc.garcol.raftcore.domain.common.Env;
import gc.garcol.raftcore.domain.model.log.LogIndex;
import gc.garcol.raftcore.domain.model.log.LogMetadata;
import lombok.SneakyThrows;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author thaivc
 * @since 2024
 */
public class LogRepository
{
    private final LogMetadata logMetadata;

    static ByteBuffer indexBufferWriter = ByteBuffer.allocate(LogIndex.SIZE);
    static ByteBuffer indexBufferReader = ByteBuffer.allocate(LogIndex.SIZE);
    static ByteBuffer metadataBufferWriter = ByteBuffer.allocate(LogMetadata.SIZE);
    static ByteBuffer metadataBufferReader = ByteBuffer.allocate(LogMetadata.SIZE);

    @SneakyThrows
    public LogRepository()
    {
        LogUtil.createDirectoryNX(Env.DATA_DIR);
        LogUtil.createDirectoryNX(Env.LOG_DIR);
        logMetadata = readMetadata();
    }

    /**
     * Reads a log entry from the log file and writes it into the provided ByteBuffer.
     * This method change the limit and position of the provided ByteBuffer.
     *
     * @param readerBuffer the ByteBuffer to write the log entry into
     * @param term         the term of the log entry to read
     * @param index        the index of the log entry to read
     * @throws java.io.IOException if an I/O error occurs
     */
    @SneakyThrows
    public void read(ByteBuffer readerBuffer, long term, long index)
    {
        try (
            RandomAccessFile indexFile = new RandomAccessFile(LogUtil.indexPath(term), "r");
            RandomAccessFile logFile = new RandomAccessFile(LogUtil.logPath(term), "r")
        )
        {
            indexFile.seek(index * LogIndex.SIZE);
            var logIndex = new LogIndex()
                .index(indexFile.readLong())
                .entryLength(indexFile.readInt());
            logFile.seek(logIndex.index());
            var buffer = new byte[logIndex.entryLength()];
            logFile.readFully(buffer);

            readerBuffer.clear();
            readerBuffer.limit(logIndex.entryLength());
            var logChannel = logFile.getChannel();
            logChannel.read(readerBuffer, logIndex.index());
            logChannel.close();
            readerBuffer.flip();
        }
    }

    @SneakyThrows
    public void write(ByteBuffer commands)
    {
        var term = logMetadata.currentTerm();
        try (
            RandomAccessFile indexFile = new RandomAccessFile(LogUtil.indexPath(term), "rw");
            RandomAccessFile logFile = new RandomAccessFile(LogUtil.logPath(term), "rw")
        )
        {
            FileChannel indexChannel = indexFile.getChannel();
            FileChannel logChannel = logFile.getChannel();

            indexBufferWriter.clear();
            indexBufferWriter.putLong(logFile.length());
            indexBufferWriter.putInt(commands.limit());
            indexBufferWriter.flip();

            var logOffset = logChannel.size();
            var indexOffset = indexChannel.size();
            logChannel.write(commands, logOffset);
            indexChannel.write(indexBufferWriter, indexOffset);

            logChannel.force(true);
            indexChannel.force(true);
        }
    }

    @SneakyThrows
    public void truncate(long term, long fromIndex)
    {
        try (
            RandomAccessFile indexFile = new RandomAccessFile(LogUtil.indexPath(term), "rw");
            RandomAccessFile logFile = new RandomAccessFile(LogUtil.logPath(term), "rw")
        )
        {
            indexBufferReader.clear();
            var indexChannel = indexFile.getChannel();
            indexChannel.read(indexBufferReader, fromIndex * LogIndex.SIZE);
            indexBufferReader.flip();
            LogIndex logIndex = new LogIndex()
                .index(indexBufferReader.getLong())
                .entryLength(indexBufferReader.getInt());
            indexFile.setLength(fromIndex * LogIndex.SIZE);
            logFile.setLength(logIndex.index());
            indexChannel.close();
        }
    }

    @SneakyThrows
    public long totalIndexOffset(long term)
    {
        try (
            RandomAccessFile indexFile = new RandomAccessFile(LogUtil.indexPath(term), "rw");
        )
        {
            return indexFile.length() / LogIndex.SIZE;
        }
    }

    @SneakyThrows
    public LogMetadata readMetadata()
    {
        try (RandomAccessFile metadataFile = new RandomAccessFile(Env.METADATA_FILE, "rw"))
        {
            if (metadataFile.length() == 0)
            {
                var metadata = new LogMetadata().currentTerm(1);
                writeMetadata(metadata);
                return metadata;
            }
            var metadataChannel = metadataFile.getChannel();
            metadataChannel.position(0);
            metadataBufferReader.clear();
            metadataChannel.read(metadataBufferReader, 0);
            metadataBufferReader.flip();
            metadataChannel.close();
            return new LogMetadata()
                .currentTerm(metadataBufferReader.getLong(0));
        }
    }

    @SneakyThrows
    private void writeMetadata(LogMetadata logMetadata)
    {
        metadataBufferWriter.clear();
        metadataBufferWriter.putLong(0, logMetadata.currentTerm());
        metadataBufferWriter.flip();
        try (RandomAccessFile metadataFile = new RandomAccessFile(Env.METADATA_FILE, "rw"))
        {
            var metaDataChannel = metadataFile.getChannel();
            metaDataChannel.position(0);
            metaDataChannel.write(metadataBufferWriter);
            metaDataChannel.force(true);
            metadataFile.setLength(LogMetadata.SIZE);
        }
    }
}
