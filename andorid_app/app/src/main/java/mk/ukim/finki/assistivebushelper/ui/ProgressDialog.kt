package mk.ukim.finki.assistivebushelper.ui

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import assistivebushelper.R

class ProgressDialog(context: Context) {
    private val buildContext: Context = context
    private val llPadding = 30

    fun build(): AlertDialog {
        val ltbPadding = 100
        val ll = LinearLayout(buildContext)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, ltbPadding, llPadding, ltbPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam

        val progressBar = createBar(llParam)

        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER

        val tvText = createTextView(llParam)

        ll.addView(progressBar)
        ll.addView(tvText)

        val builder = AlertDialog.Builder(buildContext)
        builder.setCancelable(false)
        builder.setView(ll)

        return createDialog(builder)
    }

    private fun createBar(llParam: LinearLayout.LayoutParams): ProgressBar {
        val progressBar = ProgressBar(buildContext)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam
        return progressBar
    }

    private fun createTextView(llParam: LinearLayout.LayoutParams): TextView {
        val tvText = TextView(buildContext)
        tvText.text = buildContext.resources.getText(R.string.progress_dialog_message)
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20f
        tvText.layoutParams = llParam
        return tvText
    }

    private fun createDialog(builder: AlertDialog.Builder): AlertDialog {
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