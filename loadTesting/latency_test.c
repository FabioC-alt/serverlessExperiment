// latency_test.c
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <curl/curl.h>

long get_timestamp_ms() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return (tv.tv_sec * 1000L) + (tv.tv_usec / 1000L);
}

int main(int argc, char *argv[]) {
    if (argc < 2) {
        printf("Usage: %s <URL>\n", argv[0]);
        return 1;
    }

    const char *url = argv[1];
    CURL *curl;
    CURLcode res;

    curl_global_init(CURL_GLOBAL_ALL);
    curl = curl_easy_init();

    if (curl) {
        long start = get_timestamp_ms();

        curl_easy_setopt(curl, CURLOPT_URL, url);
        curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L);
        res = curl_easy_perform(curl);

        long end = get_timestamp_ms();

        if (res != CURLE_OK)
            fprintf(stderr, "curl_easy_perform() failed: %s\n", curl_easy_strerror(res));
        else
            printf("Latency: %ld ms\n", end - start);

        curl_easy_cleanup(curl);
    }

    curl_global_cleanup();
    return 0;
}

