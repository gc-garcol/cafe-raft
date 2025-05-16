# Cafe Raft

`Cafe Raft` is a Java implementation of the Raft consensus algorithm, providing a distributed,
fault-tolerant system for managing replicated commands.
It follows the Raft consensus protocol as described in the [Raft paper](https://raft.github.io/raft.pdf).

## Development

Run at least 2 nodes:
```shell
./gradlew bootRun --args='--cluster.properties.nodeId=0 --server.port=8080'
./gradlew bootRun --args='--cluster.properties.nodeId=1 --server.port=8081'
./gradlew bootRun --args='--cluster.properties.nodeId=2 --server.port=8082'
```

Or with `docker compose`:
```shell
docker build -t cafe-raft:latest .
docker compose up -d
```

Or with `k8s`
```shell
cd k8s
kubectl apply -f .
```

### UDP supported

```shell
./gradlew bootRun --args='--cluster.properties.nodeId=0 --server.port=8080 --spring.profiles.active=rpc-udp'
./gradlew bootRun --args='--cluster.properties.nodeId=1 --server.port=8081 --spring.profiles.active=rpc-udp'
./gradlew bootRun --args='--cluster.properties.nodeId=2 --server.port=8082 --spring.profiles.active=rpc-udp'
```

NOTE: When using UDP transport, be aware of the following limitations:
- Maximum UDP datagram size (typically 65,507 bytes) restricts message size
- Configuration parameters `messageBatchSize` and `appendLogBatchSize` should be kept small to avoid fragmentation

### Dashboard

![dashboard.png](docs/dashboard.png)

## Features

| Leader Election + Log Replication | Persistence | Membership Changes | Log Compaction |
|:---------------------------------:|:-----------:|:------------------:|:--------------:|
|                Yes                |     Yes     |         No         |       No       |

## Project structure and Raft paper mapping

### Project structures
- `core`: Core Raft consensus implementation based on the Raft paper, providing leader election, log replication, and persistence.
- `application`: Example application demonstrating Raft usage.

#### Log storage

![log-storage.png](docs/log-storage.png)

### Raft paper mapping

![state-mapping.png](docs/state-mapping.png)

![append-entries.png](docs/append-entries.png)
Note: the `term` and `index` will be composed to be `position`

## My contribution

![contribute.png](docs/contribute.png)
