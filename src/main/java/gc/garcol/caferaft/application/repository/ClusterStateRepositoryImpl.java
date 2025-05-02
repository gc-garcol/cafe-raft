package gc.garcol.caferaft.application.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import gc.garcol.caferaft.core.constant.ClusterProperty;
import gc.garcol.caferaft.core.repository.ClusterStateRepository;
import gc.garcol.caferaft.core.state.NodeId;
import gc.garcol.caferaft.core.state.PersistentState;
import gc.garcol.caferaft.core.util.LogUtil;
import lombok.SneakyThrows;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * @author thaivc
 * @since 2025
 */
@Repository
public class ClusterStateRepositoryImpl implements ClusterStateRepository {

    private final ClusterProperty clusterProperty;
    private final RandomAccessFile fileWriter;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    private ClusterStateRepositoryImpl(final ClusterProperty clusterProperty) {
        this.clusterProperty = clusterProperty;
        var baseDisk = clusterProperty.getBaseDisk();
        LogUtil.createDirectoryNX(baseDisk);
        fileWriter = new RandomAccessFile(baseDisk + File.separator + "node-state.dat", "rw");
        this.objectMapper = new ObjectMapper();
    }

    @Override
    @SneakyThrows
    public PersistentState load() {
        if (fileWriter.length() == 0) {
            var persistentState = new PersistentState();
            persistentState.setNodeId(new NodeId(clusterProperty.getNodeId()));
            persistentState.setTotalNodes(clusterProperty.getNodes().size());
            return persistentState;
        }

        fileWriter.seek(0);
        byte[] bytes = new byte[(int) fileWriter.length()];
        fileWriter.readFully(bytes);
        return objectMapper.readValue(bytes, PersistentState.class);
    }

    @Override
    @SneakyThrows
    public void save(PersistentState persistentState) {
        String json = objectMapper.writeValueAsString(persistentState);
        fileWriter.setLength(0);
        fileWriter.seek(0);
        fileWriter.write(json.getBytes());
    }
}
