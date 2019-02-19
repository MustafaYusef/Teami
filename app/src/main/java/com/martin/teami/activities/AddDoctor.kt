package com.martin.teami.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.martin.teami.R
import kotlinx.android.synthetic.main.activity_add_doctor.*


class AddDoctor : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_doctor)
        finishAddBtn.setOnClickListener {
            this.finish()
        }
    }
}
