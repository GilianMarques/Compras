package dev.gmarques.compras.presenter.fragmentos.produto_io


import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.compras.R.drawable
import dev.gmarques.compras.presenter.adapter_selecionavel.SelectableAdapter
import dev.gmarques.compras.presenter.adapter_selecionavel.SelectableViewHolder
import dev.gmarques.compras.databinding.ItemRvCategoriaViewBinding
import dev.gmarques.compras.domain.entidades.Categoria


class CategoriaAdapter(
    fragment: Fragment,
    categorias: ArrayList<Categoria>,
    private val adapterCallback: CategoriaAdapterCallback,
) : SelectableAdapter<Categoria>(categorias) {


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

    inner class ViewHolder(
        private val bindingView: ItemRvCategoriaViewBinding,
    ) : SelectableViewHolder<Categoria>(bindingView.root) {

        override fun carregarView(indice: Int) {
            val categoria = itens[indice]

            bindingView.tvNome.text = categoria.nome
            bindingView.ivIcone.setImageResource(categoria.intIcone())

            bindingView.rlCard.setOnClickListener {
                if (indice > 0) adapterCallback.categoriaSelecionada(categoria)
                else adapterCallback.adicionarCategoria()
            }
            viewCarregada(this, categoria)
        }

        override fun itemSelecionado() {
            bindingView.rlCard.background = selecaoBackground
        }

        override fun itemDesselecionado() {
            bindingView.rlCard.background = originalBackground
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemRvCategoriaViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, indice: Int) =
        (holder as ViewHolder).carregarView(indice)

    override fun itensSaoIguais(obj: Categoria, obj2: Categoria): Boolean = obj.id == obj2.id


}