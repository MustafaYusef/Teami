package com.croczi.teami.models

import com.google.gson.annotations.SerializedName

class OrganizationResponse(
    @SerializedName("Organisations")
    var organization: List<Resource>
)