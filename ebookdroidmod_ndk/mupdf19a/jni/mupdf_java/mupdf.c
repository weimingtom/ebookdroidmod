#include <jni.h>
#include <time.h>
#include <pthread.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#ifdef NDK_PROFILER
#include "prof.h"
#endif

#include "mupdf/fitz.h"
#include "mupdf/pdf.h"

#define JNI_FN(A) Java_com_artifex_mupdfdemo_ ## A
#define PACKAGENAME "com/artifex/mupdfdemo"

#define LOG_TAG "libmupdf"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGT(...) __android_log_print(ANDROID_LOG_INFO,"alert",__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

/* Enable to log rendering times (render each frame 100 times and time) */
#undef TIME_DISPLAY_LIST

#define MAX_SEARCH_HITS (500)
#define NUM_CACHE (3)
#define STRIKE_HEIGHT (0.375f)
#define UNDERLINE_HEIGHT (0.075f)
#define LINE_THICKNESS (0.07f)
#define INK_THICKNESS (4.0f)
//#define SMALL_FLOAT (0.00001)
//#define PROOF_RESOLUTION (300)

typedef struct rect_node_s rect_node;

struct rect_node_s
{
	fz_rect rect;
	rect_node *next;
};

typedef struct
{
	int number;
	int width;
	int height;
	fz_rect media_box;
	fz_rect rectTemp; //FIXME:新增
	fz_page *page;
	rect_node *changed_rects;
	rect_node *hq_changed_rects;
	fz_display_list *page_list;
	fz_display_list *annot_list;
} page_cache;

typedef struct globals_s globals;

struct globals_s
{
	fz_colorspace *colorspace;
	fz_document *doc;
	int resolution;
	fz_context *ctx;
	fz_rect *hit_bbox;
	int current;
	char *current_path;

	page_cache pages[NUM_CACHE];

	//int alerts_initialised;
	// fin_lock and fin_lock2 are used during shutdown. The two waiting tasks
	// show_alert and waitForAlertInternal respectively take these locks while
	// waiting. During shutdown, the conditions are signaled and then the fin_locks
	// are taken momentarily to ensure the blocked threads leave the controlled
	// area of code before the mutexes and condition variables are destroyed.
	//pthread_mutex_t fin_lock;
	//pthread_mutex_t fin_lock2;
	// alert_lock is the main lock guarding the variables directly below.
	//pthread_mutex_t alert_lock;
	// Flag indicating if the alert system is active. When not active, both
	// show_alert and waitForAlertInternal return immediately.
	//int alerts_active;
	// Pointer to the alert struct passed in by show_alert, and valid while
	// show_alert is blocked.
	//pdf_alert_event *current_alert;
	// Flag and condition varibles to signal a request is present and a reply
	// is present, respectively. The condition variables alone are not sufficient
	// because of the pthreads permit spurious signals.
	//int alert_request;
	//int alert_reply;
	//pthread_cond_t alert_request_cond;
	//pthread_cond_t alert_reply_cond;

	// For the buffer reading mode, we need to implement stream reading, which
	// needs access to the following.
	JNIEnv *env;
	jclass thiz;
};

static jfieldID global_fid;
static jfieldID buffer_fid;

// Do our best to avoid casting warnings.
#define CAST(type, var) (type)pointer_cast(var)

static inline void *pointer_cast(jlong l)
{
	return (void *)(intptr_t)l;
}

static inline jlong jlong_cast(void *p)
{
	return (jlong)(intptr_t)p;
}

static void drop_changed_rects(fz_context *ctx, rect_node **nodePtr)
{
	rect_node *node = *nodePtr;
	while (node)
	{
		rect_node *tnode = node;
		node = node->next;
		fz_free(ctx, tnode);
	}

	*nodePtr = NULL;
}

static void drop_page_cache(globals *glo, page_cache *pc)
{
	fz_context *ctx = glo->ctx;
	fz_document *doc = glo->doc;

	LOGI("Drop page %d", pc->number);
	fz_drop_display_list(ctx, pc->page_list);
	pc->page_list = NULL;
	fz_drop_display_list(ctx, pc->annot_list);
	pc->annot_list = NULL;
	fz_drop_page(ctx, pc->page);
	pc->page = NULL;
	drop_changed_rects(ctx, &pc->changed_rects);
	drop_changed_rects(ctx, &pc->hq_changed_rects);
}

static void dump_annotation_display_lists(globals *glo)
{
	fz_context *ctx = glo->ctx;
	int i;

	for (i = 0; i < NUM_CACHE; i++) {
		fz_drop_display_list(ctx, glo->pages[i].annot_list);
		glo->pages[i].annot_list = NULL;
	}
}

// Should only be called from the single background AsyncTask thread
static globals *get_globals(JNIEnv *env, jobject thiz)
{
	globals *glo = CAST(globals *, (*env)->GetLongField(env, thiz, global_fid));
	if (glo != NULL)
	{
		glo->env = env;
		glo->thiz = thiz;
	}
	return glo;
}

// May be called from any thread, provided the values of glo->env and glo->thiz
// are not used.
static globals *get_globals_any_thread(JNIEnv *env, jobject thiz)
{
	return (globals *)(intptr_t)((*env)->GetLongField(env, thiz, global_fid));
}

JNIEXPORT jlong JNICALL
JNI_FN(MuPDFCore_openFile)(JNIEnv * env, jobject thiz, jstring jfilename)
{
	const char *filename;
	globals *glo;
	fz_context *ctx;
	jclass clazz;

#ifdef NDK_PROFILER
	//monstartup("libmupdf_java.so");
#endif

	clazz = (*env)->GetObjectClass(env, thiz);
	global_fid = (*env)->GetFieldID(env, clazz, "globals", "J");

	glo = calloc(1, sizeof(*glo));
	if (glo == NULL)
		return 0;
	glo->resolution = 160;
	//glo->alerts_initialised = 0;

#ifdef DEBUG
	/* Try and send stdout/stderr to file in debug builds. This
	 * path may not work on all platforms, but it works on the
	 * LG G3, and it's no worse than not redirecting it anywhere
	 * on anything else. */
	//freopen("/storage/emulated/0/Download/stdout.txt", "a", stdout);
	//freopen("/storage/emulated/0/Download/stderr.txt", "a", stderr);
#endif

	filename = (*env)->GetStringUTFChars(env, jfilename, NULL);
	if (filename == NULL)
	{
		LOGE("Failed to get filename");
		free(glo);
		return 0;
	}

	/* 128 MB store for low memory devices. Tweak as necessary. */
	glo->ctx = ctx = fz_new_context(NULL, NULL, 128 << 20); //FIXME:ebookdroid is 64 << 20
	if (!ctx)
	{
		LOGE("Failed to initialise context");
		(*env)->ReleaseStringUTFChars(env, jfilename, filename);
		free(glo);
		return 0;
	}

	fz_register_document_handlers(ctx);

	glo->doc = NULL;
	fz_try(ctx)
	{
		glo->colorspace = fz_device_rgb(ctx);

		LOGI("Opening document...");
		fz_try(ctx)
		{
			glo->current_path = fz_strdup(ctx, (char *)filename);
			glo->doc = fz_open_document(ctx, (char *)filename);
			//alerts_init(glo);
		}
		fz_catch(ctx)
		{
			fz_throw(ctx, FZ_ERROR_GENERIC, "Cannot open document: '%s'", filename);
		}
		LOGI("Done!");
	}
	fz_catch(ctx)
	{
		LOGE("Failed: %s", ctx->error->message);
		fz_drop_document(ctx, glo->doc);
		glo->doc = NULL;
		fz_drop_context(ctx);
		glo->ctx = NULL;
		free(glo);
		glo = NULL;
	}

	(*env)->ReleaseStringUTFChars(env, jfilename, filename);

	return jlong_cast(glo);
}

JNIEXPORT int JNICALL
JNI_FN(MuPDFCore_countPagesInternal)(JNIEnv *env, jobject thiz)
{
	globals *glo = get_globals(env, thiz);
	fz_context *ctx = glo->ctx;
	int count = 0;

	fz_try(ctx)
	{
		count = fz_count_pages(ctx, glo->doc);
	}
	fz_catch(ctx)
	{
		LOGE("exception while counting pages: %s", ctx->error->message);
	}
	return count;
}

JNIEXPORT jstring JNICALL
JNI_FN(MuPDFCore_fileFormatInternal)(JNIEnv * env, jobject thiz)
{
	char info[64];
	globals *glo = get_globals(env, thiz);
	fz_context *ctx = glo->ctx;

	fz_lookup_metadata(ctx, glo->doc, FZ_META_FORMAT, info, sizeof(info));

	return (*env)->NewStringUTF(env, info);
}

