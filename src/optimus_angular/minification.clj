(ns optimus-angular.minification
  (:require [clojure.string :as str]
            [optimus.js :as js]))

(defn- escape [s]
  (-> s
      (str/replace "\\" "\\\\")
      (str/replace "'" "\\'")
      (str/replace "\n" "\\n")))

(defn normalize-line-endings [s]
  (-> s
      (str/replace "\r\n" "\n")
      (str/replace "\r" "\n")))

(defn- ng-annotate-code [js]
  (str "(function () {
        var nga = require('ng-annotate');
        var generated = nga('" (escape (normalize-line-endings js)) "', { add: true });
        if (generated.errors) throw new Error(generated.errors[0]);
        return generated.src;
}());"))

(def ng-annotate
  (slurp (clojure.java.io/resource "ng-annotate.js")))

(defn prepare-ng-annotate-engine []
  (let [engine (js/make-engine)]
    (.eval engine ng-annotate)
    engine))

(defn ng-annotate-js
  ([js] (ng-annotate-js js {}))
  ([js options]
   (js/with-engine [engine (prepare-ng-annotate-engine)]
    (ng-annotate-js engine js options)))
  ([engine js options]
     (js/run-script-with-error-handling engine (ng-annotate-code js) (:path options))))

(defn prepare-one-for-minification
  [engine asset]
  (let [#^String path (:path asset)]
    (if (.endsWith path ".js")
      (update-in asset [:contents] #(ng-annotate-js engine % {:path path}))
      asset)))

(defn prepare-for-minification
  ([assets options] (prepare-for-minification assets))
  ([assets] (js/with-engine [engine (prepare-ng-annotate-engine)]
              (doall (map #(prepare-one-for-minification engine %) assets)))))
