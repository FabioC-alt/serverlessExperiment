package function

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"net/http/httputil"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
)

// Handle an HTTP Request.
func Handle(w http.ResponseWriter, r *http.Request) {

	endpoint := "10.244.1.10:9000"
	accessKeyID := "minio-admin"
	secretAccessKey := "minio-admin"
	useSSL := false

	minioClient, err := minio.New(endpoint, &minio.Options{
		Creds:  credentials.NewStaticV4(accessKeyID, secretAccessKey, ""),
		Secure: useSSL,
	})
	if err != nil {
		log.Fatalln(err)
	}

	log.Printf("%#v\n", minioClient)

	err = minioClient.MakeBucket(context.Background(), "mybucket", minio.MakeBucketOptions{
		Region:        "us-east-1",
		ObjectLocking: true,
	})
	if err != nil {
		fmt.Println(err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	fmt.Println("Successfully created mybucket.")

	dump, err := httputil.DumpRequest(r, true)
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}

	fmt.Println("Received request")
	fmt.Printf("%q\n", dump)
	fmt.Fprintf(w, "%q", dump)
}

