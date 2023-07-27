package com.phonecleaner.storagecleaner.cache.data.model.liveData

class State<T> {

    private var status: DataStatus = DataStatus.CREATED

    private var data: T? = null

    private var errorData: T? = null

    private var errorMsg: String? = null

    fun loading(): State<T> {
        status = DataStatus.LOADING
        data = null
        errorMsg = null
        return this
    }

    fun success(data: T): State<T> {
        status = DataStatus.SUCCESS
        this.data = data
        errorMsg = null
        return this
    }

    fun error(errorMsg: String): State<T> {
        status = DataStatus.ERROR
        data = null
        this.errorMsg = errorMsg
        return this
    }

    fun error(errorData: T): State<T> {
        status = DataStatus.ERROR
        data = null
        this.errorData = errorData
        return this
    }

    fun getStatus(): DataStatus {
        return status
    }

    fun getData(): T? {
        return data
    }


    fun getErrorMsg(): String? {
        return errorMsg
    }


    fun getErrorData(): T? {
        return errorData
    }

    enum class DataStatus {
        CREATED, SUCCESS, ERROR, LOADING
    }
}