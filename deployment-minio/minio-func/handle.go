package function

import (
	"bytes"
	"io"
	"context"
	"fmt"
	"log"
	"time"
	"net/http"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
)

// Handle an HTTP Request.
func Handle(w http.ResponseWriter, r *http.Request) {

	endpoint := "10.244.1.13:9000"
	accessKeyID := "minioadmin"
	secretAccessKey := "minioadmin123"
	useSSL := false
	bucketName := "mybucket"
	objectName := "file.txt"
	timestamp := time.Now().Format(time.RFC3339)
	

	minioClient, err := minio.New(endpoint, &minio.Options{
		Creds:  credentials.NewStaticV4(accessKeyID, secretAccessKey, ""),
		Secure: useSSL,
	})
	if err != nil {
		log.Fatalln(err)
	}

	ctx := context.Background()
	
	log.Printf("%#v\n", minioClient)

	object, err := minioClient.GetObject(ctx, bucketName, objectName, minio.GetObjectOptions{})

	var existingContent []byte
	
	if err == nil {
		// Try reading object content
		existingContent, err = io.ReadAll(object)
		if err != nil {
			log.Println("File not found or unreadable. Creating new one.")
			existingContent = []byte{}
		}
	} else {
		log.Println("Object not found. Creating new one.")
		existingContent = []byte{}
	}

	
	// Append new timestamp
	updatedContent := append(existingContent, []byte(timestamp+"\n")...)

	// Upload object back to bucket
	_, err = minioClient.PutObject(ctx, bucketName, objectName,
		bytes.NewReader(updatedContent), int64(len(updatedContent)),
		minio.PutObjectOptions{ContentType: "text/plain"})

	if err != nil {
		log.Fatalln("Failed to upload file:", err)
	}

	fmt.Println("Timestamp appended to log.txt successfully.")
}

