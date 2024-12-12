package dev.gmarques.compras.data.data.firestore

import android.os.Build
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import dev.gmarques.compras.BuildConfig
import dev.gmarques.compras.data.data.repository.UserRepository

class Firestore {

    // TODO: ajustar regras de acesso no console do firebase ate 10/01/25 ou o app nao vai mais conseguir acessar os dados
    companion object {
        // TODO: usar flavors ou modulos
        private val userRootPath = if (BuildConfig.DEBUG) "debug_database" else UserRepository.getUser()!!.email!!


        private const val DATABASE = "Database"
        private const val SHOP_LISTS = "ShopLists"
        private const val LIST_ITEM = "ListItems"

        val shopListCollection by lazy {
            Firebase.firestore.collection(userRootPath).document(DATABASE).collection(SHOP_LISTS)
        }

        val listItemCollection by lazy {
            Firebase.firestore.collection(userRootPath).document(DATABASE).collection(LIST_ITEM)
        }
    }
}