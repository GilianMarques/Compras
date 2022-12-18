package dev.gmarques.compras.presenter.adapter_selecionavel

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Extenda um viewHolder dessa classe para ter itens selecionaves
 * @see SelectableAdapter
 * */
abstract class SelectableViewHolder<T>(root: View) : RecyclerView.ViewHolder(root) {

   var selecionado = false

    /**
     * Use essa funçao para carregar a view com os dados do objeto entao chame a função
     *  'viewCarregada' no seu adapter para fazer as verificações relacionadas a seleçao
     *  Quando o usuario clicar na view chame 'SelectableAdapter.alternarSelecaoPorClique' para
     *  atualizar o status de view, isso fara com que a função itemSelecionado seja invocada.
     *  @see SelectableAdapter.viewCarregada
     *  @see SelectableAdapter.alternarSelecaoPorClique
     *  @see SelectableViewHolder.itemSelecionado
     * */
    abstract fun carregarView(indice: Int)

    /**
     *Aqui vai a logica para atualizar a view para o status selecionado
     * @see selecionado
     */
    abstract fun itemSelecionado()

    /**
     *Aqui vai a logica para atualizar a view para o status desselecionado
     * @see selecionado
     */
    abstract fun itemDesselecionado()

}