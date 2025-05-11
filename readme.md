# Cafe Raft

Paper: https://raft.github.io/raft.pdf

## Development

```shell
./gradlew bootRun --args='--cluster.properties.nodeId=0 --server.port=8080'
./gradlew bootRun --args='--cluster.properties.nodeId=1 --server.port=8081'
./gradlew bootRun --args='--cluster.properties.nodeId=2 --server.port=8082'
```

### Node info

```shell
curl http://localhost:8080/actuator/info
```

Results:
- Follower
```shell
{"state":"FOLLOWER","leaderId":1,"nodeId":0}
```

- Leader:
```shell
{"nodeId":1,"leaderId":1,"state":"LEADER"}
```

- Candidate:
```shell
{"leaderId":-1,"state":"CANDIDATE","nodeId":2}
```

## Features

| Leader Election + Log Replication | Persistence | Membership Changes | Log Compaction |
|:---------------------------------:|:-----------:|:------------------:|:--------------:|
| Yes                               | Yes         | No                 | No             |

## Architecture & Flow

![state-mapping.png](docs/state-mapping.png)

![append-entries.png](docs/append-entries.png)
Note: the `term` and `index` will be composed to be `position`

## Build

```shell
docker build -t cafe-raft:latest .
docker compose up -d
```
