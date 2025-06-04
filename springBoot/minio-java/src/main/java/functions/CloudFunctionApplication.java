package functions;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.function.Function;

@SpringBootApplication
public class CloudFunctionApplication {

    private static final String ENDPOINT = "10.244.1.10";
    private static final int PORT = 9000;
    private static final String ACCESS_KEY = "minioadmin";
    private static final String SECRET_KEY = "minioadmin123";
    private static final boolean USE_SSL = false;
    private static final String BUCKET = "mybucket";
    private static final String OBJECT = "file.txt";

    public static void main(String[] args) {
        SpringApplication.run(CloudFunctionApplication.class, args);
    }

    @Bean
    public Function<Message<String>, String> echo() {
        return (inputMessage) -> {
            var stringBuilder = new StringBuilder();
            inputMessage.getHeaders()
                    .forEach((key, value) -> stringBuilder.append(key).append(": ").append(value).append(" "));
            var payload = inputMessage.getPayload();
            if (!payload.isBlank()) {
                stringBuilder.append("echo: ").append(payload);
            }
            return stringBuilder.toString();
        };
    }

    @Bean
    public Function<Message<String>, String> update() {
        return (inputMessage) -> {
            try {
                MinioClient minioClient = MinioClient.builder()
                        .endpoint(ENDPOINT, PORT, USE_SSL)
                        .credentials(ACCESS_KEY, SECRET_KEY)
                        .build();

                byte[] existingContent;

                try (InputStream objectStream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(BUCKET)
                                .object(OBJECT)
                                .build())) {

                    existingContent = objectStream.readAllBytes();

                } catch (MinioException | IOException e) {
                    // If object does not exist or error reading, initialize empty content
                    existingContent = new byte[0];
                }

                String timestamp = Instant.now().toString() + "\n";
                byte[] updatedContent = concatenate(existingContent, timestamp.getBytes());

                try (ByteArrayInputStream inputStream = new ByteArrayInputStream(updatedContent)) {
                    minioClient.putObject(
                            PutObjectArgs.builder()
                                    .bucket(BUCKET)
                                    .object(OBJECT)
                                    .stream(inputStream, updatedContent.length, -1)
                                    .contentType("text/plain")
                                    .build());
                }

                return "Timestamp appended successfully";

            } catch (Exception e) {
                return "Error: " + e.getMessage();
            }
        };
    }

    // Helper method to concatenate two byte arrays
    private static byte[] concatenate(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}

