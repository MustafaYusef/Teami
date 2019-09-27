package com.croczi.teami.retrofit


import android.os.Build
import com.croczi.teami.models.*
import com.croczi.teami.utils.Consts.BASE_URL
import com.croczi.teami.utils.Consts.LOCK_URL
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkTools {

    private val retrofitBuilder: Retrofit.Builder = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
    private val toolsRetrofitBuilder: Retrofit.Builder = Retrofit.Builder()
        .baseUrl(LOCK_URL)
        .addConverterFactory(GsonConverterFactory.create())
    private var retrofit: Retrofit
    private var representativesInterface: RepresentativesInterface
    private var toolsInterface: ToolsInterface

    init {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            retrofitBuilder.addTLSSupport()
        }
        retrofit = retrofitBuilder.build()
        representativesInterface = retrofit.create(RepresentativesInterface::class.java)
        toolsInterface = toolsRetrofitBuilder.build().create(ToolsInterface::class.java)
    }

    private fun <T> Call<T>.getCallback(
        success: (response: T) -> Unit,
        failure: (throwable: String) -> Unit
    ) {
        enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, t: Throwable) {
                t.message?.let {
                    failure(it)
                } ?: failure("Something went wrong")
            }

            override fun onResponse(call: Call<T>, response: Response<T>) {
                checkResponseForErrors(
                    response,
                    noErrors = { data ->
                        success(data)
                    },
                    errors = { error ->
                        failure(error)
                    })
            }

        })
    }

    fun addPharmacy(
        pharmacy: Pharmacy,
        success: (response: AddPharmacyResponse) -> Unit,
        failure: (throwable: String) -> Unit
    ) {
        representativesInterface.addNewPharmacy(pharmacy)
            .getCallback({ success(it) }, { failure(it) })
    }

    fun getUserInfo(
        meRequest: MeRequest,
        success: (response: MeResponse) -> Unit,
        failure: (throwable: String) -> Unit
    ) {
        representativesInterface.getMe(meRequest)
            .getCallback({ success(it) }, { failure(it) })
    }

    fun getPharmacyStatus(
        token: String,
        phoneID: String,
        success: (response: PharmStatusResponse) -> Unit,
        failure: (throwable: String) -> Unit
    ) {
        representativesInterface.getPharmStatus(token, phoneID)
            .getCallback({ success(it) }, { failure(it) })
    }

    fun getStatus(
        token: String,
        phoneID: String,
        success: (response: StatusResponse) -> Unit,
        failure: (throwable: String) -> Unit
    ) {
        representativesInterface.getStatus(token, phoneID)
            .getCallback({ success(it) }, { failure(it) })
    }

    fun getItems(
        token: String,
        phoneID: String,
        success: (response: ItemsResponse) -> Unit,
        failure: (throwable: String) -> Unit
    ) {
        representativesInterface.getItems(token, phoneID)
            .getCallback({ success(it) }, { failure(it) })
    }

    fun postFeedback(
        feedbackRequest: FeedbackRequest,
        success: (response: FeedbackResponse) -> Unit,
        failure: (throwable: String) -> Unit
    ) {
        representativesInterface.postFeedback(feedbackRequest)
            .getCallback({ success(it) }, { failure(it) })
    }

    fun postOrder(
        orderRequest: OrderRequest,
        success: (response: OrderResponse) -> Unit,
        failure: (throwable: String) -> Unit
    ) {
        representativesInterface.postOrder(orderRequest)
            .getCallback({ success(it) }, { failure(it) })
    }

    fun checkIfAppIsLocked(
        success: (response: AppLockedResponse) -> Unit,
        failure: (throwable: String) -> Unit
    ) {
        toolsInterface.getIfAppIsLocked().getCallback({
            success(it)
        }, {
            failure(it)
        })
    }

    fun getMyResources(
        token: String,
        phoneID: String,
        success: (response: MyResourcesResponse) -> Unit,
        failure: (throwable: String) -> Unit
    ) {
        representativesInterface.getMyResources(token, phoneID)
            .getCallback({ success(it) }, { failure(it) })
    }

    fun getOrganizations(
        token: String,
        phoneID: String,
        success: (response: OrganizationResponse) -> Unit,
        failure: (throwable: String) -> Unit
    ) {
        representativesInterface.getOrgs(token, phoneID)
            .getCallback({ success(it) }, { failure(it) })
    }

    fun getRegions(
        token: String,
        phoneID: String,
        selectedOrganization: Int,
        success: (response: RegionResponse) -> Unit,
        failure: (throwable: String) -> Unit
    ) {
        representativesInterface.getRegion(token, phoneID, selectedOrganization)
            .getCallback({ success(it) }, { failure(it) })
    }

    fun getHospitals(
        token: String,
        phoneID: String,
        selectedOrganization: Int,
        success: (response: HospitalsResponse) -> Unit,
        failure: (throwable: String) -> Unit
    ) {
        representativesInterface.getHospitals(token, phoneID, selectedOrganization)
            .getCallback({ success(it) }, { failure(it) })
    }

    fun getSpecialties(
        token: String,
        phoneID: String,
        success: (response: SpecialityResponse) -> Unit,
        failure: (throwable: String) -> Unit
    ) {
        representativesInterface.getSpecialty(token, phoneID)
            .getCallback({ success(it) }, { failure(it) })
    }

    fun addDoctor(
        doctor: Doctor,
        success: (response: AddDoctorResponse) -> Unit,
        failure: (throwable: String) -> Unit
    ) {
        representativesInterface.addNewDoctor(doctor)
            .getCallback({ success(it) }, { failure(it) })
    }

    private fun <T> checkResponseForErrors(
        response: Response<T>,
        noErrors: (T) -> Unit,
        errors: (String) -> Unit
    ) {
        when (response.code()) {
            200 -> response.body()?.let {
                noErrors(it)
            } ?: errors("Something Went Wrong")
            406 -> {
                val converter = retrofit.responseBodyConverter<ErrorResponse>(
                    ErrorResponse::class.java,
                    arrayOfNulls<Annotation>(0)
                )
                val error = response.errorBody()?.let {
                    converter.convert(it)?.error?.get(0).toString()
                } ?: "Something went wrong"
                errors(error)
            }
            400 -> {
                val converter = retrofit.responseBodyConverter<ErrorResponseArray>(
                    ErrorResponseArray::class.java,
                    arrayOfNulls<Annotation>(0)
                )
                val error = response.errorBody()?.let {
                    converter.convert(it)?.error?.get(0).toString()
                } ?: "Something went wrong"
                errors(error)
            }
            else -> errors("Something went wrong")
        }
    }
}

fun Retrofit.Builder.addTLSSupport() {
    val spec = ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
        .supportsTlsExtensions(true)
        .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
        .cipherSuites(
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
            CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
            CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
            CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
            CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA
        )
        .build()
    val client = OkHttpClient.Builder()
        .connectionSpecs(listOf(spec, ConnectionSpec.CLEARTEXT))
        .build()
    this.client(client)
}