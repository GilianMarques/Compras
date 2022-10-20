package dev.gmarques.compras.ui.lista_de_compras

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.google.android.material.color.MaterialColors
import dev.gmarques.compras.App
import dev.gmarques.compras.Extensions.Companion.emMoeda
import dev.gmarques.compras.Extensions.Companion.strikeThrough
import dev.gmarques.compras.R
import dev.gmarques.compras.databinding.RvItemViewBinding
import dev.gmarques.compras.objetos.Item

class ItemAdapter(
    fragListaDeCompras: FragListaDeCompras,
    private var itens: MutableList<Item>,
    private val callback: ItemAdapterCallback,
) : Adapter<ItemAdapter.ViewHolder>() {

    private var corNormal: Int? = null
    private var corSelecao: Int? = null

    init {

        corNormal = MaterialColors.getColor(fragListaDeCompras.binding.root,
            R.attr.itemCardNormal,
            Color.WHITE)

        corSelecao = MaterialColors.getColor(fragListaDeCompras.binding.root,
            R.attr.itemCardComprado,
            Color.LTGRAY)
    }

    inner class ViewHolder(
        private val bindingView: RvItemViewBinding,
        private val callback: ItemAdapterCallback,
    ) : RecyclerView.ViewHolder(bindingView.root) {


        fun bind(item: Item, position: Int) {
            Log.d("USUK", "ViewHolder.".plus("bind() item = $item, position = $position"))

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
            bindingView.card.setCardBackgroundColor(if (this) corSelecao!! else corNormal!!) // TODO: mudar cor e implementar diff utils
            if (this) bindingView.tvNome.text =
                HtmlCompat.fromHtml("<i>${item.nome}</i>",
                    HtmlCompat.FROM_HTML_MODE_LEGACY)
            else bindingView.tvNome.text = item.nome
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, callback)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(itens[position], position)


    override fun getItemCount() = itens.size

    @SuppressLint("NotifyDataSetChanged")
    fun attLista(itens: ArrayList<Item>) {
        this.itens = itens
        notifyDataSetChanged()
    }

    fun addItem(item: Item, posicao: Int) {
        itens.add(posicao, item)
        notifyItemInserted(posicao)
    }

    fun attIem(item: Item, posicao: Int) {
        itens[posicao] = item
        notifyItemChanged(posicao)
    }

    fun removerItem(posicao: Int) {
        itens.removeAt(posicao)
        notifyItemRemoved(posicao)
    }
}