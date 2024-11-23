package gc.garcol.raftcore.core.attribute;

import gc.garcol.raftcore.core.EntryPosition;
import gc.garcol.raftcore.core.rpc.RpcRequest;
import gc.garcol.raftcore.core.rpc.RpcResponse;
import gc.garcol.raftcore.share.collection.Tuple;
import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

/**
 * @author thaivc
 * @since 2025
 */
@Getter
public class IncomeClusterRpc implements Attribute
{
    private ConcurrentMap<EntryPosition, ConcurrentMap<UUID, Tuple<RpcRequest, CompletableFuture<RpcResponse>>>> requests;
}
