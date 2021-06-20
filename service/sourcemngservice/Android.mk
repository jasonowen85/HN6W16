LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

src_dirs = src/main/java
#aidl_dirs = src/main/aidl

LOCAL_MODULE_TAGS := optional

#LOCAL_MODULE_TAGS := sourcemngservice
#LOCAL_JAVA_LIBRARIES := sourceManagerProxy
LOCAL_STATIC_JAVA_LIBRARIES := sourceManagerProxy
LOCAL_STATIC_JAVA_LIBRARIES += serviceProxySource
LOCAL_STATIC_JAVA_LIBRARIES += daemonProxySourceMng
LOCAL_STATIC_JAVA_LIBRARIES += audioBspProxySourceMng
LOCAL_STATIC_JAVA_LIBRARIES += jsonSourceMng
LOCAL_STATIC_JAVA_LIBRARIES += keyEventSourceMng
LOCAL_STATIC_JAVA_LIBRARIES += shareDataSourceMng
LOCAL_STATIC_JAVA_LIBRARIES += systemServiceSourceMng
LOCAL_STATIC_JAVA_LIBRARIES += deviceMngSourceMng
#LOCAL_STATIC_JAVA_LIBRARIES += systemDialogSourceMng
LOCAL_STATIC_JAVA_LIBRARIES += mcuCommSourceMng
LOCAL_STATIC_JAVA_LIBRARIES += adayoSourceSourceMng
LOCAL_STATIC_JAVA_LIBRARIES += servicecenterSourceMng
LOCAL_STATIC_JAVA_LIBRARIES += commonToolsSourceMng
LOCAL_STATIC_JAVA_LIBRARIES += mediaScannerSourceMng
LOCAL_STATIC_JAVA_LIBRARIES += settingSourceMng
LOCAL_STATIC_JAVA_LIBRARIES += client_sdk_1.3.7
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-appcompat \
    android-support-v4 \

LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs))
					
#LOCAL_AIDL_INCLUDES  += $(LOCAL_PATH)/$(aidl_dirs)					
					
LOCAL_MANIFEST_FILE := src/main/AndroidManifest.xml

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/src/main/res 
LOCAL_RESOURCE_DIR += frameworks/support/v7/appcompat/res

LOCAL_AAPT_FLAGS += --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.appcompat

LOCAL_REQUIRED_MODULES := libymu836

LOCAL_CERTIFICATE := platform
#LOCAL_DEX_PREOPT := nostripping
#LOCAL_SDK_VERSION := current 
LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_PROGUARD_ENABLED := disabled
#LOCAL_PROGUARD_FLAG_FILES := proguard.cfg
#LOCAL_MODULE := sourcemngservice  
LOCAL_PACKAGE_NAME := SourceMngService  

 LOCAL_DEX_PREOPT = false

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)  
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=sourceManagerProxy:../../../libs/jar/sourcemng-proxy.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=serviceProxySource:../../../libs/jar/service-proxy.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=daemonProxySourceMng:../../../libs/jar/daemon-proxy.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=audioBspProxySourceMng:../../../libs/jar/audiodsp-proxy.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=jsonSourceMng:../../../libs/jar/gson-2.7.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=client_sdk_1.3.7:../../../libs/jar/client_sdk_1.3.7.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=keyEventSourceMng:../../../libs/jar/keyevent-proxy.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=shareDataSourceMng:../../../libs/jar/sharedata-proxy.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=systemServiceSourceMng:../../../libs/jar/systemservice-proxy.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=deviceMngSourceMng:../../../libs/jar/devmgr-proxy.jar
#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=systemDialogSourceMng:../../../../Application/SystemDialog/libs/systemdialog-proxy.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=mcuCommSourceMng:../../../libs/jar/mcucomm-proxy.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=adayoSourceSourceMng:../../../libs/jar/adayosource-proxy.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=servicecenterSourceMng:../../../libs/jar/servicecenterproxy.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=commonToolsSourceMng:../../../libs/jar/commontools.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=settingSourceMng:../../../libs/jar/SettingsService-proxy.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=mediaScannerSourceMng:../../../libs/jar/mediascanner-proxy.jar

include $(BUILD_MULTI_PREBUILT)  

#LOCAL_MANIFEST_FILE := $(LOCAL_PATH)/src/main/AndroidManifest.xml 
