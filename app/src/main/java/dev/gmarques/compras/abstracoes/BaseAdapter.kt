package dev.gmarques.compras.abstracoes

import androidx.recyclerview.widget.RecyclerView

/**
 * Inclui funçoes basicas para todos os adapters
 * O proposito dessa classe é padronizar e evitar a repetiçao
 * dessas funçoes que sao amplamente usadas
 * */
abstract class BaseAdapter<T>(itens: ArrayList<T>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    /**
     * Apenas o adapter pode manipular sua lista por tanto
     * devese acessar a lista usando a funçao 'receberItens' que retorna uma
     * copia para leitura que se alterada nao refletira na lista original.
     * @see receberItens()
     */
    @Suppress("CanBePrimaryConstructorProperty")
    protected val itens: ArrayList<T> = itens

    @Suppress("MemberVisibilityCanBePrivate")
    fun receberItens(): ArrayList<T> {
        return ArrayList(itens)
    }

    @Suppress("unused")
    fun adicionarItemeNotificar(produto: T, indice: Int = itens.size - 1) {
        itens.add(indice, produto)
        notifyItemInserted(indice)
    }

    @Suppress("unused")
    fun atualizarItemeNotificar(produto: T, indice: Int = itens.indexOf(produto)) {
        itens[indice] = produto
        notifyItemChanged(indice)
    }

    @Suppress("unused")
    open fun removerItemeNotificar(produto: T) {
        val indice = itens.indexOf(produto)

        if (indice <= 0) {
            throw java.lang.Exception("Objeto nao foi encontrado na lista\nObjeto = $produto \nLista = ${itens.joinToString { "\n" }}")
        }

        itens.removeAt(indice)
        notifyItemRemoved(indice)
    }

    @Suppress("unused")
    fun atualizarColecao(itens: ArrayList<T>) {
        this.itens.clear()
        this.itens.addAll(itens)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = itens.size

}


