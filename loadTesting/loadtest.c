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

    curl_global_init(CURL_GLOBAL_DEFAULT);
    curl = curl_easy_init();

    if (!curl) {
        fprintf(stderr, "Errore inizializzazione CURL\n");
        return 1;
    }

    for (int i = 0; i < ITERATIONS; i++) {
        printf("Iterazione %d: sleep %d ms\n", i + 1, sleep_time);

        curl_easy_setopt(curl, CURLOPT_URL, url);
        curl_easy_setopt(curl, CURLOPT_NOBODY, 1L); // HEAD request più leggera

        // Inizio cronometro
        clock_gettime(CLOCK_MONOTONIC, &start);
        res = curl_easy_perform(curl);
        clock_gettime(CLOCK_MONOTONIC, &end);
        // Fine cronometro

        if (res != CURLE_OK) {
            fprintf(stderr, "Errore richiesta: %s\n", curl_easy_strerror(res));
        } else {
            long long latency_ns = diff_ns(start, end);
            printf("Latenza: %lld ns (%.3f ms)\n", latency_ns, latency_ns / 1e6);
        }

        sleep_ms(sleep_time);

        if (strcmp(mode, "inc") == 0 && sleep_time > 1)
            sleep_time /= 2;
    }

    curl_easy_cleanup(curl);
    curl_global_cleanup();
    return 0;
}

