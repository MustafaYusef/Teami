package com.martin.teami.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import com.martin.teami.R

fun showMessageOK(activity: Activity,title: String, message: String, okListener: DialogInterface.OnClickListener) {
    AlertDialog.Builder(activity)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(activity.getString(R.string.okDialog), okListener)
        .create()
        .show()
}

fun showMessageOKCancel(activity: Activity,
    title: String,
    message: String,
    okListener: DialogInterface.OnClickListener,
    cancelListener: DialogInterface.OnClickListener
) {
    AlertDialog.Builder(activity)
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(activity.getString(R.string.okDialog), okListener)
        .setNegativeButton(activity.getString(R.string.cancelDialog), cancelListener)
        .create()
        .show()
}