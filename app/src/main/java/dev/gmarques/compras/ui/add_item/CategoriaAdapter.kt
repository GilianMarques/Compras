package dev.gmarques.compras.ui.add_item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import dev.gmarques.compras.databinding.RvCategoriaViewBinding
import dev.gmarques.compras.objetos.Categoria

class CategoriaAdapter(
    private var categorias: ArrayList<Categoria>,
    private val clickCallback: (Categoria) -> Unit,
) : Adapter<CategoriaAdapter.ViewHolder>() {

    class ViewHolder(
        private val bindingView: RvCategoriaViewBinding,
        private val click: (Categoria) -> Unit,
    ) : RecyclerView.ViewHolder(bindingView.root) {

        fun bind(categoria: Categoria) {
            bindingView.tvNome.text = categoria.nome
            bindingView.ivIcone.setImageResource(Categoria.intIcone(categoria.icone))
            bindingView.card.setOnClickListener {
                click(categoria)
            }
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

    @SuppressLint("NotifyDataSetChanged")
    fun attLista(categorias: ArrayList<Categoria>) {
        this.categorias = categorias
        notifyDataSetChanged()
    }

}