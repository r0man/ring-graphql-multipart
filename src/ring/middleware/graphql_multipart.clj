(ns ring.middleware.graphql-multipart
  (:require [clojure.data.json :as json]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.walk :as walk]))

(defn underscore
  "Replace all \"_\" in `s` with \"-\"."
  [s]
  (some-> s name (str/replace #"-" "_" )))

(defn- transform-keys
  "Transform all keys in `m` by applying `f` to them."
  [m f]
  (let [f (fn [[k v]] [(f k) v])]
    (walk/postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defn- graphql-multipart?
  "Returns true if `request` is a GraphQL multipart request, otherwise false."
  [request]
  (set/subset? #{"map" "operations"} (set (keys (:multipart-params request)))))

(defn- variable-path
  "Returns the path to update a variable."
  [s]
  (map #(if (re-matches #"\d+" %1)
          (Integer/parseInt %1) %1)
       (str/split s #"\.")))

(defn- multipart-param
  "Returns the `part` from the :multipart-params of `request`."
  [request part]
  (get-in request [:multipart-params (name part)]))

(defn- operations-part
  "Returns the parsed \"operations\" section from the multipart `request`."
  [request]
  (some-> request (multipart-param "operations") json/read-str))

(defn- map-part
  "Returns the parsed \"map\" section from the multipart `request`."
  [request]
  (some-> request (multipart-param "map") (json/read-str :key-fn keyword)))

(defn- rewrite-operations
  "Rewrite the GraphQL operations from the :multipart-params of
  `request` into the :body."
  [request & [opts]]
  (if-let [part (operations-part request)]
    (assoc request :body part)
    request))

(defn- rewrite-file
  "Rewrite the GraphQL variables from the :multipart-params of `request`
  into the :body for the given `part-name` and `paths`."
  [request [part-name paths] & [{:keys [key-fn]}]]
  (reduce (fn [request path]
            (assoc-in request (concat [:body] (variable-path path))
                      (-> (multipart-param request part-name)
                          (transform-keys (or key-fn underscore)))))
          request paths))

(defn- rewrite-variables
  "Rewrite the GraphQL variables from the :multipart-params of `request`
  into the :body."
  [request & [opts]]
  (reduce #(rewrite-file %1 %2 opts) request (map-part request)))

(defn- rewrite-request
  "Rewrite the GraphQL multipart `request`."
  [request & [opts]]
  (-> (rewrite-operations request opts)
      (rewrite-variables opts)))

(defn wrap-graphql-multipart
  "Wrap `handler` with middleware that rewrites GraphQL multipart requests."
  [handler & [{:keys [key-fn] :as opts}]]
  (fn [request]
    (if (graphql-multipart? request)
      (handler (rewrite-request request opts))
      (handler request))))
