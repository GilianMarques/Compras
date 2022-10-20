package dev.gmarques.compras.ui.lista_de_compras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.compras.R
import dev.gmarques.compras.Vibrador
import dev.gmarques.compras.databinding.FragListaDeComprasBinding
import dev.gmarques.compras.io.repositorios.CategoriaRepo
import dev.gmarques.compras.objetos.Categoria
import dev.gmarques.compras.objetos.Item
import dev.gmarques.compras.viewmodel_utils.MutableListLiveData.Evento.*


class FragListaDeCompras : Fragment(), LifecycleOwner, ItemAdapterCallback {


    private lateinit var viewModel: FragListaDeComprasViewModel
    lateinit var binding: FragListaDeComprasBinding
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var categoriaAdapter: CategoriaAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragListaDeComprasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //chamado sempre que um novo item é inserido pelo fragAddItem
        setFragmentResultListener("novoItem") { _, bundle ->
            viewModel.addItem(bundle.getSerializable("item") as Item)
        }

        viewModel = ViewModelProvider(this)[FragListaDeComprasViewModel::class.java]


        inicializarRvDeItens()
        inicializarRvDeCategorias()
        inicializarActionBar()

        binding.fab.setOnClickListener {
            Vibrador.vibInteracao()
            requireView().findNavController()
                .navigate(FragListaDeComprasDirections
                    .actionFragListaDeComprasToAddItem(viewModel.listaLiveData.value?.id!!))
        }

    }


    private fun inicializarActionBar() {
        viewModel.listaLiveData.observe(viewLifecycleOwner) {

            binding.toolbar.title = it.nome
            binding.toolbar.setNavigationIcon(R.drawable.vec_lista_30_primary)
        }

    }

    private fun categoriaSelecionada(categoria: Categoria) {
        Vibrador.vibInteracao()
        viewModel.categoriaSelecionada(categoria)
    }

    private fun inicializarRvDeItens() {

        itemAdapter = ItemAdapter(this, viewModel.itens, this)
        binding.rvItens.adapter = itemAdapter
        binding.rvItens.layoutManager = LinearLayoutManager(requireContext())

        viewModel.itensLiveData.observe(viewLifecycleOwner) {
            if (it != null) when (it.evento) {
                ITEM_ADICIONADO -> itemAdapter.notifyItemInserted(it.posicao)
                ITEM_ATUALIZADO -> itemAdapter.notifyItemChanged(it.posicao)
                ITEM_REMOVIDO -> itemAdapter.notifyItemRemoved(it.posicao)
                LISTA_ATUALIZADA -> itemAdapter.attLista(viewModel.itens)
            }
        }
    }

    private fun inicializarRvDeCategorias() {
        categoriaAdapter =
            CategoriaAdapter(this, viewModel.categorias, ::categoriaSelecionada, viewModel)
        binding.rvCategorias.adapter = categoriaAdapter
        binding.rvCategorias.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        viewModel.categoriasLiveData.observe(viewLifecycleOwner) {
            if (it != null) when (it.evento) {
                ITEM_ADICIONADO -> categoriaAdapter.notifyItemInserted(it.posicao)
                ITEM_ATUALIZADO -> categoriaAdapter.notifyItemChanged(it.posicao)
                ITEM_REMOVIDO -> categoriaAdapter.notifyItemRemoved(it.posicao)
                LISTA_ATUALIZADA -> categoriaAdapter.attLista(viewModel.categorias)
            }
        }
    }


    override fun itemComprado(item: Item, position: Int) {
        Vibrador.vibInteracao()
        val dados = viewModel.itemComprado(item)

        itemAdapter.notifyItemMoved(dados.first,
            dados.second) // item comprado vai pro final ad lista (base em ordem alfabetica)
        categoriaAdapter.notifyItemChanged(viewModel.categorias.indexOf(CategoriaRepo.getCategoria(
            item))) // verifico o estado da categoria do item
    }

    override fun itemPressionado(item: Item, position: Int) {
        Vibrador.vibInteracao()
        // TODO: mostrar menu de opçoes do item
    }

}