package com.martin.teami.models

import android.provider.ContactsContract
import com.google.gson.annotations.SerializedName

class OrganizationResponse(
    @SerializedName("Organisations")
    var organization: List<Resource>
)