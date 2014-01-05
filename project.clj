(defproject optimus-angular "0.2.2"
  :description "Angular.JS optimizations for Optimus"
  :url "http://github.com/magnars/optimus-angular"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [optimus "0.13.6"]
                 [clj-v8 "0.1.5"]]
  :profiles {:dev {:dependencies [[midje "1.5.0"]]
                   :plugins [[lein-midje "3.0.0"]
                             [lein-shell "0.3.0"]]}}
  :prep-tasks [["shell" "./build-js-sources.sh"]])
