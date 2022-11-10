package dev.gmarques.compras.ui.lista_de_compras

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import dev.gmarques.compras.App
import dev.gmarques.compras.Extensions.Companion.emMoeda
import dev.gmarques.compras.Extensions.Companion.fromHtml
import dev.gmarques.compras.Extensions.Companion.strikeThrough
import dev.gmarques.compras.R
import dev.gmarques.compras.databinding.RvItemViewBinding
import dev.gmarques.compras.objetos.Item

class ItemAdapter(
    fragListaDeCompras: FragListaDeCompras,
    private val callback: ItemAdapterCallback,
) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    private var corNormal: Int? = null
    private var corComprado: Int? = null
    private var itens: ArrayList<Item> = ArrayList()

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

            alternarMenu(false)

            bindingView.tvNome.text = item.nome
            bindingView.tvPreco.text = item.preco.emMoeda()
            bindingView.tvInfo.text = item.detalhes
            bindingView.tvQtd.text =
                String.format(App.get.applicationContext.getString(R.string.un), item.qtd)
            bindingView.tvPrecoTotal.text = item.valorTotal().emMoeda()
            bindingView.cbComprado.isChecked = item.comprado

            aplicarEstilo(item)

            bindingView.root.setOnLongClickListener(View.OnLongClickListener {
                alternarMenu(true)
                return@OnLongClickListener true
            })

            bindingView.cbComprado.setOnClickListener {
                item.comprado = bindingView.cbComprado.isChecked
                callback.itemComprado(item, position)
                aplicarEstilo(item)
            }

            bindingView.fabEditar.setOnClickListener {
                alternarMenu(false)
                callback.editarItem(item, position)
            }

            bindingView.fabRemover.setOnClickListener {
                alternarMenu(false)
                callback.itemRemovido(item, position)
            }

            bindingView.tvQtd.setOnClickListener {
                callback.qtdEditada(item, position)
            }

            bindingView.tvPreco.setOnClickListener {
                callback.precoEditado(item, position)
            }
        }

        private fun alternarMenu(mostrar: Boolean) {
            bindingView.containerDados.visibility = if (mostrar) View.GONE else View.VISIBLE
            bindingView.containerOpcoes.visibility = if (mostrar) View.VISIBLE else View.GONE
        }

        @SuppressLint("SetTextI18n")
        private fun aplicarEstilo(item: Item) = with(item.comprado) {
            bindingView.tvNome.strikeThrough(this)
            bindingView.card.setCardBackgroundColor(if (this) corComprado!! else corNormal!!)
            if (this) bindingView.tvNome.text = "<i>${item.nome}</i>".fromHtml() /*italico*/
            else bindingView.tvNome.text = item.nome
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RvItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, callback)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(itens[position], position)

    override fun getItemCount(): Int = itens.size

    @SuppressLint("NotifyDataSetChanged")
    fun atualizarColecao(itens: java.util.ArrayList<Item>) {
        this@ItemAdapter.itens = itens
        notifyDataSetChanged()
        Log.d("USUK",
            "ItemAdapter.".plus("atualizarColecao() views atualizadas: itens = ${itens.size}"))
    }


}