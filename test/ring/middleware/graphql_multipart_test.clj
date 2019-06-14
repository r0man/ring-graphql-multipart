(ns ring.middleware.graphql-multipart-test
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.test :refer :all]
            [ring.middleware.graphql-multipart :as multipart]))

(def file-a
  (io/file "test-resources/a.txt"))

(def file-b
  (io/file "test-resources/b.txt"))

(def file-c
  (io/file "test-resources/c.txt"))

(def file-query
  (slurp (io/resource "upload-file.graphql")))

(def files-query
  (slurp (io/resource "upload-files.graphql")))

(def request
  {:headers {}
   :protocol "HTTP/1.1"
   :remote-addr "127.0.0.1"
   :request-method :post
   :schema :http
   :server-name "localhost"
   :server-port 80
   :uri "/graphql"})

(def file-request
  (merge request
         {:multipart-params
          {"operations"
           (json/write-str
            {:query file-query
             :variables {:file nil}})
           "map"
           (json/write-str
            {"0" ["variables.file"]})
           "0"
           {:filename "a.txt"
            :content-type "application/octet-stream"
            :tempfile file-a
            :size (.length file-a)}}}))

(def files-request
  (merge request
         {:multipart-params
          {"operations"
           (json/write-str
            {:query files-query
             :variables {:files [nil nil]}})
           "map"
           (json/write-str
            {"0" ["variables.files.0"]
             "1" ["variables.files.1"]})
           "0"
           {:filename "b.txt"
            :content-type "application/octet-stream"
            :tempfile file-b
            :size (.length file-b)}
           "1"
           {:filename "c.txt"
            :content-type "application/octet-stream"
            :tempfile file-c
            :size (.length file-c)}}}))

(deftest test-file-request
  (let [request ((multipart/wrap-graphql-multipart identity) file-request)]
    (is (= file-query (get-in request [:body "query"])))
    (is (= {"file"
            {"filename" "a.txt"
             "content_type" "application/octet-stream"
             "tempfile" file-a
             "size" 20}}
           (get-in request [:body "variables"])))))

(deftest test-files-request
  (let [request ((multipart/wrap-graphql-multipart identity) files-request)]
    (is (= files-query (get-in request [:body "query"])))
    (is (= {"files"
            [{"filename" "b.txt"
              "content_type" "application/octet-stream"
              "tempfile" file-b
              "size" 20}
             {"filename" "c.txt"
              "content_type" "application/octet-stream"
              "tempfile" file-c
              "size" 22}]}
           (get-in request [:body "variables"])))))
