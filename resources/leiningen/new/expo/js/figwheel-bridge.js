/*
 * Originally taken from https://github.com/decker405/figwheel-react-native
 *
 * @providesModule figwheel-bridge
 */

var debugEnabled = false;

var config = {
    basePath: "target/",
    googBasePath: 'goog/',
    serverPort: 19001
};

var React = require('react');
var createReactClass = require('create-react-class');
var ReactNative = require('react-native');
var Expo = require('expo');
var WebSocket = require('WebSocket');
var self;
var evaluate = eval; // This is needed, direct calls to eval does not work (RN packager???)
var externalModules = {};
var evalListeners = {};
var asyncImportChain = new Promise(function (succ,fail) {succ(true);});

function fireEvalListenters(url) {
    Object.values(evalListeners).forEach(function (listener) {
        listener(url)
    });
}

function formatCompileError(msg) {
    var errorStr = "Figwheel Compile Exception: "
    var data = msg['exception-data'];
    if(data['message']) {
        errorStr += data['message'] + " ";
    }
    if(data['file']) {
        errorStr += "in file " + data['file'] + " ";
    }
    if(data['line']) {
        errorStr += "at line " + data['line'];
    }
    if(data['column']) {
        errorStr += ", column " + data['column'];
    }
    return errorStr;
}

/* This is simply demonstrating that we can receive and react to
 * arbitrary messages from Figwheel this will enable creating a nicer
 * feedback system in the Figwheel top level React component.
 */
function figwheelMessageHandler(msg) {
    if(msg["msg-name"] == "compile-failed") {
        console.warn(formatCompileError(msg));
    }
}

function listenToFigwheelMessages() {
    if(figwheel.client.add_json_message_watch) {
        figwheel.client.add_json_message_watch("ReactNativeMessageIntercept",
            figwheelMessageHandler);
    }
}

var figwheelApp = function (platform, devHost) {
    return createReactClass({
        getInitialState: function () {
            return {loaded: false}
        },
        render: function () {
            if (!this.state.loaded) {
                var plainStyle = {flex: 1, alignItems: 'center', justifyContent: 'center'};
                return (
                    <ReactNative.View style={plainStyle}>
                        <ReactNative.Text>Waiting for Figwheel to load files.</ReactNative.Text>
                    </ReactNative.View>
                );
            }
            return this.state.root;
        },

        componentDidMount: function () {
            var app = this;
            if (typeof goog === "undefined") {
                loadApp(platform, devHost, function (appRoot) {
                    app.setState({root: appRoot, loaded: true});
                    listenToFigwheelMessages();
                });
            }
        }
    })
};

function logDebug(msg) {
    if (debugEnabled) {
        console.log(msg);
    }
}

var isChrome = function () {
    return typeof importScripts === "function"
};

function asyncImportScripts(url, transform, success, error) {
    logDebug('(asyncImportScripts) Importing: ' + url);
    asyncImportChain =
        asyncImportChain
            .then(function (v) {return fetch(url);})
            .then(function (response) {
                if(response.ok)
                    return response.text();
                throw new Error("Failed to Fetch: " + url + " - Perhaps your project was cleaned and you haven't recompiled?")
            })
            .then(function (responseText) {
                evaluate(transform(responseText));
                fireEvalListenters(url);
                success();
                return true;
            })
            .catch(function (e) {
                console.error(e);
                error();
                return true;
            });
}

function syncImportScripts(url, success, error) {
    try {
        importScripts(url);
        logDebug('Evaluated: ' + url);
        fireEvalListenters(url);
        success();
    } catch (e) {
        console.error(e);
        error()
    }
}

// Loads js file sync if possible or async.
function importJs(src, success, error) {
    var noop = function(){};
    var identity = function (arg){return arg};
    var successCb = (typeof success == 'function') ? success : noop;
    var errorCb = (typeof error   == 'function') ? error : noop;
    logDebug('(importJs) Importing: ' + src);
    if (isChrome()) {
        syncImportScripts(src, successCb, errorCb);
    } else {
        asyncImportScripts(src, identity, successCb, errorCb);
    }
}

