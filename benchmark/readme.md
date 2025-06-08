# Benchmark

## With autocannon

### Transfer money (p97.5: 20,767 Commands/s)

- `appendLogBatchSize` = 400
- `Latency`: 36ms
- `RPS`: 20,767

```shell
autocannon -t64 -c 400 -d 20 -m POST -b '{"fromId": 1, "toId": 2, "amount": 1}' -H 'Content-Type: application/json' http://localhost:8080/balance/transfer
```

![benchmark-deposit.png](benchmark-transfer.png)

### Batching commands (150k Commands/s)

![benchmark-batching-40.png](benchmark-batching-40.png)

```shell
autocannon -c 200 -d 10 -m POST -b '{"commands":[{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":10}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":11}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":12}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":13}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":15}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":15}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":16}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":17}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":18}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":19}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":20}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":10}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":11}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":12}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":13}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":15}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":15}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":16}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":17}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":18}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":19}},{"correlationId":"fd66e9e2-26e4-491a-817b-5c862d96fc72","depositCommand":{"id":1,"amount":20}}]}' -H 'Content-Type: application/json' http://localhost:8080/balance/batch
```

Request with a batch size of 40 commands, the system processes more than `3,800` requests per second, resulting in a
throughput of `150,000` commands per second.

## With wrk

```shell
wrk -t16 -c32 -d10s -s benchmark/deposit.lua http://localhost:8080/balance/deposit
```
