(ns optimus-angular.minification
  (:require [clojure.string :as str]
            [v8.core :as v8]))

(defn- escape [s]
  (-> s
      (str/replace "\\" "\\\\")
      (str/replace "'" "\\'")
      (str/replace "\n" "\\n")))

(defn- throw-v8-exception [#^String text path]
  (if (= (.indexOf text "ERROR: ") 0)
    (let [prefix (when path (str "Exception in " path ": "))
          error (clojure.core/subs text 7)]
      (throw (Exception. (str prefix error))))
    text))

(defn- run-script-with-error-handling [context script file-path]
  (throw-v8-exception
   (try
     (v8/run-script-in-context context script)
     (catch Exception e
       (str "ERROR: " (.getMessage e))))
   file-path))

(defn normalize-line-endings [s]
  (-> s
      (str/replace "\r\n" "\n")
      (str/replace "\r" "\n")))

(defn- ngmin-code [js]
  (str "(function () {
    try {
        var ngmin = require('ngmin');
        var generated = ngmin.annotate('" (escape (normalize-line-endings js)) "');
        return generated;
    } catch (e) { return 'ERROR: ' + e.message; }
}());"))

(def ngmin
  (slurp (clojure.java.io/resource "ngmin.js")))

(defn create-ngmin-context []
  (let [context (v8/create-context)]
    (v8/run-script-in-context context ngmin)
    context))

(defn ngmin-js
  ([js] (ngmin-js js {}))
  ([js options] (ngmin-js (create-ngmin-context) js options))
  ([context js options]
     (run-script-with-error-handling context (ngmin-code js) (:path options))))

(defn prepare-one-for-minification
  [context asset]
  (let [#^String path (:path asset)]
    (if (.endsWith path ".js")
      (update-in asset [:contents] #(ngmin-js context % {:path path}))
      asset)))

(defn prepare-for-minification
  ([assets options] (prepare-for-minification assets))
  ([assets] (let [context (create-ngmin-context)]
              (map #(prepare-one-for-minification context %) assets))))
