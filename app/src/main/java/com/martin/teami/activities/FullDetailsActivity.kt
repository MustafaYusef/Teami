package com.martin.teami.activities

import android.app.Dialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.martin.teami.R
import com.martin.teami.models.Item
import kotlinx.android.synthetic.main.activity_full_details.*
import kotlinx.android.synthetic.main.feedback_popup.*
import kotlinx.android.synthetic.main.items_ordered.view.*

class FullDetailsActivity : AppCompatActivity() {

    private var itemsOrdered: ArrayList<Item>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_details)

        orderBtn.setOnClickListener {
            val intent = Intent(this, OrderActivity::class.java)
            if (itemsOrdered != null) {
                intent.putParcelableArrayListExtra("ITEMS_ORDERED", itemsOrdered)
            }
            startActivityForResult(intent, 101)
        }

        feedbackBtn.setOnClickListener {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.feedback_popup)
            dialog.show()
            var noteFull =""
            var ratingFull = 0f
            if (ratingFull != null && !noteFull.isNullOrEmpty() && noteFull.isNotBlank()) {
                dialog.feedbackNoteET.setText(noteFull)
                dialog.feedbackRatingBar.rating = ratingFull
            }
            dialog.doneFeedbackBtn.setOnClickListener {
                val rating = dialog.feedbackRatingBar.rating
                val note = dialog.feedbackNoteET.text.toString()
                if (rating != null && !note.isNullOrEmpty() && note.isNotBlank()) {
                    ratingFull = rating
                    noteFull = note
                    dialog.dismiss()
                } else Toast.makeText(
                    this@FullDetailsActivity,
                    "Please, fill all the fields!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {
//        ordersLinLay.removeAllViews()
//        val items = itemsOrdered
//        if (items != null) {
//            for (item in items) {
//                addItem(item.name, item.itemId.toString(), item.quantity.toString())
//            }
//        }
        super.onResume()
    }

    private fun addItem(title: String?, id: String?, quantity: String?) {
//        val view = LayoutInflater.from(this).inflate(R.layout.items_ordered, null)
//        view.itemNameTV.text = title
//        view.quantityTV.text = quantity
//        val laypar =
//            LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT
//            )
//        laypar.setMargins(8, 16, 8, 16)
//        view.layoutParams = laypar
//        id?.let {
//            view.id = it.toInt()
//        }
//        ordersLinLay.addView(view)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            101 -> {
                itemsOrdered = data?.getParcelableArrayListExtra<Item>("ITEMS_ORDERED")
            }
            else -> return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
