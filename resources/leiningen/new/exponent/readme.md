## {{name}}

### Usage

#### Install Exponent [XDE and mobile client](https://docs.getexponent.com/versions/v10.0.0/introduction/installation.html)

#### Install [Lein](http://leiningen.org/#install)

#### Install npm modules

``` shell
npm install
```

#### Start the figwheel server
``` shell
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

### Production build (generates main.js)

``` shell
lein prod-build
```
