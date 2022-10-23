package dev.gmarques.compras.ui.item_io

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import dev.gmarques.compras.R.drawable
import dev.gmarques.compras.databinding.RvCategoriaViewBinding
import dev.gmarques.compras.objetos.Categoria


class CategoriaAdapter(
    fragment: Fragment,
    private var categorias: ArrayList<Categoria>,
    private val clickCallback: (Categoria) -> Unit,
) : Adapter<CategoriaAdapter.ViewHolder>() {

    //categoria selecionada no momento
    private var selecao: ViewHolder? = null

    private var selecaoBackground: Drawable
    private var originalBackground: Drawable

    init {
        selecaoBackground =
            ResourcesCompat.getDrawable(fragment.resources,
                drawable.vec_categoria_rv_selecionada, fragment.activity?.theme)!!

        originalBackground =
            ResourcesCompat.getDrawable(fragment.resources,
                drawable.vec_categoria_rv, fragment.activity?.theme)!!
    }


    inner class ViewHolder(
        private val bindingView: RvCategoriaViewBinding,
        private val click: (Categoria) -> Unit,
    ) : RecyclerView.ViewHolder(bindingView.root) {


        fun bind(categoria: Categoria) {
            bindingView.tvNome.text = categoria.nome
            bindingView.ivIcone.setImageResource(Categoria.intIcone(categoria.icone))

            bindingView.rlCard.setOnClickListener {

                selecao?.desSelecionar()

                if (selecao === this) selecao = null
                else selecionar()

                click(categoria)
            }
        }

        private fun selecionar() {
            selecao = this
            bindingView.rlCard.background = selecaoBackground
        }

        private fun desSelecionar() {
            bindingView.rlCard.background = originalBackground

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            RvCategoriaViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, clickCallback)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(categorias[position])


    override fun getItemCount() = categorias.size

    fun getIndex(it: Categoria): Int = categorias.indexOf(it)


}