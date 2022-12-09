package dev.gmarques.compras.ui.lista_de_compras.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import dev.gmarques.compras.App
import dev.gmarques.compras.Extensions.Companion.emMoeda
import dev.gmarques.compras.Extensions.Companion.formatarHtml
import dev.gmarques.compras.Extensions.Companion.riscarTexto
import dev.gmarques.compras.R
import dev.gmarques.compras.databinding.ItemRvViewBinding
import dev.gmarques.compras.entidades.Produto
import dev.gmarques.compras.ui.lista_de_compras.FragListaDeCompras

class ProdutoAdapter(
    fragListaDeCompras: FragListaDeCompras,
    private val callback: ProdutoAdapterCallback,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var corNormal: Int? = null
    private var corComprado: Int? = null
    private val itens: ArrayList<Produto> = ArrayList()

    init {

        corNormal = MaterialColors.getColor(fragListaDeCompras.binding.root,
            R.attr.itemCardNormal,
            Color.WHITE)

        corComprado = MaterialColors.getColor(fragListaDeCompras.binding.root,
            R.attr.itemCardComprado,
            Color.LTGRAY)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRvViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, callback)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, indice: Int) =
        (holder as ViewHolder).bind(itens[indice], indice)

    override fun getItemCount(): Int = itens.size

    /**
     * Usa Diffutils para comparar as listas e propagar as alteraçoes automaticamente
     * @throws IllegalArgumentException se o conteudo da nova lista for identico ao da lista atual.
     *
     */
    fun atualizarColecao(novosItens: ArrayList<Produto>) {

        // se o conteudo das listas antiga e nova são iguais, o conteudo da nova lista é limpo
        // pelo DiffUtil isso faz com que a lista de itens atual fique vazia se tornando inicio
        // de uma grande dor de cabeça. Embora seja possivel verificar se a nova lista esta vazia
        // antes de chamar clear() na lista atual, isso nao deve ser feito por que em nenhum cenario
        // receber uma lista identica a que ja existe é um comportamento desejavel.
        val tamanhonovosItens = novosItens.size

        val mItemDiffCallback = ItemDiffCallback(itens, novosItens)
        val resultado = DiffUtil.calculateDiff(mItemDiffCallback)
        itens.clear()
        itens.addAll(novosItens)
        resultado.dispatchUpdatesTo(this)

        if (tamanhonovosItens > 0 && novosItens.size == 0) throw java.lang.IllegalArgumentException(
            "A nova lista tinha $tamanhonovosItens itens e agora tem 0, isso significa que seu " +
                    "conteudo era identico ao conteudo da lista atual do adapter. Corrija isso editando " +
                    "uma copia da lista original, nao a lista original em si.")
    }


    inner class ViewHolder(
        private val bindingView: ItemRvViewBinding,
        private val callback: ProdutoAdapterCallback,
    ) : RecyclerView.ViewHolder(bindingView.root) {

        fun bind(produto: Produto, indice: Int) {

            alternarMenu(false)

            bindingView.tvNome.text = produto.nome
            bindingView.tvPreco.text = produto.preco.emMoeda()
            bindingView.tvInfo.text = produto.detalhes
            bindingView.tvQtd.text =
                String.format(App.get.applicationContext.getString(R.string.un), produto.quantidade)
            bindingView.tvPrecoTotal.text = produto.valorTotal().emMoeda()
            bindingView.cbComprado.isChecked = produto.comprado

            aplicarEstilo(produto)

            bindingView.root.setOnLongClickListener(View.OnLongClickListener {
                alternarMenu(true)
                return@OnLongClickListener true
            })

            bindingView.cbComprado.setOnClickListener {
                callback.produtoComprado(produto, bindingView.cbComprado.isChecked)
            }

            bindingView.fabEditar.setOnClickListener {
                alternarMenu(false)
                callback.editarProduto(produto)
            }

            bindingView.fabRemover.setOnClickListener {
                alternarMenu(false)
                callback.produtoRemovido(produto)
            }

            bindingView.tvQtd.setOnClickListener {
                callback.quantidadeEditada(produto)
            }

            bindingView.tvPreco.setOnClickListener {
                callback.precoEditado(produto)
            }
        }

        private fun alternarMenu(mostrar: Boolean) {
            bindingView.containerDados.visibility = if (mostrar) View.GONE else View.VISIBLE
            bindingView.containerOpcoes.visibility = if (mostrar) View.VISIBLE else View.GONE
        }

        @SuppressLint("SetTextI18n")
        private fun aplicarEstilo(produto: Produto) = with(produto.comprado) {
            bindingView.tvNome.riscarTexto(this)
            bindingView.card.setCardBackgroundColor(if (this) corComprado!! else corNormal!!)
            if (this) bindingView.tvNome.text = "<i>${produto.nome}</i>".formatarHtml() /*italico*/
            else bindingView.tvNome.text = produto.nome
        }

    }
}