package functions;

import io.quarkus.funqy.Funq;
import io.minio.MinioClient;
import io.minio.GetObjectArgs;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

/**
 * Your Function class
 */
public class Function {

    private static final String ENDPOINT = "10.244.1.10";
    private static final int PORT = 9000;
    private static final String ACCESS_KEY = "minioadmin";
    private static final String SECRET_KEY = "minioadmin123";
    private static final boolean USE_SSL = false;
    private static final String BUCKET = "mybucket";
    private static final String OBJECT = "file.txt";

    @Funq
    public Output function(Input input) {
        try {
            MinioClient minioClient = MinioClient.builder()
                .endpoint(ENDPOINT, PORT, USE_SSL)
                .credentials(ACCESS_KEY, SECRET_KEY)
                .build();

            // Try to get existing content
            byte[] existingContent;
            try (InputStream objectStream = minioClient.getObject(
                    GetObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(OBJECT)
                        .build())) {

                existingContent = readAllBytes(objectStream);

            } catch (MinioException | IOException e) {
                // If object does not exist or error occurs, treat content as empty
                existingContent = new byte[0];
            }

            // Append new timestamp
            String timestamp = Instant.now().toString() + "\n";
            byte[] updatedContent = concatenate(existingContent, timestamp.getBytes());

            // Upload updated object
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(updatedContent)) {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(OBJECT)
                        .stream(inputStream, updatedContent.length, -1)
                        .contentType("text/plain")
                        .build());
            }

            return new Output("Timestamp appended successfully");

        } catch (Exception e) {
            return new Output("Error: " + e.getMessage());
        }
    }

    // Utility method to concatenate two byte arrays
    private byte[] concatenate(byte[] a, byte[] b) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(a);
            outputStream.write(b);
            return outputStream.toByteArray();
        }
    }

    // Utility method to read all bytes from an InputStream
    private byte[] readAllBytes(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = input.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }
}

