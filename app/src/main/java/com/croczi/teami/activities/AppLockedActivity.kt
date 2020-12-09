package com.croczi.teami.activities

import android.graphics.ImageDecoder
import android.graphics.drawable.Animatable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.SimpleDrawerListener
import androidx.appcompat.app.AppCompatDelegate
import android.view.View
import com.croczi.teami.R
import com.croczi.teami.models.AppLockedResponse
import com.croczi.teami.utils.Consts.AppLockedExtra
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.controller.ControllerListener
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.interfaces.SimpleDraweeControllerBuilder
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.request.ImageRequest
import kotlinx.android.synthetic.main.activity_app_locked.*


class AppLockedActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_locked)

        val appLockedResponse = intent.getParcelableExtra<AppLockedResponse>(AppLockedExtra)
        lockedIV.controller = Fresco.newDraweeControllerBuilder()
            .setUri(appLockedResponse?.imgUrl)
            .setControllerListener(object:ControllerListener<ImageInfo> {
                override fun onFailure(id: String?, throwable: Throwable?) {
                }

                override fun onRelease(id: String?) {
                }

                override fun onSubmit(id: String?, callerContext: Any?) {
                }

                override fun onIntermediateImageSet(id: String?, imageInfo: ImageInfo?) {
                }

                override fun onIntermediateImageFailed(id: String?, throwable: Throwable?) {
                }

                override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
                    showLayout(appLockedResponse)
                }
            }
            ).build()
    }

    private fun showLayout(appLockedResponse: AppLockedResponse?) {
        lockedTitleTV.text = appLockedResponse?.title
        lockedMsgTV.text = appLockedResponse?.msg
        progressBar2.visibility=View.GONE
    }
}
