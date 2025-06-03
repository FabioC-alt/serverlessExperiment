notify-send "Inizio Tests hello Cold Start"

./loadtestCS http://hello-fn.default.127.0.0.1.sslip.io  const 50  latency_java_hello_coldStart_50_FabioPc.csv

notify-send "Inizio Test minio Cold Start"

./loadtestCS http://hello-fn.default.127.0.0.1.sslip.io  const 50  latency_java_hello_coldStart_50_FabioPc.csv


notify-send "Inizio Test hello End Latency"

./loadtestEL http://minio-node.default.127.0.0.1.sslip.io  const 500  latency_java_minio_endLatencyt_500_FabioPc.csv

notify-send "Inizio Test minio End Latency"

./loadtestEL http://hello-fn.default.127.0.0.1.sslip.io  const 500  latency_java_hello_endLatency_500_FabioPc.csv


notify-send "Inizio Test hello End Latency Increm"

./loadtestEL http://hello-fn.default.127.0.0.1.sslip.io  const 500  latency_java_hello_endLatencyInc_500_FabioPc.csv

notify-send "Inizio Test minio End Latency Increm"

./loadtestEL http://hello-fn.default.127.0.0.1.sslip.io  const 500  latency_java_minio_endLatencyInc_500_FabioPc.csv


notify-send "Fine test tutti i test"
