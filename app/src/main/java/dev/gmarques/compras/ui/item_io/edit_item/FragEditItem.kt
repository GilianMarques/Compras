package dev.gmarques.compras.ui.item_io.edit_item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.Extensions.Companion.showKeyboard
import dev.gmarques.compras.R
import dev.gmarques.compras.Vibrador
import dev.gmarques.compras.databinding.FragAddItemBinding
import dev.gmarques.compras.objetos.Categoria
import dev.gmarques.compras.ui.item_io.CategoriaAdapter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragEditItem : Fragment() {

    private lateinit var viewModel: EditItemViewModel
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

        viewModel = ViewModelProvider(this)[EditItemViewModel::class.java]

        val args: FragEditItemArgs = FragEditItemArgs.fromBundle(requireArguments())
        viewModel.item = args.item
        viewModel.nomeOriginal = args.item.nome
        viewModel.position = args.position

        initRecyclerViewCategorias()
        carregarUI()
        initFab()

    }

    private fun carregarUI() {

        binding.edtNome.setText(viewModel.item.nome)
        binding.edtValor.setText(viewModel.item.preco.toString())
        binding.edtQtd.setText(viewModel.item.qtd.toString())
        binding.edtDetalhes.setText(viewModel.item.detalhes)
        binding.toolbar.title = String.format(getString(R.string.Editar_item), viewModel.item.nome)
        binding.edtNome.showKeyboard()
    }

    /**
     * inicia o recyclerview de categorias
     * */
    private fun initRecyclerViewCategorias() = lifecycleScope.launch {
        val categoriaAdapter = CategoriaAdapter(requireParentFragment(),
            viewModel.carregarCategorias(),
            ::categoriaSelecionada)
        val lManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.rvCategorias.adapter = categoriaAdapter
        binding.rvCategorias.layoutManager = lManager

        carregarCategoriaSelecionada(categoriaAdapter, lManager)
    }

    /**
     * Seta como selecionada a categoria do item que sera editado simulando um clique na view
     * correspondente na tela
     * */
    private suspend fun carregarCategoriaSelecionada(
        adapter: CategoriaAdapter,
        lManager: LinearLayoutManager,
    ) {
        /*espero o recyclerview carregar antes de tentar achar a view e executar um clique nela*/
        while (lManager.childCount == 0) delay(5)

        val categoria = viewModel.carregarCategoria()
        val index = adapter.getIndex(categoria)
        val view = lManager.findViewByPosition(index)
        view?.findViewById<View>(R.id.rl_card)?.performClick()

    }


    private fun categoriaSelecionada(categoria: Categoria) {
        Vibrador.vibInteracao()
        viewModel.categoriaSelecionada(categoria)
    }

    private fun initFab() = binding.fabConcluir.setOnClickListener {
        val nome = binding.edtNome.text.toString()
        val preco = binding.edtValor.text.toString()
        val qtd = binding.edtQtd.text.toString()

        lifecycleScope.launch {

            if (verificarNome(nome)
                && !itemRepetido(nome)
                && verificarPreco(preco)
                && verificarQtd(qtd)
                && verificarCategoria()
            ) {
                viewModel.item.nome = nome
                viewModel.item.preco = preco.toFloat()
                viewModel.item.qtd = qtd.toInt()
                viewModel.item.detalhes = binding.edtDetalhes.text.toString()
                viewModel.item.categoriaId = viewModel.categoriaSelecionada!!.id

                setFragmentResult("itemAtualizado",
                    bundleOf("item" to viewModel.item,
                        "pos" to viewModel.position))

                requireActivity().onBackPressed()
                Vibrador.vibSucesso()
            }
        }

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

    private fun verificarCategoria() = if (viewModel.categoriaSelecionada == null) {
        val bar = Snackbar.make(binding.root,
            getString(R.string.selecione_uma_categoria),
            Snackbar.LENGTH_LONG)
        bar.anchorView = binding.fabConcluir
        bar.show()
        Vibrador.vibErro()
        false
    } else true


    private suspend fun itemRepetido(nome: String): Boolean =
        if (nome == viewModel.nomeOriginal) false
        else if (viewModel.itemJaExisteNaLista(nome)) {
            val bar = Snackbar.make(binding.root,
                String.format(getString(R.string.item_ja_existe_na_lista), nome),
                Snackbar.LENGTH_LONG)
            bar.anchorView = binding.fabConcluir
            bar.show()
            Vibrador.vibErro()
            true
        } else false

}