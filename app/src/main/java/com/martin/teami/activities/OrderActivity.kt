package com.martin.teami.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.widget.*
import com.martin.teami.R
import com.martin.teami.models.Item
import com.martin.teami.retrofit.RepresentativesInterface
import kotlinx.android.synthetic.main.activity_order.*
import kotlinx.android.synthetic.main.order_item_layout.view.*
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OrderActivity : AppCompatActivity() {

    private lateinit var allItems: List<Item>
    private var itemsOrdered: ArrayList<Item> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        val iteems=intent.getParcelableArrayListExtra<Item>("ITEMS_ORDERED")
        if (iteems!=null) {
            itemsOrdered = iteems
            for(item in itemsOrdered){
                addItem(item.name,item.itemId.toString(),item.quantity.toString())
            }
        }

        doneOrderFAB.setOnClickListener {
            itemsOrdered.clear()
            for (i in 0 until itemsLinLay.childCount) {
                val cardView = itemsLinLay.getChildAt(i) as CardView
                val constraintLayout = cardView.getChildAt(0) as ConstraintLayout
                val editText = constraintLayout.getChildAt(1) as EditText
                val quantity = editText.text.toString()
                val id = cardView.id
                itemsOrdered.add(allItems[id - 1])
                itemsOrdered.last().quantity = quantity.toInt()
            }
            intent.putParcelableArrayListExtra("ITEMS_ORDERED", itemsOrdered)
            setResult(101, intent)
            this.finish()
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("https://demo3577815.mockable.io/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val itemsInterface = retrofit.create(RepresentativesInterface::class.java)
        val booksResponse = itemsInterface.getBooks()
        booksResponse.enqueue(object : Callback<List<Item>> {
            override fun onFailure(call: retrofit2.Call<List<Item>>, t: Throwable) {
                Toast.makeText(this@OrderActivity, t.message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: retrofit2.Call<List<Item>>, response: Response<List<Item>>) {
                response.body()?.let {
                    val items = it
                    allItems = items
                    prepareItems(items)
                }
            }
        })
    }

    private fun prepareItems(items: List<Item>) {
        val itemNames = mutableListOf<String>()
        items.forEach {
            itemNames.add(it.name)
        }
        val arrayAdapter = ArrayAdapter<String>(this, R.layout.text_view_layout, itemNames)
        autoCompleteTextView.setAdapter(arrayAdapter)
        autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val textview = view as TextView
                addItem(textview.text.toString(), items[itemNames.indexOf(textview.text)].itemId.toString(),null)
                autoCompleteTextView.text.clear()
                arrayAdapter.remove(textview.text.toString())
            }
    }

    private fun addItem(title: String?, id: String?,quantity:String?) {
        val view = LayoutInflater.from(this).inflate(R.layout.order_item_layout, null)
        view.itemNameTV.text = title
        view.quantityET.setText(quantity)
        val laypar =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        laypar.setMargins(8, 16, 8, 16)
        view.layoutParams = laypar
        id?.let {
            view.id = it.toInt()
        }
        view.removeItemIV.setOnClickListener {
            removeItem(view.id.toString())
        }
        itemsLinLay.addView(view)

    }

    private fun removeItem(id: String) {
        val unselected = findViewById<CardView>(id.toInt())
        itemsLinLay.removeView(unselected)
    }
}
