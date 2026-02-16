package com.project.lintchecker

import android.app.Activity
import android.app.Application
import android.os.Bundle

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(ScreenshotActivityLifecycleCallbacks())
    }

    private class ScreenshotActivityLifecycleCallbacks : ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
            ScreenshotDetector.register(activity)
        }

        override fun onActivityPaused(activity: Activity) {
            ScreenshotDetector.unregister()
        }

        override fun onActivityCreated(
            activity: Activity,
            savedInstanceState: Bundle?,
        ) {}

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(
            activity: Activity,
            outState: Bundle,
        ) {}

        override fun onActivityDestroyed(activity: Activity) {}
    }
}
