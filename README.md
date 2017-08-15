# Notice
> Please ensure you are using the latest version of this template `0.19.0`
>
> Status: `Boot` isn't working.  Om and Reagent templates should be working.
>
> Pull requests welcome!  I don't know enough about `Boot` or `Rum` (or have enough time to learn) to support them! 

# Expo

[Expo](https://expo.io/) template for [Clojurescript](http://clojurescript.org/).

Most of the ideas came from
[re-natal](https://github.com/drapanjanas/re-natal) by [@drapanjanas](https://github.com/drapanjanas) and
[exp-cljstest](https://github.com/exponentjs/exp-cljstest) by [@nikki93](https://github.com/nikki93).

If you have any questions or suggestions, you can also join Expo slack #clojurescript,
https://slack.exponentjs.com/

## Features
#### 1. Supports [leiningen](https://github.com/technomancy/leiningen)
#### 2. Support reagent and om (defaults to reagent)
#### 3. Auto generated externs for google closure advanced compilation (*experiment*)
#### 4. Support source maps
#### 5. Using external modules or assets without restarting the repl.

## Dependencies (do this first!)
#### 1. install [Expo XDE and mobile client](https://docs.expo.io/versions/v19.0.0/introduction/installation.html)
#### 2. install [Lein](http://leiningen.org/#install)
#### 3. install [Yarn](https://yarnpkg.com/lang/en/docs/install/)


## Usage
#### 1. create your project

```shell
lein new expo your-project +reagent
lein new expo your-project +om
```
#### 2. change into your project's new directory

```shell
cd your-project
```

#### 2. install npm dependencies
```shell
yarn install
```

#### 3. run figwheel for a development REPL
```shell
lein figwheel
```

### To add new assets or external modules
1. Just `js/require` it somewhere in your code:

``` clj
    (def cljs-logo (js/require "./assets/images/cljs.png"))
    (def FontAwesome (js/require "@expo/vector-icons/FontAwesome"))
```
2. Reload simulator or device

### To create a Production build (use this when you "Publish")
(generates js/externs.js and main.js)

``` shell
lein prod-build
```


### Tips!

* Make sure you disable "Live Reload" and "Hot Reload" from the [Developer Menu](https://facebook.github.io/react-native/docs/debugging.html).
(Figwheel does this better!)


## License

Copyright © 2016 Tienson Qin

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
