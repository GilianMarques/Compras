package dev.gmarques.compras.viewmodel_utils

import androidx.lifecycle.MutableLiveData
import dev.gmarques.compras.viewmodel_utils.MutableListLiveData.Evento.*

/**
 * Implementaçao propria de um LiveData para notificar os UIControllers
 * sobre alteraçoes específicas em coleçoes.
 *
 * T devera ser o produto que populara o array observado
 * */
class MutableListLiveData<T> : MutableLiveData<MutableListLiveData.DadosEvento<T>>() {
    private val eventoNulo = DadosEvento<T>(null, -1, EVENTO_NULO)


    fun produtoAdicionado(produto: T, posicao: Int = -1) =
        despacharEvento(DadosEvento(produto, posicao, ITEM_ADICIONADO))

    fun produtoAtualizado(produto: T, posicao: Int = -1) =
        despacharEvento(DadosEvento(produto, posicao, ITEM_ATUALIZADO))

    fun produtoRemovido(produto: T, posicao: Int = -1) =
        despacharEvento(DadosEvento(produto, posicao, ITEM_REMOVIDO))

    fun colecaoAtualizada(itens: ArrayList<T>) =
        despacharEvento(DadosEvento(evento = LISTA_ATUALIZADA, itens = itens))

    private fun despacharEvento(evento: DadosEvento<T>) {
        //  Log.d("USUK",   "MutableListLiveData.".plus("despacharEvento() (setValue) evento = ${evento.evento}, ${evento.produto}, ${evento.posicao}, "))

        super.setValue(evento)
        super.setValue(eventoNulo)
    }


    //   private fun uiThread() = Looper.getMainLooper().thread == Thread.currentThread()

    /**
     *Permite notificar o UIController de alteraçoes especificas em uma coleçao dentro de um
     *Viewmodel, servindo como transporte para informaçoes relevantes a respeito do evento.
     */
    class DadosEvento<T>(
        produto: T? = null,
        posicao: Int = -1,
        evento: Evento,
        itens: ArrayList<T>? = null,
    ) {

        var evento: Evento
            private set

        var produto: T?
            private set

        var posicao: Int
            private set

        init {
            // se for um evento do tipo LISTA_ATUALIZADA, nao é necessario atribuir valor à produto ou posicao
            if ((produto == null) && evento != LISTA_ATUALIZADA && evento != EVENTO_NULO) throw IllegalStateException(
                "produto deve receber um valor sempre que o estado nao for ${LISTA_ATUALIZADA.name}" + "\nDETALHES: Item: $produto, Posiçao: $posicao Estado: ${evento.name}")

             if (evento == LISTA_ATUALIZADA && (itens == null || itens.isEmpty())) throw IllegalStateException(
                "Defina uma lista nao nula e nao vazia quando a action for: ${LISTA_ATUALIZADA.name}")

            this.produto = produto
            // deve jogar uma exception se um sender nao definir uma posiçao mas o listener tentar usar pra manipular um array
            // (caso voce seja lerdo: qdo tentar usar a posiçao pra manipular um produto num array sera gerada uma IndexOutOfBoundException por causa do -1)
            this.posicao = posicao ?: -1
            this.evento = evento
        }


    }

    enum class Evento {
        ITEM_ADICIONADO, ITEM_ATUALIZADO, ITEM_REMOVIDO, LISTA_ATUALIZADA, EVENTO_NULO
    }
}