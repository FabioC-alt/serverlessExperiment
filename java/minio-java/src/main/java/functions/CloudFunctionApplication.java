@Bean
public Function<Message<String>, String> appendTimestampToMinio() {
    return (inputMessage) -> {
        String endpoint = "http://10.244.1.10:9000";
        String accessKey = "minioadmin";
        String secretKey = "minioadmin123";
        String bucketName = "mybucket";
        String objectName = "file.txt";

        try {
            MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

            byte[] existingContent;
            try (InputStream is = minioClient.getObject(
                    GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build())) {

                existingContent = readAllBytes(is);

            } catch (MinioException | IOException e) {
                // Object not found or unreadable, start fresh
                existingContent = new byte[0];
            }

            String timestamp = Instant.now().toString() + "\n";
            byte[] timestampBytes = timestamp.getBytes(StandardCharsets.UTF_8);

            // Append timestamp
            byte[] updatedContent = new byte[existingContent.length + timestampBytes.length];
            System.arraycopy(existingContent, 0, updatedContent, 0, existingContent.length);
            System.arraycopy(timestampBytes, 0, updatedContent, existingContent.length, timestampBytes.length);

            // Upload updated object
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(new ByteArrayInputStream(updatedContent), updatedContent.length, -1)
                    .contentType("text/plain")
                    .build()
            );

            return "Timestamp appended to file.txt successfully.";

        } catch (MinioException | IOException | java.security.InvalidKeyException | java.security.NoSuchAlgorithmException e) {
            // Catch the checked exceptions explicitly here
            return "Error interacting with MinIO or security exception: " + e.getMessage();
        }
    };
}

