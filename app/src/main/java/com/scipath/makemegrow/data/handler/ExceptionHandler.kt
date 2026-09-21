package com.scipath.makemegrow.data.handler

import android.content.Context

interface ExceptionHandler {
    fun handle(exception: Exception, context: Context)
}