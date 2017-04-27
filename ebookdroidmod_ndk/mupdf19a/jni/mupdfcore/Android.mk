LOCAL_PATH:= $(call my-dir)
############################
include $(CLEAR_VARS)

LOCAL_CFLAGS += -Wall -Wno-maybe-uninitialized

ifeq ($(TARGET_ARCH),arm)
LOCAL_CFLAGS += -DARCH_ARM -DARCH_THUMB -DARCH_ARM_CAN_LOAD_UNALIGNED
endif

ifdef SUPPORT_GPROOF
LOCAL_CFLAGS += -DSUPPORT_GPROOF
endif

LOCAL_CFLAGS += -DAA_BITS=8

ifdef MEMENTO
LOCAL_CFLAGS += -DMEMENTO -DMEMENTO_LEAKONLY
endif

#ifdef SSL_BUILD
#LOCAL_CFLAGS += -DHAVE_OPENSSL
#endif

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../mupdfthirdparty/thirdparty/harfbuzz/src 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../mupdfthirdparty/thirdparty/jbig2dec 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../mupdfthirdparty/thirdparty/openjpeg/libopenjpeg 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../mupdfthirdparty/thirdparty/jpeg 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../mupdfthirdparty/thirdparty/mujs 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../mupdfthirdparty/thirdparty/zlib 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../mupdfthirdparty/thirdparty/freetype/include 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/source/fitz 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/source/pdf 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/source/xps 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/source/cbz 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/source/img 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/source/tiff 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/scripts/freetype 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/scripts/jpeg 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/scripts/openjpeg 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/generated 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/resources 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include 

#ifdef V8_BUILD
#LOCAL_C_INCLUDES += $(MY_ROOT)/thirdparty/$(V8)/include
#endif

#ifdef SSL_BUILD
#LOCAL_C_INCLUDES += $(MY_ROOT)/thirdparty/openssl/include
#endif

LOCAL_MODULE := mupdfcore

LOCAL_SRC_FILES := 