JNIEXPORT jboolean JNICALL
JNI_FN(MuPDFCore_isUnencryptedPDFInternal)(JNIEnv * env, jobject thiz)
{
	globals *glo = get_globals_any_thread(env, thiz);
	if (glo == NULL)
		return JNI_FALSE;

	fz_context *ctx = glo->ctx;
	pdf_document *idoc = pdf_specifics(ctx, glo->doc);
	if (idoc == NULL)
		return JNI_FALSE; // Not a PDF

	int cryptVer = pdf_crypt_version(ctx, idoc);
	return (cryptVer == 0) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
JNI_FN(MuPDFCore_gotoPageInternal)(JNIEnv *env, jobject thiz, int page)
{
	int i;
	int furthest;
	int furthest_dist = -1;
	float zoom;
	fz_matrix ctm;
	fz_irect bbox;
	page_cache *pc;
	globals *glo = get_globals(env, thiz);
	if (glo == NULL)
		return;
	fz_context *ctx = glo->ctx;

	for (i = 0; i < NUM_CACHE; i++)
	{
		if (glo->pages[i].page != NULL && glo->pages[i].number == page)
		{
			/* The page is already cached */
			glo->current = i;
			return;
		}

		if (glo->pages[i].page == NULL)
		{
			/* cache record unused, and so a good one to use */
			furthest = i;
			furthest_dist = INT_MAX;
		}
		else
		{
			int dist = abs(glo->pages[i].number - page);

			/* Further away - less likely to be needed again */
			if (dist > furthest_dist)
			{
				furthest_dist = dist;
				furthest = i;
			}
		}
	}

	glo->current = furthest;
	pc = &glo->pages[glo->current];

	drop_page_cache(glo, pc);

	/* In the event of an error, ensure we give a non-empty page */
	pc->width = 100;
	pc->height = 100;

	pc->number = page;
	LOGI("Goto page %d...", page);
	fz_try(ctx)
	{
		fz_rect rect;
		LOGI("Load page %d", pc->number);
		pc->page = fz_load_page(ctx, glo->doc, pc->number);
		zoom = glo->resolution / 72;
		fz_bound_page(ctx, pc->page, &pc->media_box);
		fz_scale(&ctm, zoom, zoom);
		rect = pc->media_box;
		LOGE("==================================MuPDFCore_gotoPageInternal MuPDFCore_getBoundsInternal %f, %f, %f, %f, %d, %d", rect.x0, rect.y0, rect.x1, rect.y1, pc->width, pc->height);
		pc->rectTemp = pc->media_box;
		fz_round_rect(&bbox, fz_transform_rect(&rect, &ctm));
		pc->width = bbox.x1-bbox.x0;
		pc->height = bbox.y1-bbox.y0;
	}
	fz_catch(ctx)
	{
		LOGE("cannot make displaylist from page %d", pc->number);
	}
}

JNIEXPORT void JNICALL
JNI_FN(MuPDFCore_getBoundsInternal)(JNIEnv *env, jobject thiz, jfloatArray bounds)
{
    globals *glo = get_globals(env, thiz);
    page_cache *pc = &glo->pages[glo->current];
    jfloat *bbox = (*env)->GetPrimitiveArrayCritical(env, bounds, 0);
	LOGE("==================================MuPDFCore_getBoundsInternal 001");
    if (!bbox)
        return;
    fz_rect rect;
    rect = pc->rectTemp;
    bbox[0] = rect.x0;
    bbox[1] = rect.y0;
    bbox[2] = rect.x1;
    bbox[3] = rect.y1;
	LOGE("==================================MuPDFCore_getBoundsInternal 002 %f, %f, %f, %f, %d, %d", bbox[0], bbox[1], bbox[2], bbox[3], pc->width, pc->height);
    (*env)->ReleasePrimitiveArrayCritical(env, bounds, bbox, 0);
	LOGE("==================================MuPDFCore_getBoundsInternal 003");
}



JNIEXPORT float JNICALL
JNI_FN(MuPDFCore_getPageWidth)(JNIEnv *env, jobject thiz)
{
	globals *glo = get_globals(env, thiz);
	LOGI("PageWidth=%d", glo->pages[glo->current].width);
	return glo->pages[glo->current].width;
}

JNIEXPORT float JNICALL
JNI_FN(MuPDFCore_getPageHeight)(JNIEnv *env, jobject thiz)
{
	globals *glo = get_globals(env, thiz);
	LOGI("PageHeight=%d", glo->pages[glo->current].height);
	return glo->pages[glo->current].height;
}

JNIEXPORT jboolean JNICALL
JNI_FN(MuPDFCore_javascriptSupported)(JNIEnv *env, jobject thiz)
{
	globals *glo = get_globals(env, thiz);
	fz_context *ctx = glo->ctx;
	pdf_document *idoc = pdf_specifics(ctx, glo->doc);
	if (idoc)
		return pdf_js_supported(ctx, idoc);
	return 0;
}

static void update_changed_rects(globals *glo, page_cache *pc, pdf_document *idoc)
{
	fz_context *ctx = glo->ctx;
	fz_annot *annot;

	pdf_update_page(ctx, idoc, (pdf_page *)pc->page);
	while ((annot = (fz_annot *)pdf_poll_changed_annot(ctx, idoc, (pdf_page *)pc->page)) != NULL)
	{
		/* FIXME: We bound the annot twice here */
		rect_node *node = fz_malloc_struct(glo->ctx, rect_node);
		fz_bound_annot(ctx, annot, &node->rect);
		node->next = pc->changed_rects;
		pc->changed_rects = node;

		node = fz_malloc_struct(glo->ctx, rect_node);
		fz_bound_annot(ctx, annot, &node->rect);
		node->next = pc->hq_changed_rects;
		pc->hq_changed_rects = node;
	}
}

JNIEXPORT jboolean JNICALL
JNI_FN(MuPDFCore_drawPage)(JNIEnv *env, jobject thiz, jobject bitmap,
		int pageW, int pageH, int patchX, int patchY, int patchW, int patchH, jlong cookiePtr)
{
	AndroidBitmapInfo info;
	void *pixels;
	int ret;
	fz_device *dev = NULL;
	float zoom;
	fz_matrix ctm;
	fz_irect bbox;
	fz_rect rect;
	fz_pixmap *pix = NULL;
	float xscale, yscale;
	globals *glo = get_globals(env, thiz);
	fz_context *ctx = glo->ctx;
	fz_document *doc = glo->doc;
	page_cache *pc = &glo->pages[glo->current];
	int hq = (patchW < pageW || patchH < pageH);
	fz_matrix scale;
	fz_cookie *cookie = (fz_cookie *)(intptr_t)cookiePtr;

	if (pc->page == NULL)
		return 0;

	fz_var(pix);
	fz_var(dev);

	LOGI("In native method\n");
	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return 0;
	}

	LOGI("Checking format\n");
	if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap format is not RGBA_8888 !");
		return 0;
	}

	LOGI("locking pixels\n");
	if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return 0;
	}

	/* Call mupdf to render display list to screen */
	LOGI("Rendering page(%d)=%dx%d patch=[%d,%d,%d,%d]",
			pc->number, pageW, pageH, patchX, patchY, patchW, patchH);

	fz_try(ctx)
	{
		fz_irect pixbbox;
		pdf_document *idoc = pdf_specifics(ctx, doc);

		if (idoc)
		{
			/* Update the changed-rects for both hq patch and main bitmap */
			update_changed_rects(glo, pc, idoc);

			/* Then drop the changed-rects for the bitmap we're about to
			render because we are rendering the entire area */
			drop_changed_rects(ctx, hq ? &pc->hq_changed_rects : &pc->changed_rects);
		}

		if (pc->page_list == NULL)
		{
			/* Render to list */
			pc->page_list = fz_new_display_list(ctx);
			dev = fz_new_list_device(ctx, pc->page_list);
			fz_run_page_contents(ctx, pc->page, dev, &fz_identity, cookie);
			fz_drop_device(ctx, dev);
			dev = NULL;
			if (cookie != NULL && cookie->abort)
			{
				fz_drop_display_list(ctx, pc->page_list);
				pc->page_list = NULL;
				fz_throw(ctx, FZ_ERROR_GENERIC, "Render aborted");
			}
		}
		if (pc->annot_list == NULL)
		{
			fz_annot *annot;
			pc->annot_list = fz_new_display_list(ctx);
			dev = fz_new_list_device(ctx, pc->annot_list);
			for (annot = fz_first_annot(ctx, pc->page); annot; annot = fz_next_annot(ctx, annot))
				fz_run_annot(ctx, annot, dev, &fz_identity, cookie);
			fz_drop_device(ctx, dev);
			dev = NULL;
			if (cookie != NULL && cookie->abort)
			{
				fz_drop_display_list(ctx, pc->annot_list);
				pc->annot_list = NULL;
				fz_throw(ctx, FZ_ERROR_GENERIC, "Render aborted");
			}
		}
		bbox.x0 = patchX;
		bbox.y0 = patchY;
		bbox.x1 = patchX + patchW;
		bbox.y1 = patchY + patchH;
		pixbbox = bbox;
		pixbbox.x1 = pixbbox.x0 + info.width;
		/* pixmaps cannot handle right-edge padding, so the bbox must be expanded to
		 * match the pixels data */
		pix = fz_new_pixmap_with_bbox_and_data(ctx, glo->colorspace, &pixbbox, pixels);
		if (pc->page_list == NULL && pc->annot_list == NULL)
		{
			fz_clear_pixmap_with_value(ctx, pix, 0xd0);
			break;
		}
		fz_clear_pixmap_with_value(ctx, pix, 0xff);

		zoom = glo->resolution / 72;
		fz_scale(&ctm, zoom, zoom);
		rect = pc->media_box;
		fz_round_rect(&bbox, fz_transform_rect(&rect, &ctm));
		/* Now, adjust ctm so that it would give the correct page width
		 * heights. */
		xscale = (float)pageW/(float)(bbox.x1-bbox.x0);
		yscale = (float)pageH/(float)(bbox.y1-bbox.y0);
		fz_concat(&ctm, &ctm, fz_scale(&scale, xscale, yscale));
		rect = pc->media_box;
		fz_transform_rect(&rect, &ctm);
		dev = fz_new_draw_device(ctx, pix);
#ifdef TIME_DISPLAY_LIST
		{
			clock_t time;
			int i;

			LOGI("Executing display list");
			time = clock();
			for (i=0; i<100;i++) {
#endif
				if (pc->page_list)
					fz_run_display_list(ctx, pc->page_list, dev, &ctm, &rect, cookie);
				if (cookie != NULL && cookie->abort)
					fz_throw(ctx, FZ_ERROR_GENERIC, "Render aborted");

				if (pc->annot_list)
					fz_run_display_list(ctx, pc->annot_list, dev, &ctm, &rect, cookie);
				if (cookie != NULL && cookie->abort)
					fz_throw(ctx, FZ_ERROR_GENERIC, "Render aborted");

#ifdef TIME_DISPLAY_LIST
			}
			time = clock() - time;
			LOGI("100 renders in %d (%d per sec)", time, CLOCKS_PER_SEC);
		}
#endif
		fz_drop_device(ctx, dev);
		dev = NULL;
		fz_drop_pixmap(ctx, pix);
		LOGI("Rendered");
	}
	fz_always(ctx)
	{
		fz_drop_device(ctx, dev);
		dev = NULL;
	}
	fz_catch(ctx)
	{
		LOGE("Render failed");
	}

	AndroidBitmap_unlockPixels(env, bitmap);

	return 1;
}

