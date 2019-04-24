package com.martin.teami.activities

import android.content.DialogInterface
import android.os.Build
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
import com.martin.teami.utils.getID
import com.martin.teami.utils.showMessageOK
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_order.*
import kotlinx.android.synthetic.main.items_ordered.view.*
import kotlinx.android.synthetic.main.order_item_layout.view.itemNameTV
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.function.Predicate

class OrderActivity : AppCompatActivity() {

    private var selectedItems: ArrayList<Item> = arrayListOf()
    private lateinit var allItems: List<Item>
    private var item_ids: ArrayList<Int> = arrayListOf()
    private var quantities: ArrayList<Float> = arrayListOf()
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        doneOrderBtn.setOnClickListener {
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
        val itemsResponse = itemsInterface.getItems(token, getID(this))
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
        val orderRequest = OrderRequest(token, getID(this), item_ids, quantities, resource.resourceType, resource.id)
        val orderCallback = retrofit.create(RepresentativesInterface::class.java).postOrder(orderRequest)
            .enqueue(object : Callback<OrderResponse> {
                override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                    Toast.makeText(this@OrderActivity,t.message,Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                    if (response.body()?.order != null) {
                        showMessageOK(this@OrderActivity,
                            getString(R.string.done),
                            getString(R.string.order_successful),
                            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
                    } else if (response.code() == 406) {
                        val converter = retrofit.responseBodyConverter<ErrorResponse>(
                            ErrorResponse::class.java,
                            arrayOfNulls<Annotation>(0)
                        )
                        val errors = converter.convert(response.errorBody())
                        Toast.makeText(this@OrderActivity, errors?.error, Toast.LENGTH_SHORT).show()
                    } else if (response.code() == 400 || response.code() == 422) {
                        val converter = retrofit.responseBodyConverter<ErrorResponseArray>(
                            ErrorResponseArray::class.java,
                            arrayOfNulls<Annotation>(0)
                        )
                        val errors = converter.convert(response.errorBody())
                        Toast.makeText(this@OrderActivity, errors?.error?.get(0), Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    private fun prepareItems(items: List<Item>) {
        val arrayAdapter = ArrayAdapter<Item>(this, R.layout.text_view_layout, items)
        ItemNameTV.setAdapter(arrayAdapter)
        ItemNameTV.setOnClickListener {
            ItemNameTV.showDropDown()
        }
        ItemNameTV.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                ItemNameTV.showDropDown()
            }
        }
        ItemNameTV.threshold = 0
        ItemNameTV.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val item = arrayAdapter.getItem(position) as Item
                ItemNameTV.setText(item.name)
                addItemBtn.setOnClickListener {
                    if (quantityET.text.isNotBlank() && ItemNameTV.text.isNotBlank()) {
                        quantities.add(quantityET.text.toString().toFloat())
                        item_ids.add(item.id)
                        selectedItems.add(item)
                        ItemNameTV.text.clear()
                        quantityET.text.clear()
                        addItem()
                    }
                }
            }
    }

    private fun addItem() {
        val view = LayoutInflater.from(this).inflate(R.layout.items_ordered, null)
        view.itemNameTV.text = selectedItems.last().name
        view.quantityTV.text = quantities.last().toString()
        val laypar =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        laypar.setMargins(8, 16, 8, 16)
        view.layoutParams = laypar
        view.id = selectedItems.last().id
        view.rmvItemBtn.setOnClickListener {
            removeItem(view.id.toString())
        }
        itemsLinLay.addView(view)

    }

    private fun removeItem(id: String) {
        val unselected = findViewById<CardView>(id.toInt())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            selectedItems.removeIf { t -> t.id == id.toInt() }
            item_ids.removeIf { t ->
                quantities.removeIf{t2->quantities.indexOf(t2)==item_ids.indexOf(t)}
                t == id.toInt()
            }
            itemsLinLay.removeView(unselected)
        }
    }
}
