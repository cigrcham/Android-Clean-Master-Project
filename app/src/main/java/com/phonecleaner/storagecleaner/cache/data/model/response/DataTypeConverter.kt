package com.phonecleaner.storagecleaner.cache.data.model.response

import androidx.annotation.Keep
import androidx.room.TypeConverter
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileApp
import com.phonecleaner.storagecleaner.cache.data.model.entity.FileDelete
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

@Keep
class DataTypeConverter {

    private var gson = Gson()

    @TypeConverter
    fun stringToFileAppArrayList(data: String?): MutableList<FileApp>? {
        val listType: Type = object : TypeToken<MutableList<FileApp>>() {}.type
        return gson.fromJson<MutableList<FileApp>>(data, listType)
    }

    @TypeConverter
    fun fileAppArrayListToString(someObjects: MutableList<FileApp>?): String? {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun stringToFileDeleteArrayList(data: String?): ArrayList<FileDelete>? {
        val listType: Type = object : TypeToken<ArrayList<FileDelete>>() {}.type
        return gson.fromJson<ArrayList<FileDelete>>(data, listType)
    }

    @TypeConverter
    fun fileDeleteArrayListToString(someObjects: ArrayList<FileDelete>?): String? {
        return gson.toJson(someObjects)
    }
}