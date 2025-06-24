notify-send "Inizio Tests hello Cold Start"

./loadtestEL http://minio-func.default.127.0.0.1.sslip.io  const 500  latency_go_hello_endLatency_500_FabioPc.csv

notify-send "Inizio Test minio End Latency"

./loadtestEL http://minio-node.default.127.0.0.1.sslip.io  const 500  latency_node_hello_endLatency_500_FabioPc.csv

notify-send "Fine test tutti i test"


