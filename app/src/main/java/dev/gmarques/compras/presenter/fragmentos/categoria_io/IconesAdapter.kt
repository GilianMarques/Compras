package dev.gmarques.compras.presenter.fragmentos.categoria_io

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import dev.gmarques.compras.R
import dev.gmarques.compras.presenter.adapter_selecionavel.SelectableAdapter
import dev.gmarques.compras.presenter.adapter_selecionavel.SelectableViewHolder
import dev.gmarques.compras.databinding.ItemRvCategoriaSemNomeBinding

class IconesAdapter(itens: ArrayList<Int>, fragment: Fragment) :
    SelectableAdapter<Int>(itens, null) {

    private var selecaoBackground: Drawable
    private var originalBackground: Drawable

    init {
        selecaoBackground = ResourcesCompat.getDrawable(fragment.resources,
            R.drawable.background_categoria_rv_selecionada,
            fragment.activity?.theme)!!

        originalBackground = ResourcesCompat.getDrawable(fragment.resources,
            R.drawable.background_categoria_rv,
            fragment.activity?.theme)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        Holder(ItemRvCategoriaSemNomeBinding
            .inflate(LayoutInflater.from(parent.context),
                parent, false))

    override fun onBindViewHolder(holder: ViewHolder, indice: Int) =
        (holder as Holder).carregarView(indice)

    override fun getItemCount(): Int = itens.size

    override fun itensSaoIguais(obj: Int, obj2: Int): Boolean = obj == obj2


    inner class Holder(val binding: ItemRvCategoriaSemNomeBinding) :
        SelectableViewHolder<Int>(binding.root) {

        /**
         * Use essa funçao para carregar a view com os dados do objeto entao chame a função
         *  'viewCarregada' no seu adapter para fazer as verificações relacionadas a seleçao
         *  @see SelectableAdapter.viewCarregada
         * */
        override fun carregarView(indice: Int) {
            binding.ivIcone.setImageResource(itens[indice])
            binding.ivIcone.setOnClickListener {
                alternarSelecaoPorClique(this, itens[indice], indice)
            }

            viewCarregada(this, itens[indice])
        }

        /**
         *Aqui vai a logica para atualizar a view para o status desselecionado
         * Ao chamar essa funçao deve-se atualizar a variavel de controle
         * 'selecionado' para manter o estado do viewHolder atualizado evitando
         * chamadas desnecessarias a esta função
         * @see selecionado
         * */
        override fun itemSelecionado() {
            binding.ivIcone.background = selecaoBackground
            Log.d("USUK", "Holder.".plus("produtoSelecionado() "))
        }

        /**
         *Aqui vai a logica para atualizar a view para o status selecionado
         * Ao chamar essa funçao deve-se atualizar a variavel de controle
         * 'selecionado' para manter o estado do viewHolder atualizado evitando
         * chamadas desnecessarias a esta função
         * @see selecionado
         * */
        override fun itemDesselecionado() {
            binding.ivIcone.background = originalBackground
            Log.d("USUK", "Holder.".plus("produtoDesselecionado() "))
        }

    }

}