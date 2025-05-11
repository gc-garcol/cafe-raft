-- wrk -t16 -c32 -d10s -s benchmark/deposit.lua http://localhost:8080/balance/deposit

wrk.method = "POST"
wrk.body   = '{"id": 1, "amount": 1}'
wrk.headers["Content-Type"] = "application/json"

-- success = 0
-- fail = 0
--
-- response = function(status, headers, body)
--     if status >= 400 then
--         fail = fail + 1
--     else
--         success = success + 1
--     end
-- end

done = function(summary, latency, requests)
--     local total_requests = summary.requests
--     local error_rate = (fail / total_requests) * 100
--
--     io.write(string.format("Total Requests: %d\n", total_requests))
--     io.write(string.format("Total Success: %d\n", success))
--     io.write(string.format("Total Fail: %d\n", fail))
--     io.write(string.format("Error Rate: %.2f%%\n", error_rate))

    if total_requests == 0 then
        io.write("No latency data collected.\n")
        return
    end

    io.write(string.format("50th percentile latency: %.2f ms\n", latency:percentile(50) / 1000))
    io.write(string.format("90th percentile latency: %.2f ms\n", latency:percentile(90) / 1000))
    io.write(string.format("99th percentile latency: %.2f ms\n", latency:percentile(99) / 1000))
    io.write(string.format("100th percentile latency: %.2f ms\n", latency.max / 1000))
end
