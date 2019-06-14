(defproject ring-graphql-multipart "0.1.0"
  :description "Ring middleware for GraphQL multipart form requests"
  :url "https://github.com/r0man/ring-graphql-multipart"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :author "r0man"
  :deploy-repositories [["releases" :clojars]]
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/data.json "0.2.6"]]
  :plugins [[jonase/eastwood "0.3.5"]]
  :aliases {"ci" ["do" ["test"] ["lint"]]
            "lint" ["do"  ["eastwood"]]}
  :profiles {:dev {:dependencies []
                   :resource-paths ["test-resources"]}})
