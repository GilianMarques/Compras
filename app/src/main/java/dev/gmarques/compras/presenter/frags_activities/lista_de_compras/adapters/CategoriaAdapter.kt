package dev.gmarques.compras.presenter.frags_activities.lista_de_compras.adapters

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.compras.R.drawable
import dev.gmarques.compras.databinding.ItemRvCategoriaViewBinding
import dev.gmarques.compras.presenter.entidades.CategoriaUi

class CategoriaAdapter(
    fragment: Fragment,
    val itens: ArrayList<CategoriaUi>,
    private val clickCallback: CategoriaAdapterCallback,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selecaoBackground: Drawable
    private var originalBackground: Drawable

    init {
        selecaoBackground = ResourcesCompat.getDrawable(fragment.resources,
            drawable.background_categoria_rv_selecionada,
            fragment.activity?.theme)!!

        originalBackground = ResourcesCompat.getDrawable(fragment.resources,
            drawable.background_categoria_rv,
            fragment.activity?.theme)!!
    }

    /**
     * Usa Diffutils para comparar as listas e propagar as alteraçoes automaticamente
     * @throws IllegalArgumentException se o conteudo da nova lista for identico ao da lista atual.
     *
     */
    fun atualizarColecaoDiff(novasCategorias: ArrayList<CategoriaUi>) {

        // se o conteudo das listas antiga e nova são iguais, o conteudo da nova lista é limpo
        // pelo DiffUtil isso faz com que a lista de itens atual fique vazia se tornando inicio
        // de uma grande dor de cabeça. Embora seja possivel verificar se a nova lista esta vazia
        // antes de chamar clear() na lista atual, isso nao deve ser feito por que em nenhum cenario
        // receber uma lista identica a que ja existe é um comportamento desejavel.
        val tamanhonovasCategorias = novasCategorias.size

        val mCategoriaDiffCallback = CategoriaDiffCallback(itens, novasCategorias)
        val resultado = DiffUtil.calculateDiff(mCategoriaDiffCallback)
        itens.clear()
        itens.addAll(novasCategorias)
        resultado.dispatchUpdatesTo(this)

        if (tamanhonovasCategorias > 0 && novasCategorias.size == 0) throw java.lang.IllegalArgumentException(
            "A nova lista tinha $tamanhonovasCategorias itens e agora tem 0, isso significa que seu " +
                    "conteudo era identico ao conteudo da lista atual do adapter. Corrija isso editando " +
                    "uma copia da lista original, nao a lista original em si.")
    }

    inner class ViewHolder(
        private val bindingView: ItemRvCategoriaViewBinding,
        private val callback: CategoriaAdapterCallback,
    ) : RecyclerView.ViewHolder(bindingView.root) {


        fun carregarView(indice: Int) {

            val cHolder = itens[indice]
            val categoria = cHolder.acxds

            bindingView.tvNome.text = categoria.nome
            bindingView.ivIcone.setImageResource(categoria.intIcone())

            bindingView.rlCard.setOnClickListener {
                callback.categoriaSelecionada(cHolder)
            }

            bindingView.rlCard.setOnLongClickListener {
                callback.categoriaPressionada(cHolder)
                return@setOnLongClickListener true
            }

            Log.d("USUK", "ViewHolder.".plus("carregarView() indice = $indice cat: $cHolder"))
            if (cHolder.itensComprados) bindingView.ivTudoComprado.visibility = View.VISIBLE
            else bindingView.ivTudoComprado.visibility = View.GONE

            if (cHolder.selecionada) itemSelecionado()
            else itemDesselecionado()
        }

        fun itemSelecionado() {
            bindingView.rlCard.background = selecaoBackground
        }

        fun itemDesselecionado() {
            bindingView.rlCard.background = originalBackground
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
            (holder as ViewHolder).carregarView(position)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
                ItemRvCategoriaViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, clickCallback)
    }

    override fun getItemCount(): Int = itens.size


}