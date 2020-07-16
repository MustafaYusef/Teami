package com.mustafayusef.holidaymaster.utils

import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO

object corurtins {
    fun main(work:suspend (()->Unit))=
        CoroutineScope(Dispatchers.IO).launch {
            async {
                work()
            }.await()

        }
}