package com.twilightkhq.base

import android.content.Context

class PathUtils {

    fun getAppDirPath(context: Context): String? {
        return context.getExternalFilesDir(null)?.absolutePath
    }
}