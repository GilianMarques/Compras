package dev.gmarques.compras.ui.lista_de_compras

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import dev.gmarques.compras.App
import dev.gmarques.compras.Extensions.Companion.emMoeda
import dev.gmarques.compras.Extensions.Companion.strikeThrough
import dev.gmarques.compras.R
import dev.gmarques.compras.databinding.RvItemViewBinding
import dev.gmarques.compras.objetos.Item

class ItemAdapter(
    fragListaDeCompras: FragListaDeCompras,
    private val callback: ItemAdapterCallback,
) : ListAdapter<Item, ItemAdapter.ViewHolder>(ItemDiffUtilCallback()) {

    private var corNormal: Int? = null
    private var corComprado: Int? = null

    init {

        corNormal = MaterialColors.getColor(fragListaDeCompras.binding.root,
            R.attr.itemCardNormal,
            Color.WHITE)

        corComprado = MaterialColors.getColor(fragListaDeCompras.binding.root,
            R.attr.itemCardComprado,
            Color.LTGRAY)
    }

    inner class ViewHolder(
        private val bindingView: RvItemViewBinding,
        private val callback: ItemAdapterCallback,
    ) : RecyclerView.ViewHolder(bindingView.root) {

        fun bind(item: Item, position: Int) {

            bindingView.tvNome.text = item.nome
            bindingView.tvPreco.text = item.preco.emMoeda()
            bindingView.tvInfo.text = item.detalhes
            bindingView.tvQtd.text =
                String.format(App.get.applicationContext.getString(R.string.un), item.qtd)
            bindingView.tvPrecoTotal.text = item.valorTotal().emMoeda()
            bindingView.cbComprado.isChecked = item.comprado

            aplicarEstilo(item)

            bindingView.root.setOnLongClickListener(View.OnLongClickListener {
                callback.itemPressionado(item, position)
                return@OnLongClickListener true
            })

            bindingView.cbComprado.setOnClickListener {
                item.comprado = bindingView.cbComprado.isChecked
                aplicarEstilo(item)
                callback.itemComprado(item, position)
            }
        }

        private fun aplicarEstilo(item: Item) = with(item.comprado) {
            bindingView.tvNome.strikeThrough(this)
            bindingView.card.setCardBackgroundColor(if (this) corComprado!! else corNormal!!)
            if (this) bindingView.tvNome.text =
                HtmlCompat.fromHtml("<i>${item.nome}</i>", /*italico*/
                    HtmlCompat.FROM_HTML_MODE_LEGACY)
            else bindingView.tvNome.text = item.nome
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, callback)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), position)

}