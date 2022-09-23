package dev.gmarques.compras.objetos

class Item : Objetos() {

    var preco: Float = 0.1f
    var qtd: Int = 1
    var detalhes: String? = null
    var comprado: Boolean = false
    lateinit var categoria: Categoria


}