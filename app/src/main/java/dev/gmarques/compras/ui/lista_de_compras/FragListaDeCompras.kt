package dev.gmarques.compras.ui.lista_de_compras

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.compras.databinding.FragListaDeComprasBinding
import dev.gmarques.compras.viewmodel_utils.MutableListLiveData.Evento.*


class FragListaDeCompras : Fragment(), LifecycleOwner {


    private lateinit var viewModel: FragListaDeComprasViewModel
    private lateinit var binding: FragListaDeComprasBinding
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
        viewModel = ViewModelProvider(this)[FragListaDeComprasViewModel::class.java]

        inicializarRvDeItens()
        inicializarRvDeCategorias()
        inicializarActionBar()

        binding.fab.setOnClickListener {
            requireView().findNavController()
                .navigate(FragListaDeComprasDirections
                    .actionFragListaDeComprasToAddItem(viewModel.listaLiveData.value?.id!!))
        }

    }

    private fun inicializarActionBar() {
        viewModel.listaLiveData.observe(viewLifecycleOwner) {
            (activity as AppCompatActivity).supportActionBar?.title = it.nome
        }
    }


    private fun inicializarRvDeItens() {

        itemAdapter = ItemAdapter(viewModel.itensCategoria, viewModel::itemClick)
        binding.rvItens.adapter = itemAdapter
        binding.rvItens.layoutManager = LinearLayoutManager(requireContext())

        viewModel.itensLiveData.observe(viewLifecycleOwner) {
            if (it != null) when (it.evento) {
                ITEM_ADICIONADO -> itemAdapter.notifyItemInserted(it.posicao)
                ITEM_ATUALIZADO -> itemAdapter.notifyItemChanged(it.posicao)
                ITEM_REMOVIDO -> itemAdapter.notifyItemRemoved(it.posicao)
                LISTA_ATUALIZADA -> itemAdapter.attLista(viewModel.itensCategoria)
            }
        }
    }

    private fun inicializarRvDeCategorias() {
        categoriaAdapter = CategoriaAdapter(this,viewModel.categorias, viewModel::categoriaClick)
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

}