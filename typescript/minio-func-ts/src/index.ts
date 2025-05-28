import { Context, StructuredReturn } from 'faas-js-runtime';
import { Client } from 'minio';

const handle = async (context: Context): Promise<StructuredReturn> => {
  const endpoint = '10.244.1.10';
  const port = 9000;
  const accessKey = process.env.MINIO_ACCESS_KEY || 'minioadmin';
  const secretKey = process.env.MINIO_SECRET_KEY || 'minioadmin123';
  const bucket = 'mybucket';
  const object = 'file.txt';
  const useSSL = false;

  const minioClient = new Client({
    endPoint: endpoint,
    port: port,
    useSSL: useSSL,
    accessKey: accessKey,
    secretKey: secretKey
  });

  const timestamp = new Date().toISOString();
  let existingContent = '';

  try {
    const stream = await minioClient.getObject(bucket, object);

    const chunks: Buffer[] = [];
    for await (const chunk of stream) {
      chunks.push(chunk as Buffer);
    }

    existingContent = Buffer.concat(chunks).toString();
  } catch (err: any) {
    context.log.warn(`Could not read object: ${err.message}`);
    existingContent = '';
  }

  const updatedContent = existingContent + timestamp + '\n';
  const buffer = Buffer.from(updatedContent);

  try {
    await minioClient.putObject(bucket, object, buffer, buffer.length, {
      'Content-Type': 'text/plain'
    });
    context.log.info(`Updated ${object} with new timestamp.`);
  } catch (err: any) {
    context.log.error(`Failed to upload object: ${err.message}`);
    return {
      statusCode: 500,
      body: JSON.stringify({ error: 'Failed to upload file' }),
      headers: { 'content-type': 'application/json' }
    };
  }

  return {
    statusCode: 200,
    body: JSON.stringify({ message: 'Timestamp appended to file.txt successfully' }),
    headers: { 'content-type': 'application/json' }
  };
};

export { handle };

