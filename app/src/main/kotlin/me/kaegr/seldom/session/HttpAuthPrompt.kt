package me.kaegr.seldom.session

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout

object HttpAuthPrompt {
    fun show(
        context: Context,
        title: String,
        onSubmit: (user: String, pass: String, remember: Boolean) -> Unit,
        onCancel: () -> Unit,
    ) {
        val user = EditText(context).apply { hint = "Username" }
        val pass = EditText(context).apply {
            hint = "Password"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val remember = CheckBox(context).apply { text = "Remember" }
        val box = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val padding = (16 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, 0)
            addView(user)
            addView(pass)
            addView(remember)
        }
        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(box)
            .setPositiveButton("Log in") { _, _ ->
                onSubmit(user.text.toString(), pass.text.toString(), remember.isChecked)
            }
            .setNegativeButton("Cancel") { _, _ -> onCancel() }
            .setOnCancelListener { onCancel() }
            .show()
    }
}
