LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

src_dirs = src/main/java
aidl_dirs = src/main/aidl

LOCAL_MODULE_TAGS := sourcemng-proxy
LOCAL_JAVA_LIBRARIES := serviceProxy
#LOCAL_STATIC_JAVA_LIBRARIES += pinyintool

LOCAL_SRC_FILES := $(call all-java-files-under, $(src_dirs)) \
					$(call all-Iaidl-files-under, $(aidl_dirs))
					
LOCAL_AIDL_INCLUDES  += $(LOCAL_PATH)/$(aidl_dirs)					
					
#LOCAL_SRC_FILES += $(call find-subdir-files, src/main/aidl/com/adayo/service/daemonproxy -name "*.aidl" -and -not -name ".*")
					
LOCAL_MANIFEST_FILE := src/main/AndroidManifest.xml

LOCAL_CERTIFICATE := platform
#LOCAL_SDK_VERSION := current 
#LOCAL_PROGUARD_ENABLED := disabled
#LOCAL_PROGUARD_FLAG_FILES := proguard.cfg
LOCAL_MODULE := sourcemng-proxy
include $(BUILD_JAVA_LIBRARY)

include $(CLEAR_VARS)  
#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES +=pinyintool:libs/pinyin4j-2.5.0.jar

#include $(BUILD_MULTI_PREBUILT)  

#LOCAL_MANIFEST_FILE := $(LOCAL_PATH)/src/main/AndroidManifest.xml 
