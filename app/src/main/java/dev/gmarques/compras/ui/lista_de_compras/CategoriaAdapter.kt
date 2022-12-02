package dev.gmarques.compras.ui.lista_de_compras

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.compras.R.drawable
import dev.gmarques.compras.abstracoes.SelectableAdapter
import dev.gmarques.compras.abstracoes.SelectableViewHolder
import dev.gmarques.compras.databinding.ItemRvCategoriaViewBinding
import dev.gmarques.compras.entidades.Categoria
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * Tentei fazer a implementação desse adapter no Fragmento de lista de compras funcionar no padrao do MVVM
 * mas por conta da função de selecionar categoria e os problemas pra implementar DiffUtils nesse
 * adapter em especifico (problema que nao fui capaz de resolver mesmo tendo implementado a soluçao
 * corretamente no Adapter de itens do mesmo fragmento) acabei tendo que optar por desviar um pouco
 * a implementação. Quando uma categorias é selecionada seja pelo viewmodel ou pela UI (usuario)
 * um Livedata especifico recebe o novo valor e o listener no fragmento solicita ao adapter que aplique a
 * nova categoria selecionada. No final das contas ainda é MVVM mas o ideal era nao ter uma LiveData
 * só para categoria selecionada
 * */
class CategoriaAdapter(
    fragment: Fragment,
    itens: ArrayList<Categoria>,
    private val clickCallback: (Categoria) -> Unit,
    private var viewModel: FragListaDeComprasViewModel,
) : SelectableAdapter<Categoria>(itens, null) {

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
        private val bindingView: ItemRvCategoriaViewBinding,
        private val click: (Categoria) -> Unit,
    ) : SelectableViewHolder<Categoria>(bindingView.root) {


        override fun carregarView(indice: Int) {
            val categoria = itens[indice]

            bindingView.tvNome.text = categoria.nome
            bindingView.ivIcone.setImageResource(Categoria.intIcone(categoria.icone))

            bindingView.rlCard.setOnClickListener {

                click(categoria)
                /*alternarSelecaoPorClique(this, categoria, indice) - nao usar essa funçao no MVVM*/
                // nao chamo o 'alternarSelecaoPorClique' a partir do adapter porque no MVVM a interface deve reagir as atualizacoes dos dados
                //a abordagem adequada é avisar o fragmento que vai avisar o viewmodel que vai decidir o que fazer e atualizar
                // o livedata que vai avisar o fragmento que por fim vai atualizar o recyclerview com o valor adequado
            }

            viewModel.viewModelScope.launch(Dispatchers.IO) {
                val itensComprados = viewModel.todosOsItensDaCategoriaForamComprados(categoria)

                withContext(Dispatchers.Main) {
                    if (itensComprados) bindingView.ivTudoComprado.visibility = View.VISIBLE
                    else bindingView.ivTudoComprado.visibility = View.GONE
                }
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        (holder as ViewHolder).carregarView(position)

    override fun itensSaoIguais(obj: Categoria, obj2: Categoria): Boolean = obj.id == obj2.id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemRvCategoriaViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, clickCallback)
    }


}