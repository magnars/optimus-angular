(ns optimus-angular.templates
  (:require [clojure.string :as str]
            [optimus.homeless :refer [assoc-non-nil]]))

(defn- escaped-js-string
  [s]
  (-> s
      (str/replace "\\" "\\\\")
      (str/replace "\"" "\\\"")
      (str/replace "\n" "\\n")))

(defn- template-cache-put
  [template]
  (let [escaped-contents (escaped-js-string (:contents template))]
    (str "  $templateCache.put(\"" (:path template) "\", \"" escaped-contents "\");\n")))

(defn- create-template-cache-js
  [module templates]
  (when-not (and (seq templates)
                 (every? :path templates)
                 (every? :contents templates))
    (throw (IllegalArgumentException. ":templates must be list of assets - try using optimus.assets/load-assets.")))
  (str "angular.module(\"" module  "\").run([\"$templateCache\", function ($templateCache) {\n"
       (apply str (map template-cache-put templates))
       "}]);" ))

(defn- max? [vals]
  (when (seq vals)
    (apply max vals)))

(defn create-template-cache
  [& {:keys [path module templates bundle]}]
  (-> {:path path
       :bundle bundle
       :contents (create-template-cache-js module templates)}
      (assoc-non-nil :last-modified (max? (keep :last-modified templates)))))
