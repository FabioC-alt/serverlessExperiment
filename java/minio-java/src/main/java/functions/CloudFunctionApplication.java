@Bean
public Function<Message<String>, String> updateFile() {
  return (inputMessage) -> {
    String bucketName = "mybucket";
    String objectName = "file.txt";
    String timestamp = java.time.ZonedDateTime.now().toString();
    StringBuilder result = new StringBuilder();

    MinioClient minioClient = MinioClient.builder()
        .endpoint("http://10.244.1.10:9000")
        .credentials("minioadmin", "minioadmin123")
        .build();

    try {
      byte[] existingContent;

      // Try reading the object
      try (InputStream stream = minioClient.getObject(
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
      String updated = new String(existingContent, StandardCharsets.UTF_8) + timestamp + "\n";

      // Create temporary file and write content to it
      File tempFile = File.createTempFile("updated-content", ".txt");
      tempFile.deleteOnExit();
      try (var fos = new FileOutputStream(tempFile)) {
        fos.write(updated.getBytes(StandardCharsets.UTF_8));
      }

      // Upload using FileInputStream
      try (FileInputStream fis = new FileInputStream(tempFile)) {
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(fis, tempFile.length(), -1)
                .contentType("text/plain")
                .build());
      }

      result.append("Timestamp appended successfully.");
    } catch (Exception e) {
      result.append("Error: ").append(e.getMessage());
    }

    return result.toString();
  };
}

