package dev.gmarques.compras.ui.item_io.add_item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.Extensions.Companion.showKeyboard
import dev.gmarques.compras.R
import dev.gmarques.compras.Vibrador
import dev.gmarques.compras.databinding.FragAddItemBinding
import dev.gmarques.compras.objetos.Categoria
import dev.gmarques.compras.ui.item_io.CategoriaAdapter
import kotlinx.coroutines.launch

class FragAddItem : Fragment() {

    private lateinit var viewModel: AddItemViewModel
    private lateinit var binding: FragAddItemBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragAddItemBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


            viewModel = ViewModelProvider(requireParentFragment())[AddItemViewModel::class.java]
              binding.toolbar.setNavigationOnClickListener {
            Vibrador.vibInteracao()
            findNavController().popBackStack()
        }

            val args: FragAddItemArgs = FragAddItemArgs.fromBundle(requireArguments())
            viewModel.listaId = args.listaId


            initEdtNome()
            initFab()
            initCategorias()

            binding.edtNome.addTextChangedListener {
                binding.textFieldNome.error = null
            }
            binding.edtValor.addTextChangedListener {
                binding.textFieldValor.error = null
            }
            binding.edtQtd.addTextChangedListener {
                binding.textFieldQtd.error = null
            }
        binding.edtNome.showKeyboard()

    }

    private fun initFab() = binding.fabConcluir.setOnClickListener {
        val nome = binding.edtNome.text.toString()
        val preco = binding.edtValor.text.toString()
        val qtd = binding.edtQtd.text.toString()

        lifecycleScope.launch {

            if (verificarNome(nome) && !itemRepetido(nome) && verificarPreco(preco) && verificarQtd(
                    qtd) && verificarCategoria()
            ) {
                viewModel.item.nome = nome
                viewModel.item.preco = preco.toFloat()
                viewModel.item.qtd = qtd.toInt()
                viewModel.item.detalhes = binding.edtDetalhes.text.toString()
                viewModel.item.listaId = viewModel.listaId
                viewModel.item.categoriaId = viewModel.categoriaSelecionada!!.id

                setFragmentResult("novoItem", bundleOf("item" to viewModel.item))
                findNavController().popBackStack()
                Vibrador.vibSucesso()
            }
        }

    }

    private fun verificarCategoria() = if (viewModel.categoriaSelecionada == null) {
        val bar = Snackbar.make(binding.root,
            getString(R.string.selecione_uma_categoria),
            Snackbar.LENGTH_LONG)
        bar.anchorView = binding.fabConcluir
        bar.show()
        Vibrador.vibErro()
        false
    } else true

    private suspend fun itemRepetido(nome: String): Boolean {
        return if (viewModel.itemJaExisteNaLista(nome)) {
            val bar = Snackbar.make(binding.root,
                String.format(getString(R.string.item_ja_existe_na_lista), nome),
                Snackbar.LENGTH_LONG)
            bar.anchorView = binding.fabConcluir
            bar.show()
            Vibrador.vibErro()
            true
        } else false
    }

    private fun initEdtNome() = binding.edtNome.addTextChangedListener { it ->

        if (it?.isNotEmpty() == true) lifecycleScope.launch {

            val adapter = ArrayAdapter(requireContext(),
                R.layout.list_item,
                viewModel.carregarSugestoes(it.toString()))
            (binding.textFieldNome.editText as? AutoCompleteTextView)?.setAdapter(adapter)
        }
    }

    private fun initCategorias() = lifecycleScope.launch {
        val categoriaAdapter = CategoriaAdapter(requireParentFragment(),
            viewModel.carregarCategorias(),
            ::categoriaSelecionada)
        binding.rvCategorias.adapter = categoriaAdapter
        binding.rvCategorias.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

    }

    private fun categoriaSelecionada(categoria: Categoria) {
        Vibrador.vibInteracao()
        viewModel.categoriaSelecionada(categoria)
    }

    private fun verificarNome(nome: String) = if (nome.isEmpty()) {
        binding.textFieldNome.error = getString(R.string.Campo_deve_ser_preenchido)
        Vibrador.vibErro()
        false
    } else true

    private fun verificarPreco(preco: String) = if (preco.isEmpty()) {
        binding.textFieldValor.error = getString(R.string.Campo_deve_ser_preenchido)
        Vibrador.vibErro()
        false
    } else true

    private fun verificarQtd(qtd: String) = if (qtd.isEmpty()) {
        binding.textFieldQtd.error = getString(R.string.Campo_deve_ser_preenchido)
        Vibrador.vibErro()
        false
    } else true

}