#LOCAL_SRC_FILES += source/fitz/*.c
LOCAL_SRC_FILES += source/fitz/bbox-device.c
LOCAL_SRC_FILES += source/fitz/bidi-std.c
LOCAL_SRC_FILES += source/fitz/bidi.c
LOCAL_SRC_FILES += source/fitz/bitmap.c
LOCAL_SRC_FILES += source/fitz/buffer.c
LOCAL_SRC_FILES += source/fitz/colorspace.c
LOCAL_SRC_FILES += source/fitz/compressed-buffer.c
LOCAL_SRC_FILES += source/fitz/context.c
LOCAL_SRC_FILES += source/fitz/crypt-aes.c
LOCAL_SRC_FILES += source/fitz/crypt-arc4.c
LOCAL_SRC_FILES += source/fitz/crypt-md5.c
LOCAL_SRC_FILES += source/fitz/crypt-sha2.c
LOCAL_SRC_FILES += source/fitz/device.c
LOCAL_SRC_FILES += source/fitz/document-all.c
LOCAL_SRC_FILES += source/fitz/document.c
LOCAL_SRC_FILES += source/fitz/draw-affine.c
LOCAL_SRC_FILES += source/fitz/draw-blend.c
LOCAL_SRC_FILES += source/fitz/draw-device.c
LOCAL_SRC_FILES += source/fitz/draw-edge.c
LOCAL_SRC_FILES += source/fitz/draw-glyph.c
LOCAL_SRC_FILES += source/fitz/draw-mesh.c
LOCAL_SRC_FILES += source/fitz/draw-paint.c
LOCAL_SRC_FILES += source/fitz/draw-path.c
LOCAL_SRC_FILES += source/fitz/draw-scale-simple.c
LOCAL_SRC_FILES += source/fitz/draw-unpack.c
LOCAL_SRC_FILES += source/fitz/error.c
LOCAL_SRC_FILES += source/fitz/filter-basic.c
LOCAL_SRC_FILES += source/fitz/filter-dct.c
LOCAL_SRC_FILES += source/fitz/filter-fax.c
LOCAL_SRC_FILES += source/fitz/filter-flate.c
LOCAL_SRC_FILES += source/fitz/filter-jbig2.c
LOCAL_SRC_FILES += source/fitz/filter-leech.c
LOCAL_SRC_FILES += source/fitz/filter-lzw.c
LOCAL_SRC_FILES += source/fitz/filter-predict.c
LOCAL_SRC_FILES += source/fitz/font.c
LOCAL_SRC_FILES += source/fitz/ftoa.c
LOCAL_SRC_FILES += source/fitz/function.c
LOCAL_SRC_FILES += source/fitz/geometry.c
LOCAL_SRC_FILES += source/fitz/getopt.c
LOCAL_SRC_FILES += source/fitz/glyph.c
LOCAL_SRC_FILES += source/fitz/halftone.c
LOCAL_SRC_FILES += source/fitz/harfbuzz.c
LOCAL_SRC_FILES += source/fitz/hash.c
LOCAL_SRC_FILES += source/fitz/image.c
LOCAL_SRC_FILES += source/fitz/jmemcust.c
LOCAL_SRC_FILES += source/fitz/link.c
LOCAL_SRC_FILES += source/fitz/list-device.c
LOCAL_SRC_FILES += source/fitz/load-bmp.c
LOCAL_SRC_FILES += source/fitz/load-gif.c
LOCAL_SRC_FILES += source/fitz/load-jpeg.c
LOCAL_SRC_FILES += source/fitz/load-jpx.c
LOCAL_SRC_FILES += source/fitz/load-jxr.c
LOCAL_SRC_FILES += source/fitz/load-png.c
LOCAL_SRC_FILES += source/fitz/load-tiff.c
LOCAL_SRC_FILES += source/fitz/memento.c
LOCAL_SRC_FILES += source/fitz/memory.c
LOCAL_SRC_FILES += source/fitz/noto.c
LOCAL_SRC_FILES += source/fitz/outline.c
LOCAL_SRC_FILES += source/fitz/output-pcl.c
LOCAL_SRC_FILES += source/fitz/output-ps.c
LOCAL_SRC_FILES += source/fitz/output-pwg.c
LOCAL_SRC_FILES += source/fitz/output.c
LOCAL_SRC_FILES += source/fitz/path.c
LOCAL_SRC_FILES += source/fitz/pixmap.c
LOCAL_SRC_FILES += source/fitz/pool.c
LOCAL_SRC_FILES += source/fitz/printf.c
LOCAL_SRC_FILES += source/fitz/separation.c
LOCAL_SRC_FILES += source/fitz/shade.c
LOCAL_SRC_FILES += source/fitz/stext-device.c
LOCAL_SRC_FILES += source/fitz/stext-output.c
LOCAL_SRC_FILES += source/fitz/stext-paragraph.c
LOCAL_SRC_FILES += source/fitz/stext-search.c
LOCAL_SRC_FILES += source/fitz/store.c
LOCAL_SRC_FILES += source/fitz/stream-open.c
LOCAL_SRC_FILES += source/fitz/stream-prog.c
LOCAL_SRC_FILES += source/fitz/stream-read.c
LOCAL_SRC_FILES += source/fitz/string.c
LOCAL_SRC_FILES += source/fitz/strtod.c
LOCAL_SRC_FILES += source/fitz/strtof.c
LOCAL_SRC_FILES += source/fitz/svg-device.c
LOCAL_SRC_FILES += source/fitz/tempfile.c
LOCAL_SRC_FILES += source/fitz/test-device.c
LOCAL_SRC_FILES += source/fitz/text.c
LOCAL_SRC_FILES += source/fitz/time.c
LOCAL_SRC_FILES += source/fitz/trace-device.c
LOCAL_SRC_FILES += source/fitz/transition.c
LOCAL_SRC_FILES += source/fitz/tree.c
LOCAL_SRC_FILES += source/fitz/ucdn.c
LOCAL_SRC_FILES += source/fitz/unzip.c
LOCAL_SRC_FILES += source/fitz/util.c
LOCAL_SRC_FILES += source/fitz/xml.c

