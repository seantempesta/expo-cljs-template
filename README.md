[![Clojars Project](https://img.shields.io/clojars/v/expo/lein-template.svg)](https://clojars.org/expo/lein-template)

# Expo

Create [React Native](https://facebook.github.io/react-native/) apps in [Clojurescript](http://clojurescript.org/) with [Expo](https://expo.io/).  Impress your friends and build truly native apps across iOS and Android in a sane language!

## Status
```diff
+ [expo "22.0.0"]
+ [org.omcljs/om "1.0.0-beta1"]
+ [reagent "0.7.0"]
+ [re-frame "0.9.3"]
+ [rum "0.11.1"]
- [boot *broken*]
```
Pull requests welcome!  I don't know enough about `Boot` (or have enough time to learn) to support them!


## Features
* Reusable codebase for iOS and Android
* Fast development feedback loops with REPL live coding (via [Figwheel](https://github.com/bhauman/lein-figwheel))
* Easily test and publish your apps without installing XCode or Android Studio
* Source map support when debugging Clojurescript
* Supports React wrappers [Reagent](https://github.com/reagent-project/reagent) and [Om.Next](https://github.com/omcljs/om)

## Need help?
* [Expo Documentation](https://docs.expo.io/versions/latest/index.html)
* [Expo Slack](https://slack.exponentjs.com/) #clojurescript
* [Clojure Slack](http://clojurians.net) #cljsrn
* [Hire me](http://tempesta.io).  I'm available for contract work and am happy to help you and your team quickly get up to speed creating your Clojurescript React Native app. :)

## Dependencies (install these first)
* [Expo XDE](https://docs.expo.io/versions/latest/introduction/installation.html)
* [Lein](http://leiningen.org/#install)
* [Yarn](https://yarnpkg.com/lang/en/docs/install/)

## Usage
#### 1. Create your project

```shell
lein new expo your-project +reagent
lein new expo your-project +om
lein new expo your-project +rum
```
#### 2. Change into your project's directory

```shell
cd your-project
```

#### 3. Install npm dependencies
```shell
yarn install
```

#### 4. Start figwheel
To auto-compile Clojurescript code and provide a development REPL
```shell
lein figwheel
```

#### 5. Start XDE and open the project's directory
From here you can Publish, Share, or run the app on a device.  See Expo's [documentation](https://docs.expo.io/versions/latest/guides/up-and-running.html) for more info.

#### 6. [optional] Set lan-ip option via file:
Create file named .lan-ip with your ip. This ip will be used by figwheel to connect via websockets. If this file is not present it gets the ip from the system.

In linux you can execute the following line to create the file.
```shell
source lan-ip.sh
```


## To add new assets or npm modules
1. Just `js/require` it somewhere in your code:

``` clj
    (def cljs-logo (js/require "./assets/images/cljs.png"))
    (def FontAwesome (js/require "@expo/vector-icons/FontAwesome"))
```
2. Reload simulator or device

## Publishing
#### 1. Clean the build directory
```shell
lein clean
```
#### 2. Create a production build
```shell
lein prod-build
```
#### 3. Open XDE and [Publish](https://docs.expo.io/versions/latest/guides/publishing.html)

## Externs
Production builds use `advanced` closure compilation which sometimes cause problems with javascript interop ([details](https://github.com/cljsjs/packages/wiki/Creating-Externs)).  In the past we ran a custom script to try and prepare a proper externs file, but I've found it to be [very](https://github.com/seantempesta/expo-cljs-template/issues/12) [problematic](https://github.com/seantempesta/expo-cljs-template/issues/16) and am now recommending the following:
* Try out the [:externs-inference](https://clojurescript.org/guides/externs#externs-inference) setting in the clojurescript compiler.  It should be enabled by default in newer versions of this template.
* Use an interop package like [cljs-oops](https://github.com/binaryage/cljs-oops) for all `js` interop as dot references can get mangled `(.-property js-object)`
* Add your externs manually to

## Upgrading
As this is only an initial template, you'll want to upgrade to newer versions of `expo`.
Honestly, it's usually as easy as reading the latest [blog post](https://blog.expo.io/expo-sdk-v20-0-0-is-now-available-79f84232a9d1) for the new version
and following the upgrade directions at the bottom.  It usually comes down to:
1. Updating the sdkVersion in `app.json`
2. Updating the react dependencies in `package.json`
3. Deleting your .node_modules directory
4. Running yarn to install the updated dependencies
5. Reopen your project in XDE and press “Restart” to clear the packager cache, or run exp start -c if you use use exp

Sometimes you'll need to upgrade clojurescript rendering dependencies (`reagent` and `om-next`), and in that case I recommend checking
the issues/commits in this project for solutions.

## Tips
* Make sure you disable "Live Reload" and "Hot Reload" from the [Developer Menu](https://facebook.github.io/react-native/docs/debugging.html).
(Figwheel does this better!)

## Inspired by
* [cljs-exponent](https://github.com/tiensonqin/cljs-exponent) by [@tiensonqin](https://github.com/tiensonqin) (forked from original author)
* [re-natal](https://github.com/drapanjanas/re-natal) by [@drapanjanas](https://github.com/drapanjanas) (heavily borrowed from)
* [exp-cljstest](https://github.com/exponentjs/exp-cljstest) by [@nikki93](https://github.com/nikki93)

## License

Copyright © 2017 Sean Tempesta

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
