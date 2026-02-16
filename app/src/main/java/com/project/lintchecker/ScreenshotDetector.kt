package com.project.lintchecker

import android.app.Activity
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.lang.ref.WeakReference

object ScreenshotDetector {
    private const val DEBOUNCE_DELAY = 1000L
    private var lastScreenshotTimestamp = 0L

    private var contentObserver: ContentObserver? = null
    private var activityRef: WeakReference<Activity>? = null

    fun register(activity: Activity) {
        activityRef = WeakReference(activity)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            registerScreenCaptureAPI(activity)
        } else {
            registerMediaStoreObserver(activity)
        }
    }

    fun unregister() {
        val activity = activityRef?.get() ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            activity.unregisterScreenCaptureCallback {}
        } else {
            contentObserver?.let {
                activity.contentResolver.unregisterContentObserver(it)
            }
        }
        contentObserver = null
        activityRef = null
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun registerScreenCaptureAPI(activity: Activity) {
        activity.registerScreenCaptureCallback(activity.mainExecutor) {
            onScreenshotEvent()
        }
    }

    private fun registerMediaStoreObserver(activity: Activity) {
        contentObserver =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(
                    selfChange: Boolean,
                    uri: Uri?,
                ) {
                    super.onChange(selfChange, uri)
                    if (uri != null) handleMediaStoreChange(uri)
                }
            }
        activity.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver!!,
        )
    }

    private fun handleMediaStoreChange(uri: Uri) {
        val activity = activityRef?.get() ?: return

        activity.contentResolver
            .query(
                uri,
                arrayOf(MediaStore.Images.Media.DISPLAY_NAME, MediaStore.MediaColumns.RELATIVE_PATH),
                null,
                null,
                null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val name = cursor.getString(0)
                    val path = cursor.getString(1)
                    if (isScreenshotPath(name, path)) onScreenshotEvent()
                }
            }
    }

    private fun isScreenshotPath(
        name: String?,
        path: String?,
    ): Boolean {
        val text = "${name.orEmpty()}${path.orEmpty()}".lowercase()
        return listOf(
            "screenshot",
            "screen_shot",
            "screen-shot",
            "screencapture",
            "screen_capture",
        ).any { text.contains(it) }
    }

    private fun onScreenshotEvent() {
        val now = System.currentTimeMillis()
        if (now - lastScreenshotTimestamp < DEBOUNCE_DELAY) return
        lastScreenshotTimestamp = now

        // 🔔 Emit event or show UI warning here

        Toast.makeText(activityRef?.get(), "No SS allowed", Toast.LENGTH_LONG).show()
    }
}