#LOCAL_SRC_FILES += $(wildcard source/pdf/*.c)
LOCAL_SRC_FILES += source/pdf/pdf-annot-edit.c
LOCAL_SRC_FILES += source/pdf/pdf-annot.c
LOCAL_SRC_FILES += source/pdf/pdf-appearance.c
LOCAL_SRC_FILES += source/pdf/pdf-clean-file.c
LOCAL_SRC_FILES += source/pdf/pdf-clean.c
LOCAL_SRC_FILES += source/pdf/pdf-cmap-load.c
LOCAL_SRC_FILES += source/pdf/pdf-cmap-parse.c
LOCAL_SRC_FILES += source/pdf/pdf-cmap-table.c
LOCAL_SRC_FILES += source/pdf/pdf-cmap.c
LOCAL_SRC_FILES += source/pdf/pdf-colorspace.c
LOCAL_SRC_FILES += source/pdf/pdf-crypt.c
LOCAL_SRC_FILES += source/pdf/pdf-device.c
LOCAL_SRC_FILES += source/pdf/pdf-encoding.c
LOCAL_SRC_FILES += source/pdf/pdf-event.c
LOCAL_SRC_FILES += source/pdf/pdf-field.c
LOCAL_SRC_FILES += source/pdf/pdf-font.c
LOCAL_SRC_FILES += source/pdf/pdf-form.c
LOCAL_SRC_FILES += source/pdf/pdf-function.c
LOCAL_SRC_FILES += source/pdf/pdf-graft.c
LOCAL_SRC_FILES += source/pdf/pdf-image.c
LOCAL_SRC_FILES += source/pdf/pdf-interpret.c
LOCAL_SRC_FILES += source/pdf/pdf-lex.c
LOCAL_SRC_FILES += source/pdf/pdf-metrics.c
LOCAL_SRC_FILES += source/pdf/pdf-nametree.c
LOCAL_SRC_FILES += source/pdf/pdf-object.c
LOCAL_SRC_FILES += source/pdf/pdf-op-buffer.c
LOCAL_SRC_FILES += source/pdf/pdf-op-filter.c
LOCAL_SRC_FILES += source/pdf/pdf-op-run.c
LOCAL_SRC_FILES += source/pdf/pdf-outline.c
LOCAL_SRC_FILES += source/pdf/pdf-page.c
LOCAL_SRC_FILES += source/pdf/pdf-parse.c
LOCAL_SRC_FILES += source/pdf/pdf-pattern.c
LOCAL_SRC_FILES += source/pdf/pdf-pkcs7.c
LOCAL_SRC_FILES += source/pdf/pdf-repair.c
LOCAL_SRC_FILES += source/pdf/pdf-resources.c
LOCAL_SRC_FILES += source/pdf/pdf-run.c
LOCAL_SRC_FILES += source/pdf/pdf-shade.c
LOCAL_SRC_FILES += source/pdf/pdf-store.c
LOCAL_SRC_FILES += source/pdf/pdf-stream.c
LOCAL_SRC_FILES += source/pdf/pdf-type3.c
LOCAL_SRC_FILES += source/pdf/pdf-unicode.c
LOCAL_SRC_FILES += source/pdf/pdf-write.c
LOCAL_SRC_FILES += source/pdf/pdf-xobject.c
LOCAL_SRC_FILES += source/pdf/pdf-xref.c

#LOCAL_SRC_FILES += $(wildcard source/xps/*.c)
LOCAL_SRC_FILES += source/xps/xps-common.c
LOCAL_SRC_FILES += source/xps/xps-doc.c
LOCAL_SRC_FILES += source/xps/xps-glyphs.c
LOCAL_SRC_FILES += source/xps/xps-gradient.c
LOCAL_SRC_FILES += source/xps/xps-image.c
LOCAL_SRC_FILES += source/xps/xps-link.c
LOCAL_SRC_FILES += source/xps/xps-outline.c
LOCAL_SRC_FILES += source/xps/xps-path.c
LOCAL_SRC_FILES += source/xps/xps-resource.c
LOCAL_SRC_FILES += source/xps/xps-tile.c
LOCAL_SRC_FILES += source/xps/xps-util.c
LOCAL_SRC_FILES += source/xps/xps-zip.c

