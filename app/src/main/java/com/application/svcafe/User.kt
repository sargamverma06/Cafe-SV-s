package com.application.svcafe

data class User(val fullName: String = "", val email: String = "") {

    constructor() : this("", "")
}
