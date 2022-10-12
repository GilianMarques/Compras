package dev.gmarques.compras.io.repositorios

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

abstract class BaseRepo {

    private val job = Job()
    val repoScope = CoroutineScope(job + Dispatchers.IO)

}
