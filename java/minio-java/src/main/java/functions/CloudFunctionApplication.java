package functions;

import io.minio.GetObjectArgs;
import io.minio.PutObjectArgs;
import io.minio.MinioClient;

import java.nio.charset.StandardCharsets;
import java.io.ByteArrayInputStream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;

import java.util.function.Function;

@SpringBootApplication
public class CloudFunctionApplication {

  public static void main(String[] args) {
    SpringApplication.run(CloudFunctionApplication.class, args);
  }

  @Bean
  public Function<Message<String>, String> echo() {
    return (inputMessage) -> {

      var stringBuilder = new StringBuilder();
      inputMessage.getHeaders().forEach((key, value) -> {
        stringBuilder.append(key).append(": ").append(value).append(" ");
      });

      var payload = inputMessage.getPayload();

      if (!payload.isBlank()) {
        stringBuilder.append("echo: ").append(payload);
      }

      return stringBuilder.toString();
    };
  }

  @Bean
  public Function<Message<String>, String> updateFile() {
    return (inputMessage) -> {
      String bucketName = "mybucket";
      String objectName = "file.txt";
      String timestamp = java.time.ZonedDateTime.now().toString();
      StringBuilder result = new StringBuilder();

      // Create MinioClient inside the function (Solution 2)
      MinioClient minioClient = MinioClient.builder()
          .endpoint("http://10.244.1.10:9000")
          .credentials("minioadmin", "minioadmin123")
          .build();

      try {
        byte[] existingContent;

        // Try reading the object
        try (var stream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build())) {

          existingContent = stream.readAllBytes();
          result.append("Existing file read successfully.\n");
        } catch (Exception e) {
          existingContent = new byte[0];
          result.append("File not found. Creating a new one.\n");
        }

        // Append timestamp
        String updated = new String(existingContent) + timestamp + "\n";
        byte[] updatedBytes = updated.getBytes(StandardCharsets.UTF_8);

        // Upload the object
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(new ByteArrayInputStream(updatedBytes), updatedBytes.length, -1)
                .contentType("text/plain")
                .build());

        result.append("Timestamp appended successfully.");
      } catch (Exception e) {
        result.append("Error: ").append(e.getMessage());
      }

      return result.toString();
    };
  }
}