JNIEXPORT jboolean JNICALL
JNI_FN(MuPDFCore_updatePageInternal)(JNIEnv *env, jobject thiz, jobject bitmap, int page,
		int pageW, int pageH, int patchX, int patchY, int patchW, int patchH, jlong cookiePtr)
{
	AndroidBitmapInfo info;
	void *pixels;
	int ret;
	fz_device *dev = NULL;
	float zoom;
	fz_matrix ctm;
	fz_irect bbox;
	fz_rect rect;
	fz_pixmap *pix = NULL;
	float xscale, yscale;
	pdf_document *idoc;
	page_cache *pc = NULL;
	int hq = (patchW < pageW || patchH < pageH);
	int i;
	globals *glo = get_globals(env, thiz);
	fz_context *ctx = glo->ctx;
	fz_document *doc = glo->doc;
	rect_node *crect;
	fz_matrix scale;
	fz_cookie *cookie = (fz_cookie *)(intptr_t)cookiePtr;

	for (i = 0; i < NUM_CACHE; i++)
	{
		if (glo->pages[i].page != NULL && glo->pages[i].number == page)
		{
			pc = &glo->pages[i];
			break;
		}
	}

	if (pc == NULL)
	{
		/* Without a cached page object we cannot perform a partial update so
		render the entire bitmap instead */
		JNI_FN(MuPDFCore_gotoPageInternal)(env, thiz, page);
		return JNI_FN(MuPDFCore_drawPage)(env, thiz, bitmap, pageW, pageH, patchX, patchY, patchW, patchH, (jlong)(intptr_t)cookie);
	}

	idoc = pdf_specifics(ctx, doc);

	fz_var(pix);
	fz_var(dev);

	LOGI("In native method\n");
	if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
		LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
		return 0;
	}

	LOGI("Checking format\n");
	if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
		LOGE("Bitmap format is not RGBA_8888 !");
		return 0;
	}

	LOGI("locking pixels\n");
	if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
		LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
		return 0;
	}

	/* Call mupdf to render display list to screen */
	LOGI("Rendering page(%d)=%dx%d patch=[%d,%d,%d,%d]",
			pc->number, pageW, pageH, patchX, patchY, patchW, patchH);

	fz_try(ctx)
	{
		fz_annot *annot;
		fz_irect pixbbox;

		if (idoc)
		{
			/* Update the changed-rects for both hq patch and main bitmap */
			update_changed_rects(glo, pc, idoc);
		}

		if (pc->page_list == NULL)
		{
			/* Render to list */
			pc->page_list = fz_new_display_list(ctx);
			dev = fz_new_list_device(ctx, pc->page_list);
			fz_run_page_contents(ctx, pc->page, dev, &fz_identity, cookie);
			fz_drop_device(ctx, dev);
			dev = NULL;
			if (cookie != NULL && cookie->abort)
			{
				fz_drop_display_list(ctx, pc->page_list);
				pc->page_list = NULL;
				fz_throw(ctx, FZ_ERROR_GENERIC, "Render aborted");
			}
		}

		if (pc->annot_list == NULL) {
			pc->annot_list = fz_new_display_list(ctx);
			dev = fz_new_list_device(ctx, pc->annot_list);
			for (annot = fz_first_annot(ctx, pc->page); annot; annot = fz_next_annot(ctx, annot))
				fz_run_annot(ctx, annot, dev, &fz_identity, cookie);
			fz_drop_device(ctx, dev);
			dev = NULL;
			if (cookie != NULL && cookie->abort)
			{
				fz_drop_display_list(ctx, pc->annot_list);
				pc->annot_list = NULL;
				fz_throw(ctx, FZ_ERROR_GENERIC, "Render aborted");
			}
		}

		bbox.x0 = patchX;
		bbox.y0 = patchY;
		bbox.x1 = patchX + patchW;
		bbox.y1 = patchY + patchH;
		pixbbox = bbox;
		pixbbox.x1 = pixbbox.x0 + info.width;
		/* pixmaps cannot handle right-edge padding, so the bbox must be expanded to
		 * match the pixels data */
		pix = fz_new_pixmap_with_bbox_and_data(ctx, glo->colorspace, &pixbbox, pixels);

		zoom = glo->resolution / 72;
		fz_scale(&ctm, zoom, zoom);
		rect = pc->media_box;
		fz_round_rect(&bbox, fz_transform_rect(&rect, &ctm));
		/* Now, adjust ctm so that it would give the correct page width
		 * heights. */
		xscale = (float)pageW/(float)(bbox.x1-bbox.x0);
		yscale = (float)pageH/(float)(bbox.y1-bbox.y0);
		fz_concat(&ctm, &ctm, fz_scale(&scale, xscale, yscale));
		rect = pc->media_box;
		fz_transform_rect(&rect, &ctm);

		LOGI("Start partial update");
		for (crect = hq ? pc->hq_changed_rects : pc->changed_rects; crect; crect = crect->next)
		{
			fz_irect abox;
			fz_rect arect = crect->rect;
			fz_intersect_rect(fz_transform_rect(&arect, &ctm), &rect);
			fz_round_rect(&abox, &arect);

			LOGI("Update rectangle (%d, %d, %d, %d)", abox.x0, abox.y0, abox.x1, abox.y1);
			if (!fz_is_empty_irect(&abox))
			{
				LOGI("And it isn't empty");
				fz_clear_pixmap_rect_with_value(ctx, pix, 0xff, &abox);
				dev = fz_new_draw_device_with_bbox(ctx, pix, &abox);
				if (pc->page_list)
					fz_run_display_list(ctx, pc->page_list, dev, &ctm, &arect, cookie);
				if (cookie != NULL && cookie->abort)
					fz_throw(ctx, FZ_ERROR_GENERIC, "Render aborted");

				if (pc->annot_list)
					fz_run_display_list(ctx, pc->annot_list, dev, &ctm, &arect, cookie);
				if (cookie != NULL && cookie->abort)
					fz_throw(ctx, FZ_ERROR_GENERIC, "Render aborted");

				fz_drop_device(ctx, dev);
				dev = NULL;
			}
		}
		LOGI("End partial update");

		/* Drop the changed rects we've just rendered */
		drop_changed_rects(ctx, hq ? &pc->hq_changed_rects : &pc->changed_rects);

		LOGI("Rendered");
	}
	fz_always(ctx)
	{
		fz_drop_device(ctx, dev);
		dev = NULL;
	}
	fz_catch(ctx)
	{
		LOGE("Render failed");
	}

	fz_drop_pixmap(ctx, pix);
	AndroidBitmap_unlockPixels(env, bitmap);

	return 1;
}

static int
countOutlineItems(fz_outline *outline)
{
	int count = 0;

	while (outline)
	{
		if (outline->dest.kind == FZ_LINK_GOTO
				&& outline->dest.ld.gotor.page >= 0
				&& outline->title)
		{
			count++;
		}
		if (outline->dest.kind == FZ_LINK_URI
				&& outline->dest.ld.uri.uri != NULL
				&& outline->title)
		{
			count++;
		}

		count += countOutlineItems(outline->down);
		outline = outline->next;
	}

	return count;
}

static int
fillInOutlineItems(JNIEnv * env, jclass olClass, jmethodID ctor, jobjectArray arr, int pos, fz_outline *outline, int level)
{
	while (outline)
	{
		int type = 0;
		int page = -1;
		char *link = NULL;
		jfloat pointx = -1.f;
		jfloat pointy = -1.f;
		jint flags = 0;
		if (outline->title == NULL)
		{
			continue; //skip
		}
		if (outline->dest.kind == FZ_LINK_GOTO)
		{
			type = 1;
			page = outline->dest.ld.gotor.page;
			if (page < 0) 
			{
				continue; //skip
			}
			pointx = outline->dest.ld.gotor.lt.x;
			pointy = outline->dest.ld.gotor.lt.y;
			flags = outline->dest.ld.gotor.flags;
		}
		else if (outline->dest.kind == FZ_LINK_URI)
		{
			type = 2;
			link = outline->dest.ld.uri.uri;
			if (link == NULL)
			{
				continue; //skip
			}
		}
		{
			jobject ol;
			jstring title = (*env)->NewStringUTF(env, outline->title);
			if (title == NULL) return -1;
			jstring linkStr = NULL;
			if (link != NULL) 
			{
				linkStr = (*env)->NewStringUTF(env, link);
				if (linkStr == NULL) return -1;
			}
			ol = (*env)->NewObject(env, olClass, ctor, level, title, page, type, link, linkStr, pointx, pointy, flags);
			if (ol == NULL) return -1;
			(*env)->SetObjectArrayElement(env, arr, pos, ol);
			(*env)->DeleteLocalRef(env, ol);
			(*env)->DeleteLocalRef(env, title);
			(*env)->DeleteLocalRef(env, linkStr);
			pos++;
		}
		pos = fillInOutlineItems(env, olClass, ctor, arr, pos, outline->down, level+1);
		if (pos < 0) 
		{
			return -1;
		}
		outline = outline->next;
	}
	return pos;
}

