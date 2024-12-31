package dev.gmarques.compras.utils

import com.google.firebase.firestore.ListenerRegistration

/**
 * Embrulha uma instancia de Listener do firestore, para evitar vazamentos
 *
 *  Preciso passar uma instancia dessa classe pra quem define um listener no firebase para que, o listener
 * possa ser dispensado quando nao for mais necessario
 * */
class ListenerRegister(private val listener: ListenerRegistration) {
    fun remove() = listener.remove()
}