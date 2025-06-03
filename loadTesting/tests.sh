echo 'Inizio test hello'
notify-send "Inizio Tests"
./loadtest http://hello-fn.default.127.0.0.1.sslip.io const 100 latency_js_hello_endLatency_100_FabioPc.csv
./loadtest http://hello-fn.default.127.0.0.1.sslip.io const 500 latency_js_hello_endLatencyInc_500_FabioPc.csv

echo 'Terminato test hello'
notify-send "Terminato test hello"

echo 'Inizio test minio'
./loadtest http://minio-node.default.127.0.0.1.sslip.io const 100 latency_js_minio_endLatency_100_FabioPc.csv
./loadtest http://minio-node.default.127.0.0.1.sslip.io const 500 latency_js_minio_endLatencyInc_500_FabioPc.csv
notify-send "Terminato test minio"
