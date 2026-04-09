package com.scipath.makemegrow.data.handler

import android.content.Context

interface ErrorHandler {
    fun handle(exception: Exception, context: Context)
}