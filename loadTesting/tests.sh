echo 'Inizio test hello'
notify-send "Inizio Tests"
./loadtest http://hello-fn.default.127.0.0.1.sslip.io const 20 latency_js_hello_20_FabioPc.csv

echo 'Terminato test hello'
notify-send "Terminato test hello"

echo 'Inizio test minio'
./loadtest http://minio-node.default.127.0.0.1.sslip.io const 20 latency_js_minio_20_FabioPc.csv
notify-send "Terminato test minio"
