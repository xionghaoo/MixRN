package xh.zero.mixproj2.Utils

import android.util.Log
import xh.zero.mixproj2.BuildConfig

class Logger {
    companion object {
        private const val DEFAULT_TAG = "appLog"

        fun e(log: String, tag: String = DEFAULT_TAG) {
            if (BuildConfig.DEBUG) {
                Log.e(tag, log)
            }
        }

        fun d(log: String, tag: String = DEFAULT_TAG) {
            if (BuildConfig.DEBUG) {
                Log.d(tag, log)
            }
        }
    }
}