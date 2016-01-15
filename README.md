# optimus-angular [![Build Status](https://secure.travis-ci.org/magnars/optimus-angular.png)](http://travis-ci.org/magnars/optimus-angular)

Angular.JS optimizations for [Optimus](http://github.com/magnars/optimus).

## Install

Add `[optimus-angular "0.3.0"]` to `:dependencies` in your `project.clj`.

## Usage

This project offers two distinct features. It helps you:

- Prepopulate the Angular.JS template cache.
- Prepare JavaScript for minification with [ng-annotate](https://github.com/olov/ng-annotate).

Both features work with [Optimus](http://github.com/magnars/optimus),
or any other asset serving framework that uses the same data structure
for asset representation: `[{:path :contents}]`

## Prepopulating the Angular.JS template cache

`optimus-angular/create-template-cache` is a custom Optimus asset
loader. It creates a virtual JavaScript asset that populates the
Angular.JS template cache with your given templates.

Here's an example usage:

```cl
(ns my-app.example
  (require [optimus.assets :as assets]
           [optimus-angular.core :as optimus-angular]))

(defn get-assets [] ;; 1
  (concat
   (assets/load-bundles "public" my-bundles) ;; 1
   [(optimus-angular.templates/create-template-cache ;; 2
     :path "/templates/angular.js" ;; 3
     :module "MYAPP" ;; 4
     :templates (assets/load-assets "public" ;; 5
                                    ["/angular/templates/home.html"
                                     "/angular/templates/form.html"
                                     "/angular/templates/list.html"]))]))
```

1. You create the template cache in your `get-assets` function along
   with your other assets.

2. Notice that `create-template-cache` creates a single asset, so it's
   in a vector to concat with the other assets .

3. You use the `:path` to reference the virtual asset when linking.

4. The name of your Angular module.

5. It takes a list of assets to include in the cache. These could be
   virtual too, like if you're creating your HTML with hiccup.

So, it creates a file `/templates/angular.js` that inlines the templates
and adds them to the `$templateCache`.

You link to this script with:

```cl
(optimus/file-path request "/templates/angular.js")
```

Or let's say you have a bundle named `app.js`. You can add a `:bundle
"app.js"` pair to the `create-template-cache` call, and the file will
be bundled together with the rest of the javascript files in
`/bundles/app.js`. Nifty.

#### Do I have to enumerate all the templates like that?

Dear me, no. That would be a chore. Optimus' `load-assets` supports
regex to pick up files on the class path:

```cl
(assets/load-assets "public" [#"/angular/templates/.+\.html$"])
```

## Preparing JavaScript for minification

When minifying JavaScript, local variable names are changed to be just
one letter. This reduces file size, but disrupts some libraries that
use clever reflection tricks - like Angular.JS.

By transforming your assets with
`optimus-angular/prepare-for-minification`, these reflection tricks
are replaced by an alternate syntax that still functions after
mangling of local names.

Make sure to insert this asset transformation earlier in the stack
than the js-minifier. This would be a good way of doing it:

```cl
(defn my-optimize [assets options]
  (-> assets
      (optimus-angular/prepare-for-minification)
      (optimus.optimizations/all options)))
```

This will change code like this:

```js
angular.module('my-app').controller('MyCtrl', function ($scope, $http) {});
```

into code like this:

```js
angular.module('my-app').controller('MyCtrl', [
  '$scope', '$http', function ($scope, $http) {}
]);
```

which can be safely minified. There are limitations tho.
Optimus-angular uses [ng-annotate](https://github.com/olov/ng-annotate) to do
this job. Please read more about how it works in
[the ng-annotate README](https://github.com/olov/ng-annotate).

## Changelog

#### From 0.2 to 0.3

- Switch from [ngmin](https://github.com/btford/ngmin) to [ng-annotate](https://github.com/olov/ng-annotate)

#### From 0.1 to 0.2

- Add Last-Modified headers to template asset

## Contributing

Yes, please do!

#### Installing dependencies

You need [npm](https://npmjs.org/) installed to fetch the JavaScript
dependencies. The actual fetching is automated however.

#### Running the tests

`lein midje` will run all tests.

`lein midje namespace.*` will run only tests beginning with "namespace.".

`lein midje :autotest` will run all the tests indefinitely. It sets up a
watcher on the code files. If they change, only the relevant tests will be
run again.

## License

Copyright © 2013 Magnar Sveen

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
