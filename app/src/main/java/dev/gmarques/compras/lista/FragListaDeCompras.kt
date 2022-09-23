package dev.gmarques.compras.lista

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dev.gmarques.compras.databinding.FragmentFragListaDeComprasBinding
import dev.gmarques.compras.viewmodel_utils.MutableListLiveData.Evento.*

class FragListaDeCompras : Fragment(), LifecycleOwner {


    private lateinit var viewModel: FragListaDeComprasViewModel
    private lateinit var binding: FragmentFragListaDeComprasBinding
    private lateinit var adapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentFragListaDeComprasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[FragListaDeComprasViewModel::class.java]

        binding.fab.setOnClickListener {
            viewModel.addItem()
        }
        inicializarRvDeItens()
        inicializarLiveData()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun inicializarLiveData() {

        viewModel.itensLiveData.observe(viewLifecycleOwner) {
            Log.d("USUK: FragListaDeCompras listaAtualizada",
                "itemListaAtualizado ${it?.evento?.name}")

            when (it?.evento) {
                ITEM_ADICIONADO -> adapter.notifyItemInserted(it.posicao)
                ITEM_ATUALIZADO -> adapter.notifyItemChanged(it.posicao)
                ITEM_REMOVIDO -> adapter.notifyItemRemoved(it.posicao)
                LISTA_ATUALIZADA -> adapter.attLista(viewModel.lista.itens)
                else -> {}
            }
        }
    }

    private fun inicializarRvDeItens() {

        adapter = ItemAdapter(viewModel.lista.itens, viewModel::itemClick)
        binding.rvItens.adapter = adapter
        binding.rvItens.layoutManager = LinearLayoutManager(requireContext())
    }


}