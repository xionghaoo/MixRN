# MixRN
> ReactNative和原生混合项目

## 开发
1. 启动development server
```
yarn start
```
2. 启动app

## Reload
#### 真机打开menu菜单方法
1. 摇晃手机
2. adb命令
```
adb shell input keyevent 82
```

adb键盘相关命令
[adb命令](https://stackoverflow.com/questions/7789826/adb-shell-input-events)

## ReactNative离线Bundle打包
```
react-native bundle --platform android --dev false --entry-file index.js --bundle-output app/src/main/assets/index.android.bundle --assets-dest app/src/main/res/
```
命令说明
```
  react-native bundle [options]:
  builds the javascript bundle for offline use

  Options:

    --entry-file <path>                Path to the root JS file, either absolute or relative to JS root
    --platform [string]                Either "ios" or "android" (default: "ios")
    --transformer [string]             Specify a custom transformer to be used
    --dev [boolean]                    If false, warnings are disabled and the bundle is minified (default: true)
    --minify [boolean]                 Allows overriding whether bundle is minified. This defaults to false if dev is true, and true if dev is false. Disabling minification can be useful for speeding up production builds for testing purposes.
    --bundle-output <string>           File name where to store the resulting bundle, ex. /tmp/groups.bundle
    --bundle-encoding [string]         Encoding the bundle should be written in (https://nodejs.org/api/buffer.html#buffer_buffer). (default: "utf8")
    --max-workers [number]             Specifies the maximum number of workers the worker-pool will spawn for transforming files. This defaults to the number of the cores available on your machine.
    --sourcemap-output [string]        File name where to store the sourcemap file for resulting bundle, ex. /tmp/groups.map
    --sourcemap-sources-root [string]  Path to make sourcemap's sources entries relative to, ex. /root/dir
    --sourcemap-use-absolute-path      Report SourceMapURL using its full path
    --assets-dest [string]             Directory name where to store assets referenced in the bundle
    --verbose                          Enables logging
    --reset-cache                      Removes cached files
    --read-global-cache                Try to fetch transformed JS code from the global cache, if configured.
    --config [string]                  Path to the CLI configuration file
    -h, --help                         output usage information
```

## 常见问题
##### 1、真连不上development server
运行以下命令
```
adb reverse tcp:8081 tcp:8081
```