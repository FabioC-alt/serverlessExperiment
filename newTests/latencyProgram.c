#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <curl/curl.h>
#include <time.h>

int main(int argc, char *argv[]) {
    if (argc != 3) {
        fprintf(stderr, "Usage: %s <host-header> <output-file>\n", argv[0]);
        return 1;
    }

    const char *host_header_value = argv[1];
    const char *output_file = argv[2];
    CURL *curl;
    CURLcode res;
    struct curl_slist *headers = NULL;

    char host_header[512];
    snprintf(host_header, sizeof(host_header), "Host: %s", host_header_value);
    headers = curl_slist_append(headers, host_header);

    FILE *logfile = fopen(output_file, "w");
    if (!logfile) {
        perror("Failed to open output file");
        return 1;
    }

    curl = curl_easy_init();
    if (!curl) {
        fprintf(stderr, "Failed to initialize curl.\n");
        fclose(logfile);
        return 1;
    }

    curl_easy_setopt(curl, CURLOPT_URL, "http://192.168.17.120");
    curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);
    curl_easy_setopt(curl, CURLOPT_VERBOSE, 0L);

    for (int i = 1; i <= 10; i++) {
        printf("Request #%d:\n", i);

        struct timespec start, end;
        clock_gettime(CLOCK_MONOTONIC, &start);

        res = curl_easy_perform(curl);

        clock_gettime(CLOCK_MONOTONIC, &end);

        long seconds = end.tv_sec - start.tv_sec;
        long nanoseconds = end.tv_nsec - start.tv_nsec;
        double elapsed_ms = seconds * 1000.0 + nanoseconds / 1.0e6;

        if (res != CURLE_OK) {
            fprintf(stderr, "Request failed: %s\n", curl_easy_strerror(res));
            fprintf(logfile, "Request #%d: ERROR\n", i);
        } else {
            printf("Request #%d completed in %.2f ms.\n", i, elapsed_ms);
            fprintf(logfile, "Request #%d: %.2f ms\n", i, elapsed_ms);
        }

        if (i < 10) {
            printf("Waiting 70 seconds before next request...\n\n");
            sleep(70);
        }
    }

    fclose(logfile);
    curl_slist_free_all(headers);
    curl_easy_cleanup(curl);

    printf("All latencies written to %s\n", output_file);

    return 0;
}

