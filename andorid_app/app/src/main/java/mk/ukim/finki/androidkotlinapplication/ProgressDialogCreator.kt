package mk.ukim.finki.androidkotlinapplication

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

class ProgressDialogCreator {
    companion object {
        fun create(context: Context): AlertDialog {
            val llPadding = 30
            val ltbPadding = 100
            val ll = LinearLayout(context)
            ll.orientation = LinearLayout.HORIZONTAL
            ll.setPadding(llPadding, ltbPadding, llPadding, ltbPadding)
            ll.gravity = Gravity.CENTER
            var llParam = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            llParam.gravity = Gravity.CENTER
            ll.layoutParams = llParam

            val progressBar = ProgressBar(context)
            progressBar.isIndeterminate = true
            progressBar.setPadding(0, 0, llPadding, 0)
            progressBar.layoutParams = llParam

            llParam = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            llParam.gravity = Gravity.CENTER
            val tvText = TextView(context)
            tvText.text = context.resources.getText(R.string.progress_dialog_message)
            tvText.setTextColor(Color.parseColor("#000000"))
            tvText.textSize = 20f
            tvText.layoutParams = llParam

            ll.addView(progressBar)
            ll.addView(tvText)

            val builder = AlertDialog.Builder(context)
            builder.setCancelable(false)
            builder.setView(ll)

            val dialog = builder.create()
            val window = dialog.window
            if (window != null) {
                val layoutParams = WindowManager.LayoutParams()
                layoutParams.copyFrom(dialog.window!!.attributes)
                layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                dialog.window!!.attributes = layoutParams
            }

            return dialog
        }
    }
}