#LOCAL_SRC_FILES += $(wildcard source/cbz/*.c)
LOCAL_SRC_FILES += source/cbz/mucbz.c
LOCAL_SRC_FILES += source/cbz/muimg.c
LOCAL_SRC_FILES += source/cbz/mutiff.c

#LOCAL_SRC_FILES += $(wildcard source/gprf/*.c)
LOCAL_SRC_FILES += source/gprf/gprf-doc.c
LOCAL_SRC_FILES += source/gprf/gprf-skeleton.c

#LOCAL_SRC_FILES += $(wildcard source/html/*.c)
LOCAL_SRC_FILES += source/html/css-apply.c
LOCAL_SRC_FILES += source/html/css-parse.c
LOCAL_SRC_FILES += source/html/epub-doc.c
LOCAL_SRC_FILES += source/html/html-doc.c
LOCAL_SRC_FILES += source/html/html-font.c
LOCAL_SRC_FILES += source/html/html-layout.c

#LOCAL_SRC_FILES += $(wildcard generated/*.c)
LOCAL_SRC_FILES += generated/CharisSIL-B.c
LOCAL_SRC_FILES += generated/CharisSIL-BI.c
LOCAL_SRC_FILES += generated/CharisSIL-I.c
LOCAL_SRC_FILES += generated/CharisSIL-R.c
LOCAL_SRC_FILES += generated/Dingbats.c
LOCAL_SRC_FILES += generated/DroidSansFallback.c
LOCAL_SRC_FILES += generated/DroidSansFallbackFull.c
LOCAL_SRC_FILES += generated/NimbusMono-Bold.c
LOCAL_SRC_FILES += generated/NimbusMono-BoldOblique.c
LOCAL_SRC_FILES += generated/NimbusMono-Oblique.c
LOCAL_SRC_FILES += generated/NimbusMono-Regular.c
LOCAL_SRC_FILES += generated/NimbusRomNo9L-Med.c
LOCAL_SRC_FILES += generated/NimbusRomNo9L-MedIta.c
LOCAL_SRC_FILES += generated/NimbusRomNo9L-Reg.c
LOCAL_SRC_FILES += generated/NimbusRomNo9L-RegIta.c
LOCAL_SRC_FILES += generated/NimbusSanL-Bol.c
LOCAL_SRC_FILES += generated/NimbusSanL-BolIta.c
LOCAL_SRC_FILES += generated/NimbusSanL-Reg.c
LOCAL_SRC_FILES += generated/NimbusSanL-RegIta.c
LOCAL_SRC_FILES += generated/NotoEmoji-Regular.c
LOCAL_SRC_FILES += generated/NotoKufiArabic-Regular.c
LOCAL_SRC_FILES += generated/NotoNaskhArabic-Regular.c
LOCAL_SRC_FILES += generated/NotoNastaliqUrdu-Regular.c
LOCAL_SRC_FILES += generated/NotoSans-Regular.c
LOCAL_SRC_FILES += generated/NotoSansArmenian-Regular.c
LOCAL_SRC_FILES += generated/NotoSansAvestan-Regular.c
LOCAL_SRC_FILES += generated/NotoSansBalinese-Regular.c
LOCAL_SRC_FILES += generated/NotoSansBamum-Regular.c
LOCAL_SRC_FILES += generated/NotoSansBatak-Regular.c
LOCAL_SRC_FILES += generated/NotoSansBengali-Regular.c
LOCAL_SRC_FILES += generated/NotoSansBrahmi-Regular.c
LOCAL_SRC_FILES += generated/NotoSansBuginese-Regular.c
LOCAL_SRC_FILES += generated/NotoSansBuhid-Regular.c
LOCAL_SRC_FILES += generated/NotoSansCanadianAboriginal-Regular.c
LOCAL_SRC_FILES += generated/NotoSansCarian-Regular.c
LOCAL_SRC_FILES += generated/NotoSansCham-Regular.c
LOCAL_SRC_FILES += generated/NotoSansCherokee-Regular.c
LOCAL_SRC_FILES += generated/NotoSansCoptic-Regular.c
LOCAL_SRC_FILES += generated/NotoSansCuneiform-Regular.c
LOCAL_SRC_FILES += generated/NotoSansCypriot-Regular.c
LOCAL_SRC_FILES += generated/NotoSansDeseret-Regular.c
LOCAL_SRC_FILES += generated/NotoSansDevanagari-Regular.c
LOCAL_SRC_FILES += generated/NotoSansEgyptianHieroglyphs-Regular.c
LOCAL_SRC_FILES += generated/NotoSansEthiopic-Regular.c
LOCAL_SRC_FILES += generated/NotoSansGeorgian-Regular.c
LOCAL_SRC_FILES += generated/NotoSansGlagolitic-Regular.c
LOCAL_SRC_FILES += generated/NotoSansGothic-Regular.c
LOCAL_SRC_FILES += generated/NotoSansGujarati-Regular.c
LOCAL_SRC_FILES += generated/NotoSansGurmukhi-Regular.c
LOCAL_SRC_FILES += generated/NotoSansHanunoo-Regular.c
LOCAL_SRC_FILES += generated/NotoSansHebrew-Regular.c
LOCAL_SRC_FILES += generated/NotoSansImperialAramaic-Regular.c
LOCAL_SRC_FILES += generated/NotoSansInscriptionalPahlavi-Regular.c
LOCAL_SRC_FILES += generated/NotoSansInscriptionalParthian-Regular.c
LOCAL_SRC_FILES += generated/NotoSansJavanese-Regular.c
LOCAL_SRC_FILES += generated/NotoSansKaithi-Regular.c
LOCAL_SRC_FILES += generated/NotoSansKannada-Regular.c
LOCAL_SRC_FILES += generated/NotoSansKayahLi-Regular.c
LOCAL_SRC_FILES += generated/NotoSansKharoshthi-Regular.c
LOCAL_SRC_FILES += generated/NotoSansKhmer-Regular.c
LOCAL_SRC_FILES += generated/NotoSansLao-Regular.c
LOCAL_SRC_FILES += generated/NotoSansLepcha-Regular.c
LOCAL_SRC_FILES += generated/NotoSansLimbu-Regular.c
LOCAL_SRC_FILES += generated/NotoSansLinearB-Regular.c
LOCAL_SRC_FILES += generated/NotoSansLisu-Regular.c
LOCAL_SRC_FILES += generated/NotoSansLycian-Regular.c
LOCAL_SRC_FILES += generated/NotoSansLydian-Regular.c
LOCAL_SRC_FILES += generated/NotoSansMalayalam-Regular.c
LOCAL_SRC_FILES += generated/NotoSansMandaic-Regular.c
LOCAL_SRC_FILES += generated/NotoSansMeeteiMayek-Regular.c
LOCAL_SRC_FILES += generated/NotoSansMongolian-Regular.c
LOCAL_SRC_FILES += generated/NotoSansMyanmar-Regular.c
LOCAL_SRC_FILES += generated/NotoSansNewTaiLue-Regular.c
LOCAL_SRC_FILES += generated/NotoSansNKo-Regular.c
LOCAL_SRC_FILES += generated/NotoSansOgham-Regular.c
LOCAL_SRC_FILES += generated/NotoSansOlChiki-Regular.c
LOCAL_SRC_FILES += generated/NotoSansOldItalic-Regular.c
LOCAL_SRC_FILES += generated/NotoSansOldPersian-Regular.c
LOCAL_SRC_FILES += generated/NotoSansOldSouthArabian-Regular.c
LOCAL_SRC_FILES += generated/NotoSansOldTurkic-Regular.c
LOCAL_SRC_FILES += generated/NotoSansOriya-Regular.c
LOCAL_SRC_FILES += generated/NotoSansOsmanya-Regular.c
LOCAL_SRC_FILES += generated/NotoSansPhagsPa-Regular.c
LOCAL_SRC_FILES += generated/NotoSansPhoenician-Regular.c
LOCAL_SRC_FILES += generated/NotoSansRejang-Regular.c
LOCAL_SRC_FILES += generated/NotoSansRunic-Regular.c
LOCAL_SRC_FILES += generated/NotoSansSamaritan-Regular.c
LOCAL_SRC_FILES += generated/NotoSansSaurashtra-Regular.c
LOCAL_SRC_FILES += generated/NotoSansShavian-Regular.c
LOCAL_SRC_FILES += generated/NotoSansSinhala-Regular.c
LOCAL_SRC_FILES += generated/NotoSansSundanese-Regular.c
LOCAL_SRC_FILES += generated/NotoSansSylotiNagri-Regular.c
LOCAL_SRC_FILES += generated/NotoSansSymbols-Regular.c
LOCAL_SRC_FILES += generated/NotoSansSyriacEastern-Regular.c
LOCAL_SRC_FILES += generated/NotoSansSyriacEstrangela-Regular.c
LOCAL_SRC_FILES += generated/NotoSansSyriacWestern-Regular.c
LOCAL_SRC_FILES += generated/NotoSansTagalog-Regular.c
LOCAL_SRC_FILES += generated/NotoSansTagbanwa-Regular.c
LOCAL_SRC_FILES += generated/NotoSansTaiLe-Regular.c
LOCAL_SRC_FILES += generated/NotoSansTaiTham-Regular.c
LOCAL_SRC_FILES += generated/NotoSansTaiViet-Regular.c
LOCAL_SRC_FILES += generated/NotoSansTamil-Regular.c
LOCAL_SRC_FILES += generated/NotoSansTelugu-Regular.c
LOCAL_SRC_FILES += generated/NotoSansThaana-Regular.c
LOCAL_SRC_FILES += generated/NotoSansThai-Regular.c
LOCAL_SRC_FILES += generated/NotoSansTibetan-Regular.c
LOCAL_SRC_FILES += generated/NotoSansTifinagh-Regular.c
LOCAL_SRC_FILES += generated/NotoSansUgaritic-Regular.c
LOCAL_SRC_FILES += generated/NotoSansVai-Regular.c
LOCAL_SRC_FILES += generated/NotoSansYi-Regular.c
LOCAL_SRC_FILES += generated/NotoSerif-Regular.c
LOCAL_SRC_FILES += generated/NotoSerifArmenian-Regular.c
LOCAL_SRC_FILES += generated/NotoSerifBengali-Regular.c
LOCAL_SRC_FILES += generated/NotoSerifGeorgian-Regular.c
LOCAL_SRC_FILES += generated/NotoSerifGujarati-Regular.c
LOCAL_SRC_FILES += generated/NotoSerifKannada-Regular.c
LOCAL_SRC_FILES += generated/NotoSerifKhmer-Regular.c
LOCAL_SRC_FILES += generated/NotoSerifLao-Regular.c
LOCAL_SRC_FILES += generated/NotoSerifMalayalam-Regular.c
LOCAL_SRC_FILES += generated/NotoSerifTamil-Regular.c
LOCAL_SRC_FILES += generated/NotoSerifTelugu-Regular.c
LOCAL_SRC_FILES += generated/NotoSerifThai-Regular.c
LOCAL_SRC_FILES += generated/StandardSymL.c

LOCAL_SRC_FILES += source/pdf/js/pdf-js.c

#ifdef SUPPORT_GPROOF
#LOCAL_SHARED_LIBRARIES := gsso
#endif

LOCAL_LDLIBSLOCAL_SRC_FILES += source/html/:= -lm -llog -ljnigraphics

#LOCAL_SRC_FILES := $(addprefix ../, $(LOCAL_SRC_FILES))

LOCAL_STATIC_LIBRARIES := mupdfthirdparty

include $(BUILD_STATIC_LIBRARY)
