package com.croczi.teami.activities

import android.content.DialogInterface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import com.croczi.teami.R
import com.croczi.teami.database.entitis.FeedbackRequestLocal
import com.croczi.teami.database.entitis.OrderRequestLocal
import com.croczi.teami.models.*
import com.croczi.teami.retrofit.NetworkTools
import com.croczi.teami.retrofit.RepresentativesInterface
import com.croczi.teami.retrofit.addTLSSupport
import com.croczi.teami.utils.Consts
import com.croczi.teami.utils.Consts.BASE_URL
import com.croczi.teami.utils.Consts.LOGIN_RESPONSE_SHARED
import com.croczi.teami.utils.checkUser
import com.croczi.teami.utils.getID
import com.croczi.teami.utils.showMessageOK
import com.mustafayusef.holidaymaster.utils.corurtins
import com.mustafayusef.sharay.database.databaseApp
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_full_details.*
import kotlinx.android.synthetic.main.activity_order.*
import kotlinx.android.synthetic.main.items_ordered.view.*
import kotlinx.android.synthetic.main.order_item_layout.view.itemNameTV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

import kotlin.collections.ArrayList

class OrderActivity : AppCompatActivity() {

    private var selectedItems: ArrayList<Item> = arrayListOf()
    private lateinit var allItems: List<Item>
    private var item_ids: ArrayList<Int> = arrayListOf()
    private var quantities: ArrayList<String> = arrayListOf()
    private lateinit var token: String
    var db: databaseApp?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)
        db= this?.let { databaseApp(it) }

        doneOrderBtn.setOnClickListener {
            postOrder()
        }
        val resource: MyResources = intent.getParcelableExtra("RESOURCE")!!
        if(resource.resourceType=="pharmacies"){
            resourceIconOrder.setImageResource(R.drawable.ic_pharmacy_small)
        }

        val loginResponse = Hawk.get<LoginResponse>(LOGIN_RESPONSE_SHARED)
        if (loginResponse != null) {
            token = loginResponse.token
        }
        getItems()
    }
    private fun getItems() {
        var allItemsLocal= mutableListOf<Item>()
        token?.let {
            corurtins.main {
                var itemsLocal=db?.items_Dao()!!.getAllItems()
                for(i in 0 until itemsLocal.size){
                    var item=Item(id=itemsLocal[i].id,
                        name=itemsLocal[i].name,
                        description=itemsLocal[i].description,
                        companyName=itemsLocal[i].companyName)
                    allItemsLocal.add(item)
                }
                CoroutineScope(Dispatchers.Main).launch {
                    allItems = allItemsLocal
                    prepareItems(allItems)
                }

            }

        }
    }
//    private fun getItems() {
//        var retrofitBuilder = Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .addConverterFactory(GsonConverterFactory.create())
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
//            retrofitBuilder.addTLSSupport()
//        }
//        val retrofit = retrofitBuilder.build()
//        val itemsInterface = retrofit.create(RepresentativesInterface::class.java)
//        val itemsResponse = itemsInterface.getItems(token, getID(this))
//        itemsResponse.enqueue(object : Callback<ItemsResponse> {
//            override fun onFailure(call: retrofit2.Call<ItemsResponse>, t: Throwable) {
//                Toast.makeText(this@OrderActivity, t.message, Toast.LENGTH_LONG).show()
//            }
//
//            override fun onResponse(
//                call: retrofit2.Call<ItemsResponse>,
//                response: Response<ItemsResponse>
//            ) {
//                response.body()?.let {
//                    val items = it.items
//                    allItems = items
//                    prepareItems(items)
//                }
//            }
//        })
//    }


    private fun postOrder() {
        orderProgress?.visibility=View.VISIBLE
        doneOrderBtn?.visibility=View.GONE
        val current =Calendar.getInstance().time
        val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        val formatedDate = sdf.format(current)
        val resource: MyResources = intent.getParcelableExtra("RESOURCE")!!
        val orderRequest = OrderRequest(
            token,
            getID(this),
            item_ids,
            quantities,
            resource.resourceType,
            resource.id,formatedDate.toString()
        )
        if(!item_ids.isNullOrEmpty()){
            NetworkTools.postOrder(orderRequest, {
                orderProgress?.visibility=View.GONE
                doneOrderBtn?.visibility=View.VISIBLE
                showMessageOK(this@OrderActivity,
                    getString(R.string.done),
                    getString(R.string.order_successful),
                    DialogInterface.OnClickListener { dialog, which -> dialog.dismiss()
                        this@OrderActivity.finish()})

            }, { message ->
                orderProgress?.visibility=View.GONE
                doneOrderBtn?.visibility=View.VISIBLE
                //            println("String item_ids:"+item_ids.toString().subSequence
//                (1,item_ids.toString().length-1).split(',').size)
                val current =Calendar.getInstance().time
                val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm:ss",Locale.ENGLISH)
                val formatedDate = sdf.format(current)

                var Feed= OrderRequestLocal( token=orderRequest.token!!,
                    phone_id=orderRequest.phone_id!!,
                    item_idArray=orderRequest.item_id.toString().subSequence
                        (1,orderRequest.item_id.toString().length-1).toString().replace(" ",""),
                    quantityArray=orderRequest.quantity .toString().subSequence
                        (1,orderRequest.quantity.toString().length-1).toString().replace(" ",""),
                    resource_type=orderRequest.resource_type,
                    resource_id=orderRequest.resource_id,created_at = formatedDate.toString())
                println("Feeeeeeeeeeeeeeeed   :"+Feed)
                corurtins.main {
                    db!!.orders_Dao().insertOrder(Feed)
                }
                showMessageOK(this@OrderActivity,
                    getString(R.string.done),
                    getString(R.string.order_successful_local),
                    DialogInterface.OnClickListener { dialog, which -> dialog.dismiss()
                        this@OrderActivity.finish()})

            })
        }else{
            orderProgress?.visibility=View.GONE
            doneOrderBtn?.visibility=View.VISIBLE
            Toast.makeText (this,"you should add some items",Toast.LENGTH_LONG).show()
        }

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
                        quantities.add(quantityET.text.toString())
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
        val unselected = findViewById<androidx.cardview.widget.CardView>(id.toInt())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            selectedItems.removeIf { t -> t.id == id.toInt() }
            item_ids.removeIf { t ->
                quantities.removeIf { t2 -> quantities.indexOf(t2) == item_ids.indexOf(t) }
                t == id.toInt()
            }
            itemsLinLay.removeView(unselected)
        }
    }
}
