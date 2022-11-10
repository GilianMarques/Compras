package dev.gmarques.compras.ui.lista_de_compras

import android.util.Log
import dev.gmarques.compras.objetos.Item


class ItemDiffUtilCallback(private val itemAdapter: ItemAdapter) {

    data class ItemInfo(val item: Item, val pos: Int)

    private var pairs: ArrayList<Pair<ItemInfo?, ItemInfo?>> = ArrayList()

    suspend fun calcDiffs(old: ArrayList<Item>, new: ArrayList<Item>) {

        criarPares(old, new)

        // adiciono  os itens necessarios
        // add na lista antiga da posiçao menor pra maior, evitando indexOutOfBoundException
        pairs.sortBy { pair -> pair.first?.pos }

        for (i in 0 until pairs.size) {
            val p = pairs[i]
            if (p.first != null && p.second == null) {
                old.add(p.first!!.pos, p.first!!.item)
                itemAdapter.notifyItemInserted(p.first!!.pos)
                //  Log.d("USUK", "ItemDiffUtilCallback.calcDiffs: ${p.first!!.item.nome} adicionado")
            }
        }
        // removo  os itens necessarios

        for (i in 0 until pairs.size) {
            val p = pairs[i]
            // first = novoItem  -> second = itemAntigo
            if (p.first == null && p.second != null) {
                val pos = old.indexOf(p.second!!.item)
                //   Log.d("USUK",
                // "ItemDiffUtilCallback.calcDiffs: ${p.second!!.item.nome} removido de $pos")

                old.removeAt(pos)
                itemAdapter.notifyItemRemoved(pos)
            }
        }


        for (i in 0 until pairs.size) {

            val itemInfoNew = pairs[i].first
            val itemInfoOld = pairs[i].second
            if (itemInfoNew == null || itemInfoOld == null) continue

            val oldPos = old.indexOf(itemInfoOld.item)
            val newPos = new.indexOf(itemInfoNew.item)

            // esse item nao mudou de posicao no array
            if (oldPos == newPos)  // se o conteudo mudou aviso o adapter
                if (!areContentsTheSame(itemInfoNew.item, itemInfoOld.item)) {
                    old[oldPos] = itemInfoNew.item
                    itemAdapter.notifyItemChanged(oldPos)
                }
        }

        for (i in 0 until old.size.coerceAtLeast(new.size)) {

            val a =
                if (i > (old.size - 1)) "     " else "$i " + old[i].nome + " " + old[i].comprado
            val b =
                if (i > (new.size - 1)) "     " else "$i " + new[i].nome + " " + new[i].comprado

            Log.d("USUK", "ItemDiffUtilCallback.calcDiffs: old: $a  - new:$b ")
            //  Log.d("USUK", "ItemDiffUtilCallback.calcDiffs: old: $a ")
        }

        for (i in 0 until new.size) {

            val itemNovo = new[i]
            val oldCopy = ArrayList<Item>()
            oldCopy.addAll(old)
            val j = old.indexOf(itemNovo)

            if (i != j) {
                old.removeAt(j)
                itemAdapter.notifyItemRemoved(j)
                old.add(i, itemNovo)
                itemAdapter.notifyItemInserted(i)
               // delay(500)
                Log.d("USUK",
                    "ItemDiffUtilCallback.calcDiffs: old ${oldCopy[j].nome} new ${itemNovo.nome} de $j para $i  iguais? ${(oldCopy[j].id == itemNovo.id)}")
            }


        }


        for (i in 0 until old.size.coerceAtLeast(new.size)) {

            val a =
                if (i > (old.size - 1)) "     " else "$i " + old[i].nome + " " + old[i].comprado
            val b =
                if (i > (new.size - 1)) "     " else "$i " + new[i].nome + " " + new[i].comprado

            //Log.d("USUK", "ItemDiffUtilCallback.calcDiffs: old: $a  - new:$b ")
            Log.d("USUK", "ItemDiffUtilCallback.calcDiffs: old: $a ")
        }

        pairs.clear()
    }

    private suspend fun criarPares(old: ArrayList<Item>, new: ArrayList<Item>) {
        //preencho um array de pares com todos os itens da nova lista
        for (i in 0 until new.size) pairs.add(Pair(ItemInfo(new[i], i), null))

        // itero sobre a lista antiga pra achar os itens iguais nas duas listas
        // itens na lista antiga que nao existem na nova, foram removidos
        // itens na nova lista que nao existem na antiga, foram adicionados
        // pares que tenham os dois itens serao verificados pra saber se houve alteração no novo item
        for (i in 0 until old.size) {
            val pairFound = buscarESalvarPar(old[i], i)
            // nao foi encontrado item igual na nova lista, item foi removido
            if (!pairFound) pairs.add(Pair(null, ItemInfo(old[i], i)))
        }
    }

    private suspend fun buscarESalvarPar(oldItem: Item, pos: Int): Boolean {

        val pairCopy = ArrayList(pairs)// evita exception por concurencyException

        for (j in 0 until pairCopy.size) {
            val par = pairCopy[j] // obtenho o par que tem um item? da nova lista
            // se os itens sao os iguais salvo um par com ambos pra comparar depois se houve alteraçao no item da nova lista
            if (areItemsTheSame(par.first?.item, oldItem)) {
                pairs[j] = Pair(par.first, ItemInfo(oldItem, pos))
                return true
            }
        }
        return false

    }

    private fun areItemsTheSame(newItem: Item?, oldItem: Item?): Boolean =
        oldItem?.id == newItem?.id

    private fun areContentsTheSame(newItem: Item?, oldItem: Item?): Boolean = oldItem == newItem

}