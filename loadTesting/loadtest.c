#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>       // usleep
#include <string.h>       // strcmp
#include <curl/curl.h>
#include <time.h>

#define INITIAL_SLEEP_MS 1000   // ritardo iniziale in millisecondi
#define ITERATIONS 10

// Calcola la differenza tra due timespec in nanosecondi
long long diff_ns(struct timespec start, struct timespec end) {
    return (end.tv_sec - start.tv_sec) * 1000000000LL +
           (end.tv_nsec - start.tv_nsec);
}

// Sleep in millisecondi
void sleep_ms(int ms) {
    usleep(ms * 1000);
}

int main(int argc, char *argv[]) {
    if (argc != 3) {
        fprintf(stderr, "Uso: %s <URL> <mode>\n", argv[0]);
        fprintf(stderr, "Mode: const | inc\n");
        return 1;
    }

    const char *url = argv[1];
    const char *mode = argv[2];
    int sleep_time = INITIAL_SLEEP_MS;

    CURL *curl;
    CURLcode res;
    struct timespec start, end;

    FILE *latency_file = fopen("latency_results.txt", "w");
    if (!latency_file) {
        perror("Errore apertura file di output");
        return 1;
    }

    curl_global_init(CURL_GLOBAL_DEFAULT);
    curl = curl_easy_init();

    if (!curl) {
        fprintf(stderr, "Errore inizializzazione CURL\n");
        fclose(latency_file);
        return 1;
    }

    fprintf(latency_file, "iterazione; valoreNs; valoreMs");

    for (int i = 0; i < ITERATIONS; i++) {
        printf("Iterazione %d: sleep %d ms\n", i + 1, sleep_time);

        curl_easy_setopt(curl, CURLOPT_URL, url);
        curl_easy_setopt(curl, CURLOPT_NOBODY, 1L); // HEAD request più leggera

        clock_gettime(CLOCK_MONOTONIC, &start);
        res = curl_easy_perform(curl);
        clock_gettime(CLOCK_MONOTONIC, &end);

        if (res != CURLE_OK) {
            fprintf(stderr, "Errore richiesta: %s\n", curl_easy_strerror(res));
        } else {
            long long latency_ns = diff_ns(start, end);
            double latency_ms = latency_ns / 1e6;
            printf("Latenza: %lld ns (%.3f ms)\n", latency_ns, latency_ms);
            fprintf(latency_file, "%d; %lld; %.3f;\n", i + 1, latency_ns, latency_ms);
        }

        sleep_ms(sleep_time);

        if (strcmp(mode, "inc") == 0 && sleep_time > 1)
            sleep_time /= 2;
    }

    curl_easy_cleanup(curl);
    curl_global_cleanup();
    fclose(latency_file);

    printf("Risultati salvati in 'latency_results.txt'\n");
    return 0;
}