JNIEXPORT jboolean JNICALL
JNI_FN(MuPDFCore_needsPasswordInternal)(JNIEnv * env, jobject thiz)
{
	globals *glo = get_globals(env, thiz);
	fz_context *ctx = glo->ctx;

	return fz_needs_password(ctx, glo->doc) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
JNI_FN(MuPDFCore_hasOutlineInternal)(JNIEnv * env, jobject thiz)
{
	globals *glo = get_globals(env, thiz);
	fz_context *ctx = glo->ctx;
	fz_outline *outline = fz_load_outline(ctx, glo->doc);

	fz_drop_outline(glo->ctx, outline);
	return (outline == NULL) ? JNI_FALSE : JNI_TRUE;
}

JNIEXPORT jobjectArray JNICALL
JNI_FN(MuPDFCore_getOutlineInternal)(JNIEnv * env, jobject thiz)
{
	jclass olClass;
	jmethodID ctor;
	jobjectArray arr;
	jobject ol;
	fz_outline *outline;
	int nItems;
	globals *glo = get_globals(env, thiz);
	fz_context *ctx = glo->ctx;
	jobjectArray ret;
	LOGE("==================================MuPDFCore_getOutlineInternal 001");
	olClass = (*env)->FindClass(env, PACKAGENAME "/OutlineItem");
	if (olClass == NULL) return NULL;
	ctor = (*env)->GetMethodID(env, olClass, "<init>", "(ILjava/lang/String;IILjava/lang/String;FFI)V");
	if (ctor == NULL) return NULL;
	LOGE("==================================MuPDFCore_getOutlineInternal 002");
	outline = fz_load_outline(ctx, glo->doc);
	nItems = countOutlineItems(outline);
	LOGE("==================================MuPDFCore_getOutlineInternal 003");
	arr = (*env)->NewObjectArray(env,
					nItems,
					olClass,
					NULL);
	if (arr == NULL) return NULL;
	LOGE("==================================MuPDFCore_getOutlineInternal 004");
	ret = fillInOutlineItems(env, olClass, ctor, arr, 0, outline, 0) > 0 ? arr : NULL;
	fz_drop_outline(glo->ctx, outline);
	LOGE("==================================MuPDFCore_getOutlineInternal 005");
	return ret;
}

JNIEXPORT jobjectArray JNICALL
JNI_FN(MuPDFCore_searchPage)(JNIEnv * env, jobject thiz, jstring jtext)
{
	jclass rectClass;
	jmethodID ctor;
	jobjectArray arr;
	jobject rect;
	fz_stext_sheet *sheet = NULL;
	fz_stext_page *text = NULL;
	fz_device *dev = NULL;
	float zoom;
	fz_matrix ctm;
	int pos;
	int len;
	int i, n;
	int hit_count = 0;
	const char *str;
	globals *glo = get_globals(env, thiz);
	fz_context *ctx = glo->ctx;
	fz_document *doc = glo->doc;
	page_cache *pc = &glo->pages[glo->current];

	rectClass = (*env)->FindClass(env, "android/graphics/RectF");
	if (rectClass == NULL) return NULL;
	ctor = (*env)->GetMethodID(env, rectClass, "<init>", "(FFFF)V");
	if (ctor == NULL) return NULL;
	str = (*env)->GetStringUTFChars(env, jtext, NULL);
	if (str == NULL) return NULL;

	fz_var(sheet);
	fz_var(text);
	fz_var(dev);

	fz_try(ctx)
	{
		if (glo->hit_bbox == NULL)
			glo->hit_bbox = fz_malloc_array(ctx, MAX_SEARCH_HITS, sizeof(*glo->hit_bbox));

		zoom = glo->resolution / 72;
		fz_scale(&ctm, zoom, zoom);
		sheet = fz_new_stext_sheet(ctx);
		text = fz_new_stext_page(ctx);
		dev = fz_new_stext_device(ctx, sheet, text);
		fz_run_page(ctx, pc->page, dev, &ctm, NULL);
		fz_drop_device(ctx, dev);
		dev = NULL;

		hit_count = fz_search_stext_page(ctx, text, str, glo->hit_bbox, MAX_SEARCH_HITS);
	}
	fz_always(ctx)
	{
		fz_drop_stext_page(ctx, text);
		fz_drop_stext_sheet(ctx, sheet);
		fz_drop_device(ctx, dev);
	}
	fz_catch(ctx)
	{
		jclass cls;
		(*env)->ReleaseStringUTFChars(env, jtext, str);
		cls = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
		if (cls != NULL)
			(*env)->ThrowNew(env, cls, "Out of memory in MuPDFCore_searchPage");
		(*env)->DeleteLocalRef(env, cls);

		return NULL;
	}

	(*env)->ReleaseStringUTFChars(env, jtext, str);

	arr = (*env)->NewObjectArray(env,
					hit_count,
					rectClass,
					NULL);
	if (arr == NULL) return NULL;

	for (i = 0; i < hit_count; i++) {
		rect = (*env)->NewObject(env, rectClass, ctor,
				(float) (glo->hit_bbox[i].x0),
				(float) (glo->hit_bbox[i].y0),
				(float) (glo->hit_bbox[i].x1),
				(float) (glo->hit_bbox[i].y1));
		if (rect == NULL)
			return NULL;
		(*env)->SetObjectArrayElement(env, arr, i, rect);
		(*env)->DeleteLocalRef(env, rect);
	}

	return arr;
}

JNIEXPORT jobjectArray JNICALL
JNI_FN(MuPDFCore_text)(JNIEnv * env, jobject thiz)
{
	jclass textCharClass;
	jclass textSpanClass;
	jclass textLineClass;
	jclass textBlockClass;
	jmethodID ctor;
	jobjectArray barr = NULL;
	fz_stext_sheet *sheet = NULL;
	fz_stext_page *text = NULL;
	fz_device *dev = NULL;
	float zoom;
	fz_matrix ctm;
	globals *glo = get_globals(env, thiz);
	fz_context *ctx = glo->ctx;
	fz_document *doc = glo->doc;
	page_cache *pc = &glo->pages[glo->current];

	textCharClass = (*env)->FindClass(env, PACKAGENAME "/TextChar");
	if (textCharClass == NULL) return NULL;
	textSpanClass = (*env)->FindClass(env, "[L" PACKAGENAME "/TextChar;");
	if (textSpanClass == NULL) return NULL;
	textLineClass = (*env)->FindClass(env, "[[L" PACKAGENAME "/TextChar;");
	if (textLineClass == NULL) return NULL;
	textBlockClass = (*env)->FindClass(env, "[[[L" PACKAGENAME "/TextChar;");
	if (textBlockClass == NULL) return NULL;
	ctor = (*env)->GetMethodID(env, textCharClass, "<init>", "(FFFFC)V");
	if (ctor == NULL) return NULL;

	fz_var(sheet);
	fz_var(text);
	fz_var(dev);

	fz_try(ctx)
	{
		int b, l, s, c;

		zoom = glo->resolution / 72;
		fz_scale(&ctm, zoom, zoom);
		sheet = fz_new_stext_sheet(ctx);
		text = fz_new_stext_page(ctx);
		dev = fz_new_stext_device(ctx, sheet, text);
		fz_run_page(ctx, pc->page, dev, &ctm, NULL);
		fz_drop_device(ctx, dev);
		dev = NULL;

		barr = (*env)->NewObjectArray(env, text->len, textBlockClass, NULL);
		if (barr == NULL) fz_throw(ctx, FZ_ERROR_GENERIC, "NewObjectArray failed");

		for (b = 0; b < text->len; b++)
		{
			fz_stext_block *block;
			jobjectArray *larr;

			if (text->blocks[b].type != FZ_PAGE_BLOCK_TEXT)
				continue;
			block = text->blocks[b].u.text;
			larr = (*env)->NewObjectArray(env, block->len, textLineClass, NULL);
			if (larr == NULL) fz_throw(ctx, FZ_ERROR_GENERIC, "NewObjectArray failed");

			for (l = 0; l < block->len; l++)
			{
				fz_stext_line *line = &block->lines[l];
				jobjectArray *sarr;
				fz_stext_span *span;
				int len = 0;

				for (span = line->first_span; span; span = span->next)
					len++;

				sarr = (*env)->NewObjectArray(env, len, textSpanClass, NULL);
				if (sarr == NULL) fz_throw(ctx, FZ_ERROR_GENERIC, "NewObjectArray failed");

				for (s=0, span = line->first_span; span; s++, span = span->next)
				{
					jobjectArray *carr = (*env)->NewObjectArray(env, span->len, textCharClass, NULL);
					if (carr == NULL) fz_throw(ctx, FZ_ERROR_GENERIC, "NewObjectArray failed");

					for (c = 0; c < span->len; c++)
					{
						fz_stext_char *ch = &span->text[c];
						fz_rect bbox;
						fz_stext_char_bbox(ctx, &bbox, span, c);
						jobject cobj = (*env)->NewObject(env, textCharClass, ctor, bbox.x0, bbox.y0, bbox.x1, bbox.y1, ch->c);
						if (cobj == NULL) fz_throw(ctx, FZ_ERROR_GENERIC, "NewObjectfailed");

						(*env)->SetObjectArrayElement(env, carr, c, cobj);
						(*env)->DeleteLocalRef(env, cobj);
					}

					(*env)->SetObjectArrayElement(env, sarr, s, carr);
					(*env)->DeleteLocalRef(env, carr);
				}

				(*env)->SetObjectArrayElement(env, larr, l, sarr);
				(*env)->DeleteLocalRef(env, sarr);
			}

			(*env)->SetObjectArrayElement(env, barr, b, larr);
			(*env)->DeleteLocalRef(env, larr);
		}
	}
	fz_always(ctx)
	{
		fz_drop_stext_page(ctx, text);
		fz_drop_stext_sheet(ctx, sheet);
		fz_drop_device(ctx, dev);
	}
	fz_catch(ctx)
	{
		jclass cls = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
		if (cls != NULL)
			(*env)->ThrowNew(env, cls, "Out of memory in MuPDFCore_text");
		(*env)->DeleteLocalRef(env, cls);

		return NULL;
	}

	return barr;
}

JNIEXPORT void JNICALL
JNI_FN(MuPDFCore_addMarkupAnnotationInternal)(JNIEnv * env, jobject thiz, jobjectArray points, fz_annot_type type)
{
	globals *glo = get_globals(env, thiz);
	fz_context *ctx = glo->ctx;
	fz_document *doc = glo->doc;
	pdf_document *idoc = pdf_specifics(ctx, doc);
	page_cache *pc = &glo->pages[glo->current];
	jclass pt_cls;
	jfieldID x_fid, y_fid;
	int i, n;
	fz_point *pts = NULL;
	float color[3];
	float alpha;
	float line_height;
	float line_thickness;

	if (idoc == NULL)
		return;

	switch (type)
	{
		case FZ_ANNOT_HIGHLIGHT:
			color[0] = 1.0;
			color[1] = 1.0;
			color[2] = 0.0;
			alpha = 0.5;
			line_thickness = 1.0;
			line_height = 0.5;
			break;
		case FZ_ANNOT_UNDERLINE:
			color[0] = 0.0;
			color[1] = 0.0;
			color[2] = 1.0;
			alpha = 1.0;
			line_thickness = LINE_THICKNESS;
			line_height = UNDERLINE_HEIGHT;
			break;
		case FZ_ANNOT_STRIKEOUT:
			color[0] = 1.0;
			color[1] = 0.0;
			color[2] = 0.0;
			alpha = 1.0;
			line_thickness = LINE_THICKNESS;
			line_height = STRIKE_HEIGHT;
			break;
		default:
			return;
	}

	fz_var(pts);
	fz_try(ctx)
	{
		fz_annot *annot;
		fz_matrix ctm;

		float zoom = glo->resolution / 72;
		zoom = 1.0 / zoom;
		fz_scale(&ctm, zoom, zoom);
		pt_cls = (*env)->FindClass(env, "android/graphics/PointF");
		if (pt_cls == NULL) fz_throw(ctx, FZ_ERROR_GENERIC, "FindClass");
		x_fid = (*env)->GetFieldID(env, pt_cls, "x", "F");
		if (x_fid == NULL) fz_throw(ctx, FZ_ERROR_GENERIC, "GetFieldID(x)");
		y_fid = (*env)->GetFieldID(env, pt_cls, "y", "F");
		if (y_fid == NULL) fz_throw(ctx, FZ_ERROR_GENERIC, "GetFieldID(y)");

		n = (*env)->GetArrayLength(env, points);

		pts = fz_malloc_array(ctx, n, sizeof(fz_point));

		for (i = 0; i < n; i++)
		{
			jobject opt = (*env)->GetObjectArrayElement(env, points, i);
			pts[i].x = opt ? (*env)->GetFloatField(env, opt, x_fid) : 0.0f;
			pts[i].y = opt ? (*env)->GetFloatField(env, opt, y_fid) : 0.0f;
			fz_transform_point(&pts[i], &ctm);
		}

		annot = (fz_annot *)pdf_create_annot(ctx, idoc, (pdf_page *)pc->page, type);

		pdf_set_markup_annot_quadpoints(ctx, idoc, (pdf_annot *)annot, pts, n);
		pdf_set_markup_appearance(ctx, idoc, (pdf_annot *)annot, color, alpha, line_thickness, line_height);

		dump_annotation_display_lists(glo);
	}
	fz_always(ctx)
	{
		fz_free(ctx, pts);
	}
	fz_catch(ctx)
	{
		LOGE("addStrikeOutAnnotation: %s failed", ctx->error->message);
		jclass cls = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
		if (cls != NULL)
			(*env)->ThrowNew(env, cls, "Out of memory in MuPDFCore_searchPage");
		(*env)->DeleteLocalRef(env, cls);
	}
}

JNIEXPORT void JNICALL
JNI_FN(MuPDFCore_addInkAnnotationInternal)(JNIEnv * env, jobject thiz, jobjectArray arcs)
{
	globals *glo = get_globals(env, thiz);
	fz_context *ctx = glo->ctx;
	fz_document *doc = glo->doc;
	pdf_document *idoc = pdf_specifics(ctx, doc);
	page_cache *pc = &glo->pages[glo->current];
	jclass pt_cls;
	jfieldID x_fid, y_fid;
	int i, j, k, n;
	fz_point *pts = NULL;
	int *counts = NULL;
	int total = 0;
	float color[3];

	if (idoc == NULL)
		return;

	color[0] = 1.0;
	color[1] = 0.0;
	color[2] = 0.0;

	fz_var(pts);
	fz_var(counts);
	fz_try(ctx)
	{
		fz_annot *annot;
		fz_matrix ctm;

		float zoom = glo->resolution / 72;
		zoom = 1.0 / zoom;
		fz_scale(&ctm, zoom, zoom);
		pt_cls = (*env)->FindClass(env, "android/graphics/PointF");
		if (pt_cls == NULL) fz_throw(ctx, FZ_ERROR_GENERIC, "FindClass");
		x_fid = (*env)->GetFieldID(env, pt_cls, "x", "F");
		if (x_fid == NULL) fz_throw(ctx, FZ_ERROR_GENERIC, "GetFieldID(x)");
		y_fid = (*env)->GetFieldID(env, pt_cls, "y", "F");
		if (y_fid == NULL) fz_throw(ctx, FZ_ERROR_GENERIC, "GetFieldID(y)");

		n = (*env)->GetArrayLength(env, arcs);

		counts = fz_malloc_array(ctx, n, sizeof(int));

		for (i = 0; i < n; i++)
		{
			jobjectArray arc = (jobjectArray)(*env)->GetObjectArrayElement(env, arcs, i);
			int count = (*env)->GetArrayLength(env, arc);

			counts[i] = count;
			total += count;
		}

		pts = fz_malloc_array(ctx, total, sizeof(fz_point));

		k = 0;
		for (i = 0; i < n; i++)
		{
			jobjectArray arc = (jobjectArray)(*env)->GetObjectArrayElement(env, arcs, i);
			int count = counts[i];

			for (j = 0; j < count; j++)
			{
				jobject pt = (*env)->GetObjectArrayElement(env, arc, j);

				pts[k].x = pt ? (*env)->GetFloatField(env, pt, x_fid) : 0.0f;
				pts[k].y = pt ? (*env)->GetFloatField(env, pt, y_fid) : 0.0f;
				(*env)->DeleteLocalRef(env, pt);
				fz_transform_point(&pts[k], &ctm);
				k++;
			}
			(*env)->DeleteLocalRef(env, arc);
		}

		annot = (fz_annot *)pdf_create_annot(ctx, idoc, (pdf_page *)pc->page, FZ_ANNOT_INK);

		pdf_set_ink_annot_list(ctx, idoc, (pdf_annot *)annot, pts, counts, n, color, INK_THICKNESS);

		dump_annotation_display_lists(glo);
	}
	fz_always(ctx)
	{
		fz_free(ctx, pts);
		fz_free(ctx, counts);
	}
	fz_catch(ctx)
	{
		LOGE("addInkAnnotation: %s failed", ctx->error->message);
		jclass cls = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
		if (cls != NULL)
			(*env)->ThrowNew(env, cls, "Out of memory in MuPDFCore_searchPage");
		(*env)->DeleteLocalRef(env, cls);
	}
}

JNIEXPORT void JNICALL
JNI_FN(MuPDFCore_deleteAnnotationInternal)(JNIEnv * env, jobject thiz, int annot_index)
{
	globals *glo = get_globals(env, thiz);
	fz_context *ctx = glo->ctx;
	fz_document *doc = glo->doc;
	pdf_document *idoc = pdf_specifics(ctx, doc);
	page_cache *pc = &glo->pages[glo->current];
	fz_annot *annot;
	int i;

	if (idoc == NULL)
		return;

	fz_try(ctx)
	{
		annot = fz_first_annot(ctx, pc->page);
		for (i = 0; i < annot_index && annot; i++)
			annot = fz_next_annot(ctx, annot);

		if (annot)
		{
			pdf_delete_annot(ctx, idoc, (pdf_page *)pc->page, (pdf_annot *)annot);
			dump_annotation_display_lists(glo);
		}
	}
	fz_catch(ctx)
	{
		LOGE("deleteAnnotationInternal: %s", ctx->error->message);
	}
}

/* Close the document, at least enough to be able to save over it. This
 * may be called again later, so must be idempotent. */
static void close_doc(globals *glo)
{
	int i;

	fz_free(glo->ctx, glo->hit_bbox);
	glo->hit_bbox = NULL;

	for (i = 0; i < NUM_CACHE; i++)
		drop_page_cache(glo, &glo->pages[i]);

	//alerts_fin(glo);

	fz_drop_document(glo->ctx, glo->doc);
	glo->doc = NULL;
}

JNIEXPORT void JNICALL
JNI_FN(MuPDFCore_destroying)(JNIEnv * env, jobject thiz)
{
	globals *glo = get_globals(env, thiz);

	if (glo == NULL)
		return;
	LOGI("Destroying");
	fz_free(glo->ctx, glo->current_path);
	glo->current_path = NULL;
	close_doc(glo);
	fz_drop_context(glo->ctx);
	glo->ctx = NULL;
	free(glo);
#ifdef MEMENTO
	LOGI("Destroying dump start");
	Memento_listBlocks();
	Memento_stats();
	LOGI("Destroying dump end");
#endif
#ifdef NDK_PROFILER
	// Apparently we should really be writing to whatever path we get
	// from calling getFilesDir() in the java part, which supposedly
	// gives /sdcard/data/data/com.artifex.MuPDF/gmon.out, but that's
	// unfriendly.
	//setenv("CPUPROFILE", "/sdcard/gmon.out", 1);
	//moncleanup();
#endif
}

JNIEXPORT jobjectArray JNICALL
JNI_FN(MuPDFCore_getPageLinksInternal)(JNIEnv * env, jobject thiz, int pageNumber, jboolean isscale)
{
	jclass linkInfoClass;
	jmethodID ctor;
	jobjectArray arr;
	jobject linkInfo;
	fz_matrix ctm;
	float zoom;
	fz_link *list;
	fz_link *link;
	int count;
	page_cache *pc;
	globals *glo = get_globals(env, thiz);
	LOGE("==================================MuPDFCore_getPageLinksInternal 001");
	linkInfoClass = (*env)->FindClass(env, PACKAGENAME "/LinkInfo");
	if (linkInfoClass == NULL) return NULL;
	ctor = (*env)->GetMethodID(env, linkInfoClass, "<init>", "(FFFFIILjava/lang/String;ZFFI)V");
	LOGE("==================================MuPDFCore_getPageLinksInternal 001-2");
	if (ctor == NULL) return NULL;
	LOGE("==================================MuPDFCore_getPageLinksInternal 002 %lx", (long)glo);
	JNI_FN(MuPDFCore_gotoPageInternal)(env, thiz, pageNumber);
	LOGE("==================================MuPDFCore_getPageLinksInternal 002-1");
	pc = &glo->pages[glo->current];
	LOGE("==================================MuPDFCore_getPageLinksInternal 002-2");
	if (pc->page == NULL || pc->number != pageNumber)
	{
	LOGE("==================================MuPDFCore_getPageLinksInternal 002-3");
		return NULL;
	}
	LOGE("==================================MuPDFCore_getPageLinksInternal 002-4");
	LOGE("==================================MuPDFCore_getPageLinksInternal 003");
	zoom = glo->resolution / 72;
	fz_scale(&ctm, zoom, zoom);
	LOGE("==================================MuPDFCore_getPageLinksInternal 004");
	list = fz_load_links(glo->ctx, pc->page);
	count = 0;
	for (link = list; link; link = link->next)
	{
		LOGE("==================================MuPDFCore_getPageLinksInternal 004 for==> %d", link->dest.kind);
		switch (link->dest.kind)
		{
		case FZ_LINK_GOTO:
		case FZ_LINK_GOTOR:
		case FZ_LINK_URI:
			count++ ;
		}
	}
	LOGE("==================================MuPDFCore_getPageLinksInternal 005");
	arr = (*env)->NewObjectArray(env, count, linkInfoClass, NULL);
	if (arr == NULL)
	{
		fz_drop_link(glo->ctx, list);
		return NULL;
	}
	LOGE("==================================MuPDFCore_getPageLinksInternal 006");
	count = 0;
	for (link = list; link; link = link->next)
	{
		fz_rect rect = link->rect;
		if (isscale)
		{
			fz_transform_rect(&rect, &ctm);
		}

		switch (link->dest.kind)
		{
		case FZ_LINK_GOTO:
		{
			linkInfo = (*env)->NewObject(env, linkInfoClass, ctor,
					(float)rect.x0, (float)rect.y0, (float)rect.x1, (float)rect.y1,
					1, link->dest.ld.gotor.page, NULL, JNI_FALSE,
					link->dest.ld.gotor.lt.x, link->dest.ld.gotor.lt.y, link->dest.ld.gotor.flags);
			break;
		}

		case FZ_LINK_GOTOR:
		{
			jstring juri = (*env)->NewStringUTF(env, link->dest.ld.gotor.file_spec);
			linkInfo = (*env)->NewObject(env, linkInfoClass, ctor,
					(float)rect.x0, (float)rect.y0, (float)rect.x1, (float)rect.y1,
					3, 0, juri, link->dest.ld.gotor.page, link->dest.ld.gotor.new_window ? JNI_TRUE : JNI_FALSE,
					0.0f, 0.0f, 0);
			break;
		}

		case FZ_LINK_URI:
		{
			jstring juri = (*env)->NewStringUTF(env, link->dest.ld.uri.uri);
			linkInfo = (*env)->NewObject(env, linkInfoClass, ctor,
					(float)rect.x0, (float)rect.y0, (float)rect.x1, (float)rect.y1,
					3, 0, juri, JNI_FALSE,
					0.0f, 0.0f, 0);
			break;
		}

		default:
			continue;
		}

		if (linkInfo == NULL)
		{
			fz_drop_link(glo->ctx, list);
			return NULL;
		}
		(*env)->SetObjectArrayElement(env, arr, count, linkInfo);
		(*env)->DeleteLocalRef(env, linkInfo);
		count++;
	}
	fz_drop_link(glo->ctx, list);
	LOGE("==================================MuPDFCore_getPageLinksInternal 007");
	return arr;
}




JNIEXPORT jobjectArray JNICALL
JNI_FN(MuPDFCore_getAnnotationsInternal)(JNIEnv * env, jobject thiz, int pageNumber)
{
	jclass annotClass;
	jmethodID ctor;
	jobjectArray arr;
	jobject jannot;
	fz_annot *annot;
	fz_matrix ctm;
	float zoom;
	int count;
	page_cache *pc;
	globals *glo = get_globals(env, thiz);
	if (glo == NULL)
		return NULL;
	fz_context *ctx = glo->ctx;

	annotClass = (*env)->FindClass(env, PACKAGENAME "/Annotation");
	if (annotClass == NULL) return NULL;
	ctor = (*env)->GetMethodID(env, annotClass, "<init>", "(FFFFI)V");
	if (ctor == NULL) return NULL;

	JNI_FN(MuPDFCore_gotoPageInternal)(env, thiz, pageNumber);
	pc = &glo->pages[glo->current];
	if (pc->number != pageNumber || pc->page == NULL)
		return NULL;

	zoom = glo->resolution / 72;
	fz_scale(&ctm, zoom, zoom);

	count = 0;
	for (annot = fz_first_annot(ctx, pc->page); annot; annot = fz_next_annot(ctx, annot))
		count ++;

	arr = (*env)->NewObjectArray(env, count, annotClass, NULL);
	if (arr == NULL) return NULL;

	count = 0;
	for (annot = fz_first_annot(ctx, pc->page); annot; annot = fz_next_annot(ctx, annot))
	{
		fz_rect rect;
		fz_annot_type type = pdf_annot_type(ctx, (pdf_annot *)annot);
		fz_bound_annot(ctx, annot, &rect);
		fz_transform_rect(&rect, &ctm);

		jannot = (*env)->NewObject(env, annotClass, ctor,
				(float)rect.x0, (float)rect.y0, (float)rect.x1, (float)rect.y1, type);
		if (jannot == NULL) return NULL;
		(*env)->SetObjectArrayElement(env, arr, count, jannot);
		(*env)->DeleteLocalRef(env, jannot);

		count ++;
	}

	return arr;
}



JNIEXPORT jboolean JNICALL
JNI_FN(MuPDFCore_hasChangesInternal)(JNIEnv * env, jobject thiz)
{
	globals *glo = get_globals(env, thiz);
	fz_context *ctx = glo->ctx;
	pdf_document *idoc = pdf_specifics(ctx, glo->doc);

	return (idoc && pdf_has_unsaved_changes(ctx, idoc)) ? JNI_TRUE : JNI_FALSE;
}

static char *tmp_path(char *path)
{
	int f;
	char *buf = malloc(strlen(path) + 6 + 1);
	if (!buf)
		return NULL;

	strcpy(buf, path);
	strcat(buf, "XXXXXX");

	f = mkstemp(buf);

	if (f >= 0)
	{
		close(f);
		return buf;
	}
	else
	{
		free(buf);
		return NULL;
	}
}

JNIEXPORT void JNICALL
JNI_FN(MuPDFCore_saveInternal)(JNIEnv * env, jobject thiz)
{
	globals *glo = get_globals(env, thiz);
	fz_context *ctx = glo->ctx;
	pdf_document *idoc = pdf_specifics(ctx, glo->doc);

	if (idoc && glo->current_path)
	{
		char *tmp;
		pdf_write_options opts = { 0 };

		opts.do_incremental = 1;

		tmp = tmp_path(glo->current_path);
		if (tmp)
		{
			int written = 0;

			fz_var(written);
			fz_try(ctx)
			{
				FILE *fin = fopen(glo->current_path, "rb");
				FILE *fout = fopen(tmp, "wb");
				char buf[256];
				int n, err = 1;

				if (fin && fout)
				{
					while ((n = fread(buf, 1, sizeof(buf), fin)) > 0)
						fwrite(buf, 1, n, fout);
					err = (ferror(fin) || ferror(fout));
				}

				if (fin)
					fclose(fin);
				if (fout)
					fclose(fout);

				if (!err)
				{
					pdf_save_document(ctx, idoc, tmp, &opts);
					written = 1;
				}
			}
			fz_catch(ctx)
			{
				written = 0;
			}

			if (written)
			{
				close_doc(glo);
				rename(tmp, glo->current_path);
			}

			free(tmp);
		}
	}
}

JNIEXPORT jlong JNICALL
JNI_FN(MuPDFCore_createCookie)(JNIEnv * env, jobject thiz)
{
	globals *glo = get_globals_any_thread(env, thiz);
	if (glo == NULL)
		return 0;
	fz_context *ctx = glo->ctx;

	return (jlong) (intptr_t) fz_calloc_no_throw(ctx,1, sizeof(fz_cookie));
}

JNIEXPORT void JNICALL
JNI_FN(MuPDFCore_destroyCookie)(JNIEnv * env, jobject thiz, jlong cookiePtr)
{
	fz_cookie *cookie = (fz_cookie *) (intptr_t) cookiePtr;
	globals *glo = get_globals_any_thread(env, thiz);
	if (glo == NULL)
		return;
	fz_context *ctx = glo->ctx;

	fz_free(ctx, cookie);
}

JNIEXPORT void JNICALL
JNI_FN(MuPDFCore_abortCookie)(JNIEnv * env, jobject thiz, jlong cookiePtr)
{
	fz_cookie *cookie = (fz_cookie *) (intptr_t) cookiePtr;
	if (cookie != NULL)
		cookie->abort = 1;
}


JNIEXPORT void JNICALL
JNI_FN(MuPDFCore_getBounds2)(JNIEnv *env, jobject thiz, jint pagenum, jfloatArray bounds)
{
	globals *glo = get_globals(env, thiz);
	if (glo == NULL)
		return;
	//FIXME: must be after get_globals, JNI DETECTED ERROR IN APPLICATION: using JNI after critical get ;
	jfloat *bbox = (*env)->GetPrimitiveArrayCritical(env, bounds, 0);
    if (!bbox)
        return;
        	
	fz_context *ctx = glo->ctx;
	fz_document *doc = glo->doc;
	fz_page *page = fz_load_page(ctx, doc, pagenum);
	
    fz_rect media_box;
    const fz_rect *page_bounds = fz_bound_page(ctx, page, &media_box);
    // DEBUG("Bounds: %f %f %f %f", page_bounds.x0, page_bounds.y0, page_bounds.x1, page_bounds.y1);
    bbox[0] = page_bounds->x0;
    bbox[1] = page_bounds->y0;
    bbox[2] = page_bounds->x1;
    bbox[3] = page_bounds->y1;
    (*env)->ReleasePrimitiveArrayCritical(env, bounds, bbox, 0);
    fz_drop_page(ctx, page); //FIXME:释放
}

JNIEXPORT void JNICALL
JNI_FN(MuPDFCore_renderPage2)(JNIEnv *env, jobject thiz, jint pagenum,
    jintArray viewboxarray, jfloatArray matrixarray,
    jintArray bufferarray)
{
	LOGE("==================================MuPDFCore_renderPage2 001");
	globals *glo = get_globals(env, thiz);
	if (glo == NULL)
		return;
	LOGE("==================================MuPDFCore_renderPage2 002");		
	fz_context *ctx = glo->ctx;
	fz_document *doc = glo->doc;
	fz_page *page = NULL;
	fz_display_list *page_list = NULL;
	fz_device *devPage = NULL;
    LOGE("==================================MuPDFCore_renderPage2 003");
    // DEBUG("PdfView(%p).renderPage(%p, %p)", this, doc, page);
    fz_matrix ctm;
    fz_rect viewbox;
    fz_pixmap *pixmap;
    jfloat *matrix;
    jint *viewboxarr;
    jint *dimen;
    jint *buffer;
    int length, val;
    fz_device *dev = NULL;

    /* initialize parameter arrays for MuPDF */
	LOGE("==================================MuPDFCore_renderPage2 004");
	
    matrix = (*env)->GetPrimitiveArrayCritical(env, matrixarray, 0);
    ctm = fz_identity;
    ctm.a = matrix[0];
    ctm.b = matrix[1];
    ctm.c = matrix[2];
    ctm.d = matrix[3];
    ctm.e = matrix[4];
    ctm.f = matrix[5];
    (*env)->ReleasePrimitiveArrayCritical(env, matrixarray, matrix, 0);

    viewboxarr = (*env)->GetPrimitiveArrayCritical(env, viewboxarray, 0);
    viewbox.x0 = viewboxarr[0];
    viewbox.y0 = viewboxarr[1];
    viewbox.x1 = viewboxarr[2];
    viewbox.y1 = viewboxarr[3];
    (*env)->ReleasePrimitiveArrayCritical(env, viewboxarray, viewboxarr, 0);

    buffer = (*env)->GetPrimitiveArrayCritical(env, bufferarray, 0);
	LOGE("==================================MuPDFCore_renderPage2 005");
    fz_try(ctx)
    {
       page_list = fz_new_display_list(ctx); 
	   devPage = fz_new_list_device(ctx, page_list);
	   page = fz_load_page(ctx, doc, pagenum);
	   
	   fz_run_page(ctx, page, devPage, &fz_identity, NULL);
	   
	   //----------------------
	   
       pixmap = fz_new_pixmap_with_data(ctx, fz_device_bgr(ctx), viewbox.x1 - viewbox.x0, viewbox.y1 - viewbox.y0, (unsigned char*) buffer);

       fz_clear_pixmap_with_value(ctx, pixmap, 0xff);
	LOGE("==================================MuPDFCore_renderPage2 006");
       dev = fz_new_draw_device(ctx, pixmap);
       fz_run_display_list(ctx, page_list, dev, &ctm, &viewbox, NULL);
	LOGE("==================================MuPDFCore_renderPage2 007");
       fz_drop_pixmap(ctx, pixmap);
    }
    fz_always(ctx)
    {
    	fz_drop_device(ctx, dev);
    }
    fz_catch(ctx)
    {
        LOGE("Render failed");
    }
	LOGE("==================================MuPDFCore_renderPage2 008");
    (*env)->ReleasePrimitiveArrayCritical(env, bufferarray, buffer, 0);
    if (devPage) fz_drop_device(ctx, devPage); //FIXME:释放
    if (page_list) fz_drop_display_list(ctx, page_list); //FIXME:释放
    if (page) fz_drop_page(ctx, page); //FIXME:释放
    LOGE("==================================MuPDFCore_renderPage2 009");
}


//------------------------------------------------------
typedef struct CharacterHelper2_s CharacterHelper2;
typedef struct ArrayListHelper2_s ArrayListHelper2;
typedef struct PageTextBoxHelper2_s PageTextBoxHelper2;

struct CharacterHelper2_s
{
    JNIEnv* jenv;
    jclass cls;
    jmethodID midToLowerCase;
    int valid;
};

int CharacterHelper2_init(CharacterHelper2* that, JNIEnv* env)
{
    that->jenv = env;
    that->cls = (*(that->jenv))->FindClass(that->jenv, "java/lang/Character");
    if (that->cls)
    {
        that->midToLowerCase = (*(that->jenv))->GetStaticMethodID(that->jenv, that->cls, "toLowerCase", "(C)C");
    }
    that->valid = that->cls && that->midToLowerCase;
    return that->valid;
}

unsigned short CharacterHelper2_toLowerCase(CharacterHelper2* that, unsigned short ch)
{
    return that->valid ? (*(that->jenv))->CallStaticCharMethod(that->jenv, that->cls, that->midToLowerCase, ch) : ch;
}

struct ArrayListHelper2_s
{
    JNIEnv* jenv;
    jclass cls;
    jmethodID cid;
    jmethodID midAdd;
    int valid;
};

int ArrayListHelper2_init(ArrayListHelper2* that, JNIEnv* env)
{
    that->jenv = env;
    that->cls = (*(that->jenv))->FindClass(that->jenv, "java/util/ArrayList");
    if (that->cls)
    {
        that->cid = (*(that->jenv))->GetMethodID(that->jenv, that->cls, "<init>", "()V");
        that->midAdd = (*(that->jenv))->GetMethodID(that->jenv, that->cls, "add", "(Ljava/lang/Object;)Z");
    }
    that->valid = that->cls && that->cid && that->midAdd;
    return that->valid;
}

jobject ArrayListHelper2_create(ArrayListHelper2* that)
{
    return that->valid ? (*(that->jenv))->NewObject(that->jenv, that->cls, that->cid) : NULL;
}

void ArrayListHelper2_add(ArrayListHelper2* that, jobject arrayList, jobject obj)
{
    if (that->valid && arrayList)
    {
        (*(that->jenv))->CallBooleanMethod(that->jenv, arrayList, that->midAdd, obj);
    }
}

struct PageTextBoxHelper2_s
{
    JNIEnv* jenv;
    jclass cls;
    jmethodID cid;
    jfieldID fidLeft;
    jfieldID fidTop;
    jfieldID fidRight;
    jfieldID fidBottom;
    jfieldID fidText;
    int valid;
};

int PageTextBoxHelper2_init(PageTextBoxHelper2* that, JNIEnv* env)
{
    that->jenv = env;
    that->cls = (*(that->jenv))->FindClass(that->jenv, PACKAGENAME "/PageTextBox2");
    if (that->cls)
    {
        that->cid = (*(that->jenv))->GetMethodID(that->jenv, that->cls, "<init>", "()V");
        that->fidLeft = (*(that->jenv))->GetFieldID(that->jenv, that->cls, "left", "F");
        that->fidTop = (*(that->jenv))->GetFieldID(that->jenv, that->cls, "top", "F");
        that->fidRight = (*(that->jenv))->GetFieldID(that->jenv, that->cls, "right", "F");
        that->fidBottom = (*(that->jenv))->GetFieldID(that->jenv, that->cls, "bottom", "F");
        that->fidText = (*(that->jenv))->GetFieldID(that->jenv, that->cls, "text", "Ljava/lang/String;");
    }

    that->valid = that->cls && that->cid && that->fidLeft && that->fidTop && that->fidRight && that->fidBottom && that->fidText;
    return that->valid;
}

jobject PageTextBoxHelper2_create(PageTextBoxHelper2* that)
{
    return that->valid ? (*(that->jenv))->NewObject(that->jenv, that->cls, that->cid) : NULL;
}

jobject PageTextBoxHelper2_setRect(PageTextBoxHelper2* that, jobject ptb, const int* coords)
{
    if (that->valid && ptb)
    {
        (*(that->jenv))->SetFloatField(that->jenv, ptb, that->fidLeft, (jfloat) (float) coords[0]);
        (*(that->jenv))->SetFloatField(that->jenv, ptb, that->fidTop, (jfloat) (float) coords[1]);
        (*(that->jenv))->SetFloatField(that->jenv, ptb, that->fidRight, (jfloat) (float) coords[2]);
        (*(that->jenv))->SetFloatField(that->jenv, ptb, that->fidBottom, (jfloat) (float) coords[3]);
    }
    return ptb;
}

jobject PageTextBoxHelper2_setText(PageTextBoxHelper2* that, jobject ptb, jstring text)
{
    if (that->valid && ptb)
    {
        (*(that->jenv))->SetObjectField(that->jenv, ptb, that->fidText, text);
    }
    return ptb;
}

//-----------------------------


static int charat2(fz_context *ctx, fz_stext_page *page, int idx)
{
	fz_char_and_box cab;
	return fz_stext_char_at(ctx, &cab, page, idx)->c;
}

static fz_rect bboxcharat2(fz_context *ctx, fz_stext_page *page, int idx)
{
	fz_char_and_box cab;
	return fz_stext_char_at(ctx, &cab, page, idx)->bbox;
}

static int textlen2(fz_context *ctx, fz_stext_page *page)
{
	int len = 0;
	int block_num;

	for (block_num = 0; block_num < page->len; block_num++)
	{
		fz_stext_block *block;
		fz_stext_line *line;

		if (page->blocks[block_num].type != FZ_PAGE_BLOCK_TEXT)
			continue;
		block = page->blocks[block_num].u.text;
		for (line = block->lines; line < block->lines + block->len; line++)
		{
			fz_stext_span *span;

			for (span = line->first_span; span; span = span->next)
			{
				len += span->len;
			}
			len++; /* pseudo-newline */
		}
	}
	return len;
}

static int match2(fz_context *ctx, CharacterHelper2* ch, fz_stext_page *page, const char *s, int n)
{
    int orig = n;
    int c;

    while (*s)
    {
        s += fz_chartorune(&c, (char *) s);
        if (c == ' ' && charat2(ctx, page, n) == ' ')
        {
            while (charat2(ctx, page, n) == ' ')
            {
                n++;
            }
        }
        else
        {
            if (c != CharacterHelper2_toLowerCase(ch, charat2(ctx, page, n)))
            {
                return 0;
            }
            n++;
        }
    }
    return n - orig;
}

JNIEXPORT jobject/*jobjectArray*/ JNICALL
JNI_FN(MuPDFCore_search2)(JNIEnv * env, jobject thiz, jint pagenum, jstring text)
{
	LOGE("==================================MuPDFCore_search2 001");
	globals *glo = get_globals(env, thiz);
	if (glo == NULL)
		return;
	
	LOGE("==================================MuPDFCore_search2 002");
		
    //renderdocument_t *doc = (renderdocument_t*) (long) dochandle;
    //renderpage_t *page = (renderpage_t*) (long) pagehandle;
    // DEBUG("MuPdfPage(%p).search(%p, %p)", thiz, doc, page);
	fz_context *ctx = glo->ctx;
	fz_document *doc = glo->doc;
	fz_page *page = NULL;
	
	LOGE("==================================MuPDFCore_search2 003");
	
    if (!doc)
    {
        return NULL;
    }
    
	LOGE("==================================MuPDFCore_search2 004");
    const char *str = (*env)->GetStringUTFChars(env, text, NULL);
    if (str == NULL)
    {
        return NULL;
    }
    
    LOGE("==================================MuPDFCore_search2 005");

    ArrayListHelper2 alh;
    PageTextBoxHelper2 ptbh;
    CharacterHelper2 ch;

	LOGE("==================================MuPDFCore_search2 006");
	
    if (!ArrayListHelper2_init(&alh, env) || !PageTextBoxHelper2_init(&ptbh, env)|| !CharacterHelper2_init(&ch, env))
    {
        LOGE("search(): JNI helper initialization failed"/*, pagehandle*/);
        return NULL;
    }
    LOGE("==================================MuPDFCore_search2 007");
    jobject arrayList = ArrayListHelper2_create(&alh);
    // DEBUG("MuPdfPage(%p).search(%p, %p): array: %p", thiz, doc, page, arrayList);
    if (!arrayList)
    {
        return NULL;
    }
	LOGE("==================================MuPDFCore_search2 008");
    fz_rect *hit_bbox = NULL;

    fz_stext_sheet *sheet = NULL;
    fz_stext_page *pagetext = NULL;
    fz_device *dev = NULL;
    int pos;
    int len;
    int i, n;
    int hit_count = 0;
	LOGE("==================================MuPDFCore_search2 009");
    fz_try(ctx)
    {
		page = fz_load_page(ctx, doc, pagenum);
		LOGE("==================================MuPDFCore_search2 0010");
        if (page)
        {
			const fz_rect *rect;

			// DEBUG("MuPdfPage(%p).search(%p, %p): load page text", thiz, doc, page);

			fz_rect media_box;
			rect = fz_bound_page(ctx, page, &media_box);
			sheet = fz_new_stext_sheet(ctx);

			pagetext = fz_new_stext_page(ctx/*,rect*/);
			dev = fz_new_stext_device(ctx, sheet, pagetext);
			fz_run_page(ctx, page, dev, &fz_identity, NULL);

			// DEBUG("MuPdfPage(%p).search(%p, %p): free text device", thiz, doc, page);

			fz_drop_device(ctx, dev);
			dev = NULL;

			len = textlen2(ctx, pagetext);

			// DEBUG("MuPdfPage(%p).search(%p, %p): text length: %d", thiz, doc, page, len);
			LOGE("==================================MuPDFCore_search2 0011");
			for (pos = 0; pos < len; pos++)
			{
				fz_rect rremp = fz_empty_rect;
				fz_rect *rr = &rremp;
				// DEBUG("MuPdfPage(%p).search(%p, %p): match %d", thiz, doc, page, pos);

				n = match2(ctx, &ch, pagetext, str, pos);
				if (n > 0)
				{
	//                DEBUG("MuPdfPage(%p).search(%p, %p): match found: %d, %d", thiz, doc, page, pos, n);
					for (i = 0; i < n; i++)
					{
						fz_rect temp;
						temp = bboxcharat2(ctx, pagetext, pos + i);
						rr = fz_union_rect(rr, &temp);
					}

					if (!fz_is_empty_rect(rr))
					{
						int coords[4];
						coords[0] = (rr->x0);
						coords[1] = (rr->y0);
						coords[2] = (rr->x1);
						coords[3] = (rr->y1);
	//                    DEBUG("MuPdfPage(%p).search(%p, %p): found rectangle (%d, %d - %d, %d)", thiz, doc, page, coords[0], coords[1], coords[2], coords[3]);
						jobject ptb = PageTextBoxHelper2_create(&ptbh);
						if (ptb)
						{
							// DEBUG("MuPdfPage(%p).search(%p, %p): rect %p", thiz, doc, page, ptb);
							PageTextBoxHelper2_setRect(&ptbh, ptb, coords);
							// PageTextBoxHelper2_setText(&ptbh, ptb, txt);
							// DEBUG("MuPdfPage(%p).search(%p, %p): add rect %p to array %p", thiz, doc, page, ptb, arrayList);
							ArrayListHelper2_add(&alh, arrayList, ptb);
						}
					}
				}
			}
		}
        LOGE("==================================MuPDFCore_search2 012");
    } 
    fz_always(ctx)
    {
        // DEBUG("MuPdfPage(%p).search(%p, %p): free resources", thiz, doc, page);
        if (pagetext)
        {
            fz_drop_stext_page(ctx, pagetext);
        }
        if (sheet)
        {
            fz_drop_stext_sheet(ctx, sheet);
        }
        if (dev)
        {
            fz_drop_device(ctx, dev);
        }
    }
    fz_catch(ctx)
    {
        jclass cls;
        (*env)->ReleaseStringUTFChars(env, text, str);
        cls = (*env)->FindClass(env, "java/lang/OutOfMemoryError");
        if (cls != NULL)
        {
            (*env)->ThrowNew(env, cls, "Out of memory in MuPDFCore_searchPage");
        }
        (*env)->DeleteLocalRef(env, cls);
        return NULL;
    }
	LOGE("==================================MuPDFCore_search2 013");
    (*env)->ReleaseStringUTFChars(env, text, str);
	
	if (page) fz_drop_page(ctx, page); //FIXME:释放
	LOGE("==================================MuPDFCore_search2 014");
    return arrayList;
}


