(ns optimus-angular.minification-test
  (:use optimus-angular.minification
        midje.sweet))

(fact
 "When minifying JavaScript, local variable names are changed to be
  just one letter. This reduces file size, but disrupts some libraries
  that use clever reflection tricks - like Angular.JS.

  When preparing for minification, these reflection tricks are
  replaced by an alternate syntax that still functions after mangling
  of local names."

 (ngmin-js "angular.module('whatever').controller('MyCtrl', function ($scope, $http) {});")
 => "angular.module('whatever').controller('MyCtrl', [
  '$scope',
  '$http',
  function ($scope, $http) {
  }
]);")


(fact
 "It prepares a list of JS assets."
 (prepare-for-minification [{:path "app.js" :contents "angular.module('whatever').factory('MyFactory', function ($http) {});"}
                            {:path "lib.js" :contents "angular.module('whatever').directive('MyDirective', function ($scope) {});"}])
 => [{:path "app.js" :contents "angular.module('whatever').factory('MyFactory', [\n  '$http',\n  function ($http) {\n  }\n]);"}
     {:path "lib.js" :contents "angular.module('whatever').directive('MyDirective', [\n  '$scope',\n  function ($scope) {\n  }\n]);"}])

(fact
 "It only prepares .css files"
 (prepare-for-minification [{:path "app.js" :contents "angular.module('whatever').factory('MyFactory', function ($http) {});"}
                            {:path "styles.css" :contents "#id { margin: 0; }"}])
 => [{:path "app.js" :contents "angular.module('whatever').factory('MyFactory', [\n  '$http',\n  function ($http) {\n  }\n]);"}
     {:path "styles.css" :contents "#id { margin: 0; }"}])

(fact
 "It includes the path in exception."
 (prepare-for-minification [{:path "app.js" :contents "angular.module('whatever')."}])
 => (throws Exception "Exception in app.js: Line 1: Unexpected end of input"))

(fact
 "It doesn't fall over and die when encountering DOS line endings."
 (prepare-for-minification [{:path "app.js" :contents "angular.module('whatever').factory('MyFactory', function ($http) {});\r\n"}])
 => [{:path "app.js" :contents "angular.module('whatever').factory('MyFactory', [\n  '$http',\n  function ($http) {\n  }\n]);"}])
