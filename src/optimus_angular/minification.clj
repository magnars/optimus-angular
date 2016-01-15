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

(defn- ng-annotate-code [js]
  (str "(function () {
    try {
        var nga = require('ng-annotate');
        var generated = nga('" (escape (normalize-line-endings js)) "', { add: true });
        if (generated.errors) return 'ERROR: ' + generated.errors[0];
        return generated.src;
    } catch (e) { return 'ERROR: ' + e.message; }
}());"))

(def ng-annotate
  (slurp (clojure.java.io/resource "ng-annotate.js")))

(defn create-ng-annotate-context []
  (let [context (v8/create-context)]
    (v8/run-script-in-context context ng-annotate)
    context))

(defn ng-annotate-js
  ([js] (ng-annotate-js js {}))
  ([js options] (ng-annotate-js (create-ng-annotate-context) js options))
  ([context js options]
     (run-script-with-error-handling context (ng-annotate-code js) (:path options))))

(defn prepare-one-for-minification
  [context asset]
  (let [#^String path (:path asset)]
    (if (.endsWith path ".js")
      (update-in asset [:contents] #(ng-annotate-js context % {:path path}))
      asset)))

(defn prepare-for-minification
  ([assets options] (prepare-for-minification assets))
  ([assets] (let [context (create-ng-annotate-context)]
              (map #(prepare-one-for-minification context %) assets))))
