package com.scipath.makemegrow.ui.toast

import android.app.Activity
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.scipath.makemegrow.R

class AppToast private constructor(
    private val activity: Activity,
    private val message: String,
    private val activityDecor: View,
    private val parentDecor: View
) {

    companion object {
        private const val DISPLAY_DURATION_MS = 3750L
        private const val BOTTOM_OFFSET_DP  = 40

        fun makeText(
            activity: Activity,
            message: String,
            parentDecor: View = activity.window.decorView
        ): AppToast {
            return AppToast(
                activity = activity,
                message = message,
                activityDecor = activity.window.decorView,
                parentDecor = parentDecor
            )
        }
    }

    @MainThread
    fun show() {
        if (activity.isFinishing || activity.isDestroyed) return
        if (!activityDecor.isAttachedToWindow ||
            !parentDecor.isAttachedToWindow ||
            activityDecor.height == 0 ||
            parentDecor.height == 0
        ) return

        val view = LayoutInflater.from(activity)
            .inflate(R.layout.layout_toast, null, false)

        view.findViewById<TextView>(R.id.text_message).text = message

        val popupWindow = PopupWindow(
            view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            isOutsideTouchable = false
            isFocusable = false
            isTouchable = false
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }

        val insets = ViewCompat.getRootWindowInsets(activityDecor)
        val bottomInset = insets?.getInsets(
            WindowInsetsCompat.Type.systemBars()
        )?.bottom ?: 0
        val bottomOffset = bottomInset + activity.dpToPx(BOTTOM_OFFSET_DP)

        popupWindow.showAtLocation(
            parentDecor,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL,
            0,
            if (activityDecor === parentDecor) {
                bottomOffset
            } else {
                val activityLocation = IntArray(2)
                val parentLocation = IntArray(2)
                activityDecor.getLocationOnScreen(activityLocation)
                parentDecor.getLocationOnScreen(parentLocation)
                val activityBottom = activityLocation[1] + activityDecor.height
                val parentBottom = parentLocation[1] + parentDecor.height
                parentBottom - activityBottom + bottomOffset
            }
        )

        val enterAnimation = AnimationUtils.loadAnimation(
            activity,
            R.anim.fade_in
        )

        view.startAnimation(enterAnimation)

        Handler(Looper.getMainLooper()).postDelayed({
            if (activity.isFinishing || activity.isDestroyed) {
                popupWindow.dismiss()
                return@postDelayed
            }

            if (popupWindow.isShowing) {
                val exitAnimation = AnimationUtils.loadAnimation(
                    activity,
                    R.anim.fade_out
                )

                exitAnimation.setAnimationListener(
                    object : Animation.AnimationListener {
                        override fun onAnimationStart(animation: Animation?) {}
                        override fun onAnimationRepeat(animation: Animation?) {}
                        override fun onAnimationEnd(animation: Animation?) {
                            popupWindow.dismiss()
                        }
                    }
                )

                view.startAnimation(exitAnimation)
            }
        }, DISPLAY_DURATION_MS)
    }

    private fun Activity.dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}