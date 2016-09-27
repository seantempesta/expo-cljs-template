# Exponent

[Exponentjs](https://getexponent.com/) template for [Clojurescript](http://clojurescript.org/).

Most of the ideas come from [exp-cljstest](https://github.com/exponentjs/exp-cljstest) by [@nikki93](https://github.com/nikki93).

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

### Add new assets (images, fonts, videos, etc)
1. Re-generate modules:
``` shell
    lein re-generate
```
2. Click `restart` button on Exponent XDE

### Using external React Native modules
1. Add modules to `js-modules` in `project.clj`.
2. Re-generate modules:
``` shell
    lein re-generate
```
3. Click `restart` button on Exponent XDE


## License

Copyright © 2016 Tienson Qin

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
