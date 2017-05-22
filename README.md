# Notice
> Tienson Qin (@tiensonqin) recently transferred ownership and maintenance of `expo-cljs-template` to me.  Please bear with me while I get my footing.  :)
>
> Major Upgrades in progress to bring this project up to date with the latest expo version 17.0.0.  The `reagent` + `lein` versions should work, but I'm slowly working on updating the other libraries and boot.
>
> Pull requests welcome!

# Expo

[Expo](https://expo.io/) template for [Clojurescript](http://clojurescript.org/).

Most of the ideas came from
[re-natal](https://github.com/drapanjanas/re-natal) by [@drapanjanas](https://github.com/drapanjanas) and
[exp-cljstest](https://github.com/exponentjs/exp-cljstest) by [@nikki93](https://github.com/nikki93).

If you have any questions or suggestions, you can also join Expo slack #clojurescript,
https://slack.exponentjs.com/

## Features
#### 1. Support both [leiningen](https://github.com/technomancy/leiningen) and [boot](https://github.com/boot-clj/boot)
#### 2. Support reagent, om and rum (defaults to reagent)
#### 3. Auto generated externs for google closure advanced compilation (*experiment*)
#### 4. Support source maps
#### 5. Using external modules or assets without restarting the repl.

``` shell
lein new expo your-project +reagent
lein new expo your-project +om
lein new expo your-project +rum
```

## Setup
#### 1. install [Expo XDE and mobile client](https://docs.expo.io/versions/v17.0.0/introduction/installation.html)
#### 2. install [Lein](http://leiningen.org/#install) or [Boot](https://github.com/boot-clj/boot)

## Usage

```shell
lein new expo your-project

npm install -g yarn
cd your-project
yarn install

;; leiningen users
lein figwheel

;; boot users
boot dev

;; then input (cljs-repl) to connect to boot cljs repl
```

### Add new assets or external modules
1. `require` module:

``` clj
    (def cljs-logo (js/require "./assets/images/cljs.png"))
    (def FontAwesome (js/require "@expo/vector-icons/FontAwesome"))
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

## License

Copyright © 2016 Tienson Qin

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