function interceptRequire() {
    var oldRequire = window.__r;
    console.info("Shimming require");
    function newRequire (id) {
        console.info("Requiring: " + id);
        if (externalModules[id]) {
            return externalModules[id];
        }
        return oldRequire(id);
    }
    window.require = newRequire;
    window.__r = newRequire;
}

function serverBaseUrl(host) {
    return "http://" + host + ":" + config.serverPort
}

function isUnDefined(x) {
    return typeof x == "undefined";
}

// unlikely to happen but it happened to me a couple of times so ...
function assertRootElExists(platform) {
    var basicMessage = "ClojureScript project didn't compile, or didn't load correctly.";
    if(isUnDefined(env)) {
        throw new Error("Critical Error: env namespace not defined - " + basicMessage);
    } else if(isUnDefined(env[platform])) {
        throw new Error("Critical Error: env." + platform + " namespace not defined - " + basicMessage);
    } else if(isUnDefined(env[platform].main)) {
        throw new Error("Critical Error: env." + platform + ".main namespace not defined - " + basicMessage);
    } else if(isUnDefined(env[platform].main.root_el)) {
        throw new Error("Critical Error: env." +
            platform + ".main namespace doesn't define a root-el which should hold the root react node of your app.");
    }
}

function importIndexJs(fileBasePath) {
    var src = fileBasePath + '/index.js';
    var transformFn = function(code) {
        var defines = code.match(new RegExp ("goog.global.CLOSURE_UNCOMPILED_DEFINES.*?;"));
        var deps = code.match(/goog.require\(.*?\);/g);
        var transformedCode = defines.concat(deps).join('');
        logDebug('transformed index.js: ', transformedCode);
        return transformedCode;
    };
    logDebug('(importIndexJs) Importing: ' + src);
    asyncImportScripts(src, transformFn, function(){}, function(){});
}

function loadApp(platform, devHost, onLoadCb) {
    var fileBasePath = serverBaseUrl((isChrome() ? "localhost" : devHost)) + "/" + config.basePath + platform;

    // callback when app is ready to get the reloadable component
    var mainJs = `/env/${platform}/main.js`;
    evalListeners.waitForFinalEval = function (url) {
        if (url.indexOf(mainJs) > -1) {
            assertRootElExists(platform);
            onLoadCb(env[platform].main.root_el);
            console.info('Done loading Clojure app');
            delete evalListeners.waitForFinalEval;
        }
    };

    if (typeof goog === "undefined") {
        console.info('Loading Closure base.');
        interceptRequire();

        // need to know base path here
        importJs(fileBasePath + '/goog/base.js', function () {
            shimBaseGoog(fileBasePath);
            importJs(fileBasePath + '/cljs_deps.js', function () {
                importJs(fileBasePath + '/goog/deps.js', function () {
                    // This is needed because of RN packager
                    // seriously React packager? why.
                    var googreq = goog.require;
                    googreq(`env.${platform}.main`);

                    // Hot reloading Works for figwheel 0.5.14, but not 0.5.18 -- why?
                    // importIndexJs(fileBasePath);
                });
            });
        });
    }
}

function startApp(appName, platform, devHost) {
    Expo.registerRootComponent(figwheelApp(platform, devHost));
}

function withModules(moduleById) {
    externalModules = moduleById;
    return self;
}

function figwheelImportScript(uri, callback) {
    importJs(uri.toString(),
        function () {callback(true);},
        function () {callback(false);})
}

// Goog fixes
function shimBaseGoog(basePath) {
    console.info('Shimming goog functions.');
    goog.basePath = basePath + '/' + config.googBasePath;
    goog.global.FIGWHEEL_WEBSOCKET_CLASS = WebSocket;
    goog.global.FIGWHEEL_IMPORT_SCRIPT = figwheelImportScript;
    goog.writeScriptSrcNode = importJs;
    goog.writeScriptTag_ = function (src, optSourceText) {
        importJs(src);
        return true;
    };
}

self = {
    withModules: withModules,
    start: startApp
};

module.exports = self;
