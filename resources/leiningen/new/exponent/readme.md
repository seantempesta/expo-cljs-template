## {{name}}

### Usage

#### Install Expo [XDE and mobile client](https://docs.expo.io/versions/v14.0.0/introduction/installation.html)
    If you don't want to use XDE (not IDE, it stands for Expo Development Tools), you can use [exp CLI](https://docs.expo.io/versions/v14.0.0/guides/exp-cli.html).

``` shell
    yarn install -g exp
```

#### Install [Lein](http://leiningen.org/#install) or [Boot](https://github.com/boot-clj/boot)

#### Install npm modules

``` shell
    yarn install
```

#### Signup using exp CLI

``` shell
    exp signup
```

#### Start the figwheel server and cljs repl

##### leiningen users
``` shell
    lein figwheel
```

##### boot users
``` shell
    boot dev

    ;; then input (cljs-repl) in the connected clojure repl to connect to boot cljs repl
```

#### Start Exponent server (Using `exp`)

##### Also connect to Android device

``` shell
    exp start -a --lan
```

##### Also connect to iOS Simulator

``` shell
    exp start -i --lan
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

#### leiningen users
``` shell
lein prod-build
```

#### boot users
``` shell
boot prod
```
