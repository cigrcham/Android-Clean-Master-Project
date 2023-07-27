package com.phonecleaner.storagecleaner.cache.utils

import timber.log.Timber

/**
 * Created by Thinhvh on 23/09/2022.
 * Phone: 0398477967
 * Email: thinhvh.fpt@gmail.com
 */

class DebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String? {
        return String.format(
            "C:%s:%s",
            super.createStackElementTag(element),
            element.lineNumber
        )
    }
}