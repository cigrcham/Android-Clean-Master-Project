//package com.phonecleaner.storagecleaner.cache.utils
//
//import android.content.Context
//import android.content.SharedPreferences
//import com.phonecleaner.storagecleaner.cache.extension.editMe
//import com.phonecleaner.storagecleaner.cache.extension.put
//import dagger.hilt.android.qualifiers.ApplicationContext
//import javax.inject.Inject
//import javax.inject.Singleton
//
///**
// * Created by Thinhvh on 23/09/2022.
// * Phone: 0398477967
// * Email: thinhvh.fpt@gmail.com
// */
//
//@Singleton
//class PreferenceHelper @Inject constructor(@ApplicationContext private val context: Context) {
//
//    var sharedPreferences: SharedPreferences =
//        context.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE)
//
//    var isFirstTimeLaunched
//        get() = sharedPreferences.getBoolean(FIRST_TIME_LAUNCHED, true)
//        set(value) {
//            sharedPreferences.editMe {
//                it.put(FIRST_TIME_LAUNCHED to value)
//            }
//        }
//
//    companion object {
//        private const val SHARED_PREFERENCE = "SHARED_PREFERENCE"
//        private const val FIRST_TIME_LAUNCHED = "FIRST_TIME_LAUNCHED"
//    }
//}