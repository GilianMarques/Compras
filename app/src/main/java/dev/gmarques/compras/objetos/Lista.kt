package dev.gmarques.compras.objetos

class Lista : Objetos() {
    fun addItem(item: Item) {
        itens.add(item)
        // TODO: add no db
    }

    var itens: ArrayList<Item> = ArrayList<Item>()
}