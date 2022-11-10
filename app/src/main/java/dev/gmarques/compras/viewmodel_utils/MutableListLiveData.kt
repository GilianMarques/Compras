package dev.gmarques.compras.viewmodel_utils

import android.util.Log
import androidx.lifecycle.MutableLiveData
import dev.gmarques.compras.viewmodel_utils.MutableListLiveData.Evento.*

/**
 * Implementaçao propria de um LiveData para notificar os UIControllers
 * sobre alteraçoes específicas em coleçoes.
 *
 * T devera ser o item que populara o array observado
 * */
class MutableListLiveData<T> : MutableLiveData<MutableListLiveData.DadosEvento<T>>() {
    private val eventoNulo = DadosEvento<T>(null, -1, EVENTO_NULO)


    fun itemAdicionado(item: T, posicao: Int) =
        despacharEvento(DadosEvento(item, posicao, ITEM_ADICIONADO))

    fun itemAtualizado(item: T, posicao: Int) =
        despacharEvento(DadosEvento(item, posicao, ITEM_ATUALIZADO))

    fun itemRemovido(item: T, posicao: Int) =
        despacharEvento(DadosEvento(item, posicao, ITEM_REMOVIDO))

    fun colecaoAtualizada() = despacharEvento(DadosEvento(evento = LISTA_ATUALIZADA))

    private fun despacharEvento(evento: DadosEvento<T>) {
        Log.d("USUK",
            "MutableListLiveData.".plus("despacharEvento() (setValue) evento = ${evento.evento}, ${evento.item}, ${evento.posicao}, "))

        super.setValue(evento)
        super.setValue(eventoNulo)
    }


    //   private fun uiThread() = Looper.getMainLooper().thread == Thread.currentThread()

    /**
     *Permite notificar o UIController de alteraçoes especificas em uma coleçao dentro de um
     *Viewmodel, servindo como transporte para informaçoes relevantes a respeito do evento.
     */
    class DadosEvento<T>(item: T? = null, posicao: Int? = null, evento: Evento) {

        var evento: Evento
            private set

        var item: T?
            private set

        var posicao: Int
            private set

        init {
            // se for umevento do tipo LISTA_ATUALIZADA, nao é necessario atribuir valor à item ou posicao
            if ((item == null || posicao == null) && evento != LISTA_ATUALIZADA && evento != EVENTO_NULO) throw IllegalStateException(
                "item e posicao devem receber um valor sempre que o estado nao for ${LISTA_ATUALIZADA.name}" +
                        "\nDETALHES: Item: $item, Posiçao: $posicao Estado: ${evento.name}")

            this.item = item
            this.posicao = posicao ?: 0
            this.evento = evento
        }


    }

    enum class Evento {
        ITEM_ADICIONADO, ITEM_ATUALIZADO, ITEM_REMOVIDO, LISTA_ATUALIZADA, EVENTO_NULO
    }
}