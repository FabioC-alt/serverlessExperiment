const Minio = require('minio');

/**
 * Your HTTP handling function, invoked with each request.
 *
 * @param {Context} context
 * @param {object} body
 */
const handle = async (context, body) => {
  const endpoint = '10.244.1.10';
  const port = 9000;
  const useSSL = false;
  const accessKey = process.env.MINIO_ACCESS_KEY || 'minioadmin';
  const secretKey = process.env.MINIO_SECRET_KEY || 'minioadmin123';
  const bucketName = 'mybucket';
  const objectName = 'file.txt';
  const timestamp = new Date().toISOString();

  const client = new Minio.Client({
    endPoint: endpoint,
    port: port,
    useSSL: useSSL,
    accessKey: accessKey,
    secretKey: secretKey,
  });

  try {
    // Check if bucket exists
    const exists = await client.bucketExists(bucketName);
    if (!exists) {
      context.log.error(`Bucket "${bucketName}" not found`);
      return { statusCode: 404, body: 'Bucket not found' };
    }

    // Read existing file content, if any
    let existingContent = '';
    try {
      const stream = await client.getObject(bucketName, objectName);
      existingContent = await streamToString(stream);
    } catch (err) {
      context.log.warn('Object not found or unreadable, creating new one.');
    }

    const updatedContent = existingContent + timestamp + '\n';

    await client.putObject(bucketName, objectName, updatedContent, {
      'Content-Type': 'text/plain',
    });

    context.log.info('Timestamp appended successfully.');
    return { body: 'Timestamp appended to file.txt successfully.' };

  } catch (err) {
    context.log.error('Error interacting with MinIO:', err);
    return { statusCode: 500, body: 'Internal server error' };
  }
};

// Helper to convert a stream to a string
const streamToString = (stream) => {
  return new Promise((resolve, reject) => {
    let data = '';
    stream.on('data', chunk => data += chunk);
    stream.on('end', () => resolve(data));
    stream.on('error', err => reject(err));
  });
};

module.exports = { handle };

