set apk_path="D:\sign\sign8.1"
set apk_name=SourceMngService
set install=system/app
java -jar %apk_path%\signapk.jar %apk_path%\platform.x509.pem %apk_path%\platform.pk8  build\outputs\apk\release\sourcemngservice-release-unsigned.apk %apk_name%.apk
adb root
adb remount
::adb shell rm -rf /%install%/%apk_name%/oat
::adb shell rm  /%install%/%apk_name%/%apk_name%.apk
::adb shell mkdir /%install%/%apk_name%/
dir .\build\outputs\apk\release\sourcemngservice-release-unsigned.apk
adb push %apk_name%.apk /%install%/%apk_name%/
::adb install -r -d %apk_name%.apk
