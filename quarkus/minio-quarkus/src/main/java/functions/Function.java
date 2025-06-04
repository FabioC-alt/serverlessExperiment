package functions;

import io.minio.*;
import io.minio.errors.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.logging.Logger;

@Path("/")
public class Function {

    private static final Logger LOGGER = Logger.getLogger(Function.class.getName());

    private final String endpoint = "http://10.244.1.10:9000";
    private final String accessKey = "minioadmin";
    private final String secretKey = "minioadmin123";
    private final String bucketName = "mybucket";
    private final String objectName = "file.txt";

    @POST
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.TEXT_PLAIN)
    public Response handle(String body) {
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
                existingContent = is.readAllBytes();
            } catch (Exception e) {
                LOGGER.info("Object not found or unreadable. Creating new one.");
                existingContent = new byte[0];
            }

            String timestamp = ZonedDateTime.now().toString() + "\n";
            byte[] updatedContent = concat(existingContent, timestamp.getBytes(StandardCharsets.UTF_8));

            try (ByteArrayInputStream bais = new ByteArrayInputStream(updatedContent)) {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(bais, updatedContent.length, -1)
                        .contentType("text/plain")
                        .build()
                );
            }

            return Response.ok("Timestamp appended to file.txt successfully.\n").build();

        } catch (Exception e) {
            LOGGER.severe("Failed to process request: " + e.getMessage());
            return Response.serverError().entity("Internal Server Error\n").build();
        }
    }

    private byte[] concat(byte[] a, byte[] b) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(a);
        outputStream.write(b);
        return outputStream.toByteArray();
    }
}

