(defproject optimus-angular "1.0.0-rc1"
  :description "Angular.JS optimizations for Optimus"
  :url "http://github.com/magnars/optimus-angular"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[optimus "1.0.0-rc3"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.10.0"]
                                  [midje "1.9.9"]]
                   :plugins [[lein-midje "3.2.1"]
                             [lein-shell "0.5.0"]]}}
  :prep-tasks [["shell" "./build-js-sources.sh"]])
