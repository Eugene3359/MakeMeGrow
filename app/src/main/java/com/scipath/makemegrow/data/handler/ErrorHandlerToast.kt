package com.scipath.makemegrow.data.handler

import android.content.Context
import android.util.Log
import android.widget.Toast

object ErrorHandlerToast: ErrorHandler {
    override fun handle(exception: Exception, context: Context) {
        Log.e("ErrorHandler", "Error occurred", exception)
        Toast.makeText(context, "Unexpected error occurred.", Toast.LENGTH_LONG).show()
    }
}