package dev.gmarques.compras.ui.lista_de_compras

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import dev.gmarques.compras.R.*
import dev.gmarques.compras.databinding.RvCategoriaViewBinding
import dev.gmarques.compras.io.repositorios.ItemRepo
import dev.gmarques.compras.objetos.Categoria
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class CategoriaAdapter(
    fragment: Fragment,
    private var categorias: ArrayList<Categoria>,
    private val clickCallback: (Categoria) -> Unit,
    private var viewModel: FragListaDeComprasViewModel,
) : Adapter<CategoriaAdapter.ViewHolder>() {

    //categoria selecionada no momento
    private var selecao: ViewHolder? = null

    private var selecaoBackground: Drawable
    private var originalBackground: Drawable

    init {
        selecaoBackground = ResourcesCompat.getDrawable(fragment.resources,
            drawable.vec_categoria_rv_selecionada,
            fragment.activity?.theme)!!

        originalBackground = ResourcesCompat.getDrawable(fragment.resources,
            drawable.vec_categoria_rv,
            fragment.activity?.theme)!!
    }


    inner class ViewHolder(
        private val bindingView: RvCategoriaViewBinding,
        private val click: (Categoria) -> Unit,
    ) : RecyclerView.ViewHolder(bindingView.root) {


        fun bind(categoria: Categoria) {
            bindingView.tvNome.text = categoria.nome
            bindingView.ivIcone.setImageResource(Categoria.intIcone(categoria.icone))

            if (viewModel.getCategoriaSelecionada() == categoria.id) selecionar()

            bindingView.rlCard.setOnClickListener {
                selecao?.desSelecionar()

                if (selecao === this) selecao = null
                else selecionar()

                click(categoria)
            }

            CoroutineScope(Job()).launch(Dispatchers.Main) {
                val itens =
                    ItemRepo.getItensNaListaPorCategoria(viewModel.listaLiveData.value!!.id,
                        categoria.id)

                if (itens.all { it.comprado }) bindingView.ivTudoComprado.visibility = View.VISIBLE
                else bindingView.ivTudoComprado.visibility = View.GONE
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

    @SuppressLint("NotifyDataSetChanged")
    fun attLista(categorias: ArrayList<Categoria>) {
        this.categorias = categorias
        notifyDataSetChanged()
    }

}