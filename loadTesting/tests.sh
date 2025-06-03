echo 'Inizio test hello'
notify-send "Inizio Tests"

./loadtest http://hello-fn.default.127.0.0.1.sslip.io  const 50  latency_js_hello_coldStart_50_FabioPc.csv

./loadtest http://minio-node.default.127.0.0.1.sslip.io  const 50  latency_js_minio_coldStart_50_FabioPc.csv

notify-send "Fine test minio"
