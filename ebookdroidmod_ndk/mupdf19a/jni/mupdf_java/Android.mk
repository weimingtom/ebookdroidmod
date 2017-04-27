LOCAL_PATH:= $(call my-dir)

############################
include $(CLEAR_VARS)

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../mupdfcore/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../mupdfcore/source/fitz
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../mupdfcore/source/pdf
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../mupdfcore/platform/java

LOCAL_CFLAGS := -DHAVE_ANDROID
LOCAL_MODULE := mupdf_java

LOCAL_SRC_FILES := mupdf.c
#LOCAL_SRC_FILES += pdfdroidbridge.c 

LOCAL_STATIC_LIBRARIES := mupdfcore

LOCAL_LDLIBS    := -lm -llog -ljnigraphics -lz

#mupdf/mupdf.c
#LOCAL_LDLIBS    := -lm -llog -ljnigraphics
#ebookdroid/ebookdroidjni.c
#LOCAL_LDLIBS := -llog -lz

#ifdef SSL_BUILD
#LOCAL_LDLIBS	+= -L$(MUPDF_ROOT)/thirdparty/openssl/android -lcrypto -lssl
#endif

include $(BUILD_SHARED_LIBRARY)
############################
