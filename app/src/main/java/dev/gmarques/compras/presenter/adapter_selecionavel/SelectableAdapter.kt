package dev.gmarques.compras.presenter.adapter_selecionavel

import android.util.Log

/**
 * Extenda um adapter dessa classe para ter itens selecionaves
 * @see SelectableViewHolder
 * */
abstract class SelectableAdapter<T>(itens: ArrayList<T>, val callback: Callback<T>? = null) :
    BaseAdapter<T>(itens) {

    /**
     * Sempre que o valor de 'itemSelecionado' muda o viewHolder selecionado
     * é chamado para desfazer sua seleçao para que o proximo VH seja selecionado
     * Essa chamada acontece a partir do setter da propriedade.
     * */
    private var itemNuloListener: (() -> Unit)? = null

    private var itemSelecionado: T? = null
        set(value) {
            itemNuloListener?.invoke()
            itemNuloListener = null
            field = value
        }

    /**
     * Verifica se a view que esta sendo adicionada a tela é a view atualmente
     * selecionada. Chame essa função a partir de 'SelectableViewHolder.carregarView'
     * depois de inicializar a completamente a view (na ultima linha da função)
     *
     * @see SelectableViewHolder.carregarView
     * @see alternarSelecaoPorClique
     * */
    fun viewCarregada(viewHolder: SelectableViewHolder<T>, objetoAtual: T) {

        if (itemSelecionado != null && itensSaoIguais(objetoAtual, itemSelecionado!!)) {
            fazerSelecao(viewHolder)
        } else {
            desfazerSelecao(viewHolder)
        }

    }

    /**
     * Verifica e altera o estado do viewHolder para impedir
     * chamadas desnecessarias a funçao 'itemSelecionado'
     *Atenção:  Nao cabe ao cliente o controle da variavel 'selecionado', embora
     * ele tenha acesso a ela.
     * @see SelectableViewHolder.itemSelecionado
     * */
    private fun fazerSelecao(viewHolder: SelectableViewHolder<T>) {
        //item esta selecionado mas viewholder nao
        if (!viewHolder.selecionado) {
            //listener para o viewHolder se desselecionar qdo o itemSelecionado == null
            itemNuloListener = {
                desfazerSelecao(viewHolder)
            }
            viewHolder.selecionado = true
            viewHolder.itemSelecionado()
        }
    }

    /**
     * Verifica e altera o estado do viewHolder para impedir
     * chamadas desnecessarias a funçao 'itemDesselecionado'
     *Atenção:  Nao cabe ao cliente o controle da variavel 'selecionado', embora
     * ele tenha acesso a ela.
     * @see SelectableViewHolder.itemDesselecionado
     * */
    private fun desfazerSelecao(viewHolder: SelectableViewHolder<T>) {
        //item esta desselecionado mas viewholder nao
        if (viewHolder.selecionado) {
            viewHolder.selecionado = false
            viewHolder.itemDesselecionado()
        }else Log.d("USUK", "SelectableAdapter.desfazerSelecao: nenhuma categoria selecionada")
    }

    /**
     * Deve comparar dois objetos distintos atraves de um identificador unico
     * A proposta dessa funçao é comparar dois itens atraves de um identificador
     * que nao falhe caso os objetos sejam o mesmo porem o conteudo nao seja exatamente o mesmo
     * no caso do usuario estar tentado atualizar um objeto recem editado ou nao tenham
     * a mesma referencia na memoria caso o objeto tenha sido recarregado do DB
     * */
    abstract fun itensSaoIguais(obj: T, obj2: T): Boolean

    /**
     * Chame essa função quando o usuario clicar em um item
     * */
    fun alternarSelecaoPorClique(viewHolder: SelectableViewHolder<T>, item: T, indice: Int) {

        //usuario desselecionou o item
        if (itemSelecionado != null && itensSaoIguais(item, itemSelecionado!!)) {

            // desfaz a selecao no viewHolder selecionado
            itemSelecionado = null
            callback?.selecaoAtualizada(itemSelecionado, -1)

        } else {

            itemSelecionado = item
            fazerSelecao(viewHolder)
            callback?.selecaoAtualizada(itemSelecionado, indice)

        }

    }

    /**
     * Busca o indice do itemSelecionado na lista de itens com base
     * em uma verificaçao definida pelo cliente que busca identificar
     * os objetos por um identificador unico
     *
     * O objeto selecionado nao pode ser nulo
     * @throws NullPointerException
     * */
    private fun obterIndiceDoitemSelecionado(): Int {
        if (itemSelecionado == null) throw java.lang.NullPointerException("Inicialize o itemSelecionado antes de chamar essa função")
        for (i in 0 until itens.size)
            if (itensSaoIguais(itens[i], itemSelecionado!!)) {
                return i
            }
        return -1 // objeto nao existe no array, ainda nao foi adicionado ou foi removido
    }

    /**
     * Define o objeto recebido como selecionado, desselecionando o ultimo objeto no processo.
     * Adicione o objeto da lista de itens antes de chamar essa função ou vera uma exception
     * @throws IllegalArgumentException
     *
     * */
    @Suppress("unused")
    fun selecionarItem(item: T) {
        itemSelecionado = item

        val indice = obterIndiceDoitemSelecionado()

        if (indice >= 0 /*&& lManager.getChildAt(indice) != null*/) notifyItemChanged(indice)
        else {
            throw IllegalArgumentException("O objeto deve estar presente na lista de itens antes de ser definido como selecionado." +
                    "\nindice: $indice" +
                    "\ntamanho lista: ${itens.size}" +
                    "\nconteudo da lista:\n${itens.joinToString(separator = "\n")}")
        }
    }

    /**
     * Use para obter a instancia de onjeto selecionado
     * */
    fun receberItemSelecionado(): T? = itemSelecionado

    /**
     * Função sobrescrita para desselecionar um item que foi removido
     * caso ele seja o item selecionado atualmente
     * */
    override fun removerItemeNotificar(produto: T) {
        itemSelecionado?.let {
            if (itensSaoIguais(produto, itemSelecionado!!)) {
                itemSelecionado =
                    null // desseleciono a view que sera removida e limpo referencia ao objeto, para o GC
            }
        }

        super.removerItemeNotificar(produto)
    }

    /**
     * Chame a qualquer momento para remover a selecao atualmente feita
     * limpando a referencia ao objeto e acionando o callback para o viewHolder
     * remover a selecao, atualizando a interface
     * */
    fun removerSelecao() {
        itemSelecionado = null
    }

    /**
     * Use para receber atualizaçoes sempre que houver alteração na selecao do recyclerview
     */
    interface Callback<T> {
        fun selecaoAtualizada(it: T?, indice: Int)
    }
}


