package com.phonecleaner.storagecleaner.cache.utils

import android.os.Build


fun isOreoPlus() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
