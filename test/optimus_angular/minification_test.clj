(ns optimus-angular.minification-test
  (:use midje.sweet
        optimus-angular.minification))

(fact
 "When minifying JavaScript, local variable names are changed to be
  just one letter. This reduces file size, but disrupts some libraries
  that use clever reflection tricks - like Angular.JS.

  When preparing for minification, these reflection tricks are
  replaced by an alternate syntax that still functions after mangling
  of local names."

 (ng-annotate-js "angular.module('whatever').controller('MyCtrl', function ($scope, $http) {});")
 => "angular.module('whatever').controller('MyCtrl', [\"$scope\", \"$http\", function ($scope, $http) {}]);")

(fact
 "It prepares a list of JS assets."
 (prepare-for-minification [{:path "app.js" :contents "angular.module('whatever').factory('MyFactory', function ($http) {});"}
                            {:path "lib.js" :contents "angular.module('whatever').directive('MyDirective', function ($scope) {});"}])
 => [{:path "app.js" :contents "angular.module('whatever').factory('MyFactory', [\"$http\", function ($http) {}]);"}
     {:path "lib.js" :contents "angular.module('whatever').directive('MyDirective', [\"$scope\", function ($scope) {}]);"}])

(fact
 "It only prepares .js files"
 (prepare-for-minification [{:path "app.js" :contents "angular.module('whatever').factory('MyFactory', function ($http) {});"}
                            {:path "styles.css" :contents "#id { margin: 0; }"}])
 => [{:path "app.js" :contents "angular.module('whatever').factory('MyFactory', [\"$http\", function ($http) {}]);"}
     {:path "styles.css" :contents "#id { margin: 0; }"}])

(fact
 "It includes the path in exception."
 (prepare-for-minification [{:path "app.js" :contents "angular.module('whatever')."}])
 => (throws Exception "Exception in app.js: error: couldn't process source due to parse error"))

(fact
 "It doesn't fall over and die when encountering DOS line endings."
 (prepare-for-minification [{:path "app.js" :contents "angular.module('whatever').factory('MyFactory', function ($http) {});\r\n"}])
 => [{:path "app.js" :contents "angular.module('whatever').factory('MyFactory', [\"$http\", function ($http) {}]);\n"}])
