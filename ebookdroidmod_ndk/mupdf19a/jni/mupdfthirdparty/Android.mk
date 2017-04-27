LOCAL_PATH:= $(call my-dir)

############################
include $(CLEAR_VARS)

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../mupdfcore/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/thirdparty/harfbuzz/src
LOCAL_C_INCLUDES += $(LOCAL_PATH)/thirdparty/jbig2dec
LOCAL_C_INCLUDES += $(LOCAL_PATH)/thirdparty/openjpeg/libopenjpeg 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/thirdparty/jpeg 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/thirdparty/mujs 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/thirdparty/zlib 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/thirdparty/freetype/include 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/scripts/freetype 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/scripts/jpeg 
LOCAL_C_INCLUDES += $(LOCAL_PATH)/scripts/openjpeg

LOCAL_EXPORT_C_INCLUDES :=
LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/thirdparty/harfbuzz/src
LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/thirdparty/jbig2dec
LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/thirdparty/openjpeg/libopenjpeg 
LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/thirdparty/jpeg 
LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/thirdparty/mujs 
LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/thirdparty/zlib 
LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/thirdparty/freetype/include 
LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/scripts/freetype 
LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/scripts/jpeg 
LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/scripts/openjpeg

LOCAL_CFLAGS := 
LOCAL_CFLAGS += -DFT2_BUILD_LIBRARY -DDARWIN_NO_CARBON -DHAVE_STDINT_H 
LOCAL_CFLAGS += -DOPJ_HAVE_STDINT_H 
LOCAL_CFLAGS += '-DFT_CONFIG_MODULES_H="slimftmodules.h"' 
LOCAL_CFLAGS += '-DFT_CONFIG_OPTIONS_H="slimftoptions.h"' 
LOCAL_CFLAGS += -Dhb_malloc_impl=hb_malloc -Dhb_calloc_impl=hb_calloc 
LOCAL_CFLAGS += -Dhb_realloc_impl=hb_realloc -Dhb_free_impl=hb_free 
LOCAL_CFLAGS += -DHAVE_OT -DHAVE_UCDN -DHB_NO_MT

ifdef MEMENTO
LOCAL_CFLAGS += -DMEMENTO -DMEMENTO_LEAKONLY
endif

LOCAL_CPP_EXTENSION := .cc

LOCAL_MODULE := mupdfthirdparty
LOCAL_SRC_FILES :=
LOCAL_SRC_FILES += thirdparty/mujs/one.c 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-blob.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-buffer.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-buffer-serialize.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-common.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-face.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-fallback-shape.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-font.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ft.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ot-font.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ot-layout.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ot-map.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ot-shape-complex-arabic.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ot-shape-complex-default.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ot-shape-complex-hangul.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ot-shape-complex-hebrew.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ot-shape-complex-indic-table.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ot-shape-complex-indic.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ot-shape-complex-myanmar.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ot-shape-complex-thai.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ot-shape-complex-tibetan.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ot-shape-complex-use-table.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ot-shape-complex-use.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ot-shape-fallback.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ot-shape-normalize.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ot-shape.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ot-tag.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-set.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-shape-plan.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-shape.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-shaper.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-ucdn.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-unicode.cc 
LOCAL_SRC_FILES += thirdparty/harfbuzz/src/hb-warning.cc 
LOCAL_SRC_FILES += thirdparty/jbig2dec/jbig2.c 
LOCAL_SRC_FILES += thirdparty/jbig2dec/jbig2_arith.c 
LOCAL_SRC_FILES += thirdparty/jbig2dec/jbig2_arith_iaid.c 
LOCAL_SRC_FILES += thirdparty/jbig2dec/jbig2_arith_int.c 
LOCAL_SRC_FILES += thirdparty/jbig2dec/jbig2_generic.c 
LOCAL_SRC_FILES += thirdparty/jbig2dec/jbig2_halftone.c 
LOCAL_SRC_FILES += thirdparty/jbig2dec/jbig2_huffman.c 
LOCAL_SRC_FILES += thirdparty/jbig2dec/jbig2_image.c 
LOCAL_SRC_FILES += thirdparty/jbig2dec/jbig2_metadata.c 
LOCAL_SRC_FILES += thirdparty/jbig2dec/jbig2_mmr.c 
LOCAL_SRC_FILES += thirdparty/jbig2dec/jbig2_page.c 
LOCAL_SRC_FILES += thirdparty/jbig2dec/jbig2_refinement.c 
LOCAL_SRC_FILES += thirdparty/jbig2dec/jbig2_segment.c 
LOCAL_SRC_FILES += thirdparty/jbig2dec/jbig2_symbol_dict.c 
LOCAL_SRC_FILES += thirdparty/jbig2dec/jbig2_text.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/bio.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/cidx_manager.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/cio.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/dwt.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/event.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/function_list.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/image.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/invert.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/j2k.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/jp2.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/mct.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/mqc.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/openjpeg.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/opj_clock.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/phix_manager.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/pi.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/ppix_manager.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/raw.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/t1.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/t1_generate_luts.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/t2.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/tcd.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/tgt.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/thix_manager.c 
LOCAL_SRC_FILES += thirdparty/openjpeg/libopenjpeg/tpix_manager.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jaricom.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jcomapi.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jdapimin.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jdapistd.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jdarith.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jdatadst.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jdatasrc.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jdcoefct.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jdcolor.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jddctmgr.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jdhuff.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jdinput.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jdmainct.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jdmarker.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jdmaster.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jdmerge.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jdpostct.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jdsample.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jdtrans.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jerror.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jfdctflt.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jfdctfst.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jfdctint.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jidctflt.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jidctfst.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jidctint.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jmemmgr.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jquant1.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jquant2.c 
LOCAL_SRC_FILES += thirdparty/jpeg/jutils.c 
LOCAL_SRC_FILES += thirdparty/zlib/adler32.c 
LOCAL_SRC_FILES += thirdparty/zlib/compress.c 
LOCAL_SRC_FILES += thirdparty/zlib/crc32.c 
LOCAL_SRC_FILES += thirdparty/zlib/deflate.c 
LOCAL_SRC_FILES += thirdparty/zlib/inffast.c 
LOCAL_SRC_FILES += thirdparty/zlib/inflate.c 
LOCAL_SRC_FILES += thirdparty/zlib/inftrees.c 
LOCAL_SRC_FILES += thirdparty/zlib/trees.c 
LOCAL_SRC_FILES += thirdparty/zlib/uncompr.c 
LOCAL_SRC_FILES += thirdparty/zlib/zutil.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/base/ftbase.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/base/ftbbox.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/base/ftbitmap.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/base/ftfntfmt.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/base/ftgasp.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/base/ftglyph.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/base/ftinit.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/base/ftstroke.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/base/ftsynth.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/base/ftsystem.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/base/fttype1.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/cff/cff.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/cid/type1cid.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/psaux/psaux.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/pshinter/pshinter.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/psnames/psnames.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/raster/raster.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/smooth/smooth.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/sfnt/sfnt.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/truetype/truetype.c 
LOCAL_SRC_FILES += thirdparty/freetype/src/type1/type1.c

#LOCAL_SRC_FILES := $(addprefix ../, $(LOCAL_SRC_FILES))

#LOCAL_STATIC_LIBRARIES := 

include $(BUILD_STATIC_LIBRARY)
