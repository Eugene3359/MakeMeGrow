package com.scipath.makemegrow.data.handler

import android.content.Context
import android.util.Log
import android.widget.Toast

object ExceptionHandlerToast: ExceptionHandler {
    override fun handle(exception: Exception, context: Context) {
        Log.e("ExceptionHandler", "Exception has occurred", exception)
        Toast.makeText(context, "Exception has occurred.", Toast.LENGTH_LONG).show()
    }
}