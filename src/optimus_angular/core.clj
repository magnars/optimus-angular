(ns optimus-angular.core
  (:require [optimus-angular.minification]
            [optimus-angular.templates]))

(def create-template-cache optimus-angular.templates/create-template-cache)
(def prepare-for-minification optimus-angular.minification/prepare-for-minification)
