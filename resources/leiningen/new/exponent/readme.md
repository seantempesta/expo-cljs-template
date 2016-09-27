## {{name}}

### Usage

#### Install Exponent
    [XDE and mobile client](https://docs.getexponent.com/versions/v10.0.0/introduction/installation.html).

#### Install [Lein](http://leiningen.org/#install)

#### Install npm modules

``` shell
npm install
```

#### Start the figwheel server
``` shell
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
