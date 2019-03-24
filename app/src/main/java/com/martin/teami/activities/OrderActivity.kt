package com.martin.teami.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.widget.*
import com.martin.teami.R
import com.martin.teami.models.*
import com.martin.teami.retrofit.RepresentativesInterface
import com.martin.teami.utils.Consts.BASE_URL
import com.martin.teami.utils.Consts.LOGIN_RESPONSE_SHARED
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_order.*
import kotlinx.android.synthetic.main.order_item_layout.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OrderActivity : AppCompatActivity() {

    private lateinit var allItems: List<Item>
    private var itemsOrdered: ArrayList<ItemsOrdered> = arrayListOf()
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        doneOrderFAB.setOnClickListener {
            itemsOrdered.clear()
            for (i in 0 until itemsLinLay.childCount) {
                val cardView = itemsLinLay.getChildAt(i) as CardView
                val constraintLayout = cardView.getChildAt(0) as ConstraintLayout
                val editText = constraintLayout.getChildAt(1) as EditText
                val quantity = editText.text.toString()
                var item: Item? = null
                val id = cardView.id
                allItems.forEach {
                    if (it.id == id) {
                        item = it
                    }
                }
                item?.let {
                    itemsOrdered.add(ItemsOrdered(it.id, it.description.toFloat()))
                }
                itemsOrdered.last().quantity = quantity.toFloat()
            }
            postOrder()
        }

        val loginResponse = Hawk.get<LoginResponse>(LOGIN_RESPONSE_SHARED)
        if (loginResponse != null) {
            token = loginResponse.token
        }
        getItems()
    }

    private fun getItems() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val itemsInterface = retrofit.create(RepresentativesInterface::class.java)
        val itemsResponse = itemsInterface.getItems(token, getID())
        itemsResponse.enqueue(object : Callback<ItemsResponse> {
            override fun onFailure(call: retrofit2.Call<ItemsResponse>, t: Throwable) {
                Toast.makeText(this@OrderActivity, t.message, Toast.LENGTH_LONG).show()
            }

            override fun onResponse(call: retrofit2.Call<ItemsResponse>, response: Response<ItemsResponse>) {
                response.body()?.let {
                    val items = it.items
                    allItems = items
                    prepareItems(items)
                }
            }
        })
    }

    private fun postOrder() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val resource: MyResources = intent.getParcelableExtra("RESOURCE")
        val orderRequest = OrderRequest(token, getID(), itemsOrdered, resource.resourceType, resource.id)
        val orderCallback = retrofit.create(RepresentativesInterface::class.java).postOrder(orderRequest)
            .enqueue(object : Callback<OrderResponse> {
                override fun onFailure(call: Call<OrderResponse>, t: Throwable) {

                }

                override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {

                }
            })
    }

    private fun prepareItems(items: List<Item>) {
        val arrayAdapter = ArrayAdapter<Item>(this, R.layout.text_view_layout, items)
        autoCompleteTextView.setAdapter(arrayAdapter)
        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.showDropDown()
        }
        autoCompleteTextView.threshold = 0
        autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val textview = view as TextView
                addItem(textview.text.toString(), arrayAdapter.getItem(position).id.toString(), null)
                autoCompleteTextView.text.clear()
//                arrayAdapter.remove(arrayAdapter.getItem(position))
//                arrayAdapter.notifyDataSetChanged()
            }
    }

    private fun addItem(title: String?, id: String?, quantity: String?) {
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


    fun getID(): String {
        return Settings.Secure.getString(this@OrderActivity.contentResolver, Settings.Secure.ANDROID_ID)
    }
}
