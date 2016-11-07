# Exponent

[Exponentjs](https://getexponent.com/) template for [Clojurescript](http://clojurescript.org/).

Most of the ideas came from
[re-natal](https://github.com/drapanjanas/re-natal) by [@drapanjanas](https://github.com/drapanjanas) and
[exp-cljstest](https://github.com/exponentjs/exp-cljstest) by [@nikki93](https://github.com/nikki93).

## Features
#### 1. Support reagent and om (defaults to reagent)
#### 2. Auto generated externs for google closure advanced compilation
#### 3. Support source maps
#### 4. Using external modules or assets without restarting the repl.

``` shell
lein new exponent your-project +reagent
lein new exponent your-project +om
```

## Setup
#### 1. install [Exponent XDE and mobile client](https://docs.getexponent.com/versions/v10.0.0/introduction/installation.html)
#### 2. install [Lein](http://leiningen.org/#install)

## Usage

```shell
lein new exponent your-project

cd your-project

npm install

lein figwheel
```

### Add new assets or external modules
1. `require` module:

``` clj
    (def cljs-logo (js/require "./assets/images/cljs.png"))
    (def FontAwesome (js/require "@exponent/vector-icons/FontAwesome"))
```
2. Reload simulator or device

### Make sure you disable live reload from the Developer Menu, also turn off Hot Module Reload.
Since Figwheel already does those.

### Production build (generates js/externs.js and main.js)

``` shell
lein prod-build
```

## License

Copyright © 2016 Tienson Qin

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
