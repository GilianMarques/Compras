package dev.gmarques.compras.presenter.fragmentos.produto_io.edit_produto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
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
import com.google.android.material.textfield.TextInputLayout
 
 import dev.gmarques.compras.Extensions.formatarHtml
 import dev.gmarques.compras.Extensions.mostrarTeclado
 import dev.gmarques.compras.Extensions.smoothScroolToPosition
import dev.gmarques.compras.R
import dev.gmarques.compras.presenter.Vibrador
import dev.gmarques.compras.databinding.FragAddProdutoBinding
import dev.gmarques.compras.domain.entidades.Categoria
import dev.gmarques.compras.domain.ConvencaoNome.formatarComoNomeValido
import dev.gmarques.compras.domain.entidades.ProdutoDispensa
import dev.gmarques.compras.presenter.fragmentos.categoria_io.AddCategoriaDialog
import dev.gmarques.compras.presenter.fragmentos.produto_io.CategoriaAdapter
import dev.gmarques.compras.presenter.fragmentos.produto_io.CategoriaAdapterCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragEditProduto : Fragment(), CategoriaAdapterCallback {

    private lateinit var categoriaAdapter: CategoriaAdapter
    private lateinit var viewModel: EditProdutoViewModel
    private lateinit var binding: FragAddProdutoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragAddProdutoBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[EditProdutoViewModel::class.java]

        carregarArgumentos()
        initToolbar()
        initRvCategorias()
        initEdtNome()
        mostrarDadosDoItemNaUi()
        initFab()

    }

    private fun carregarArgumentos() {
        val args: FragEditProdutoArgs = FragEditProdutoArgs.fromBundle(requireArguments())
        viewModel.produto = args.produto.clonar()
        viewModel.produtoOriginal = args.produto
    }

    private fun initToolbar() = binding.toolbar.setNavigationOnClickListener {
        Vibrador.vibInteracao()
        findNavController().popBackStack()
    }

    private fun mostrarDadosDoItemNaUi() = lifecycleScope.launch {

        binding.edtNome.setText(viewModel.produto.nome)
        binding.edtValor.setText(viewModel.produto.preco.toString())
        binding.edtQtd.setText(viewModel.produto.quantidade.toString())
        binding.edtDetalhes.setText(viewModel.produto.detalhes)
        binding.toolbar.title = String.format(getString(R.string.Editar_x), viewModel.produto.nome)
        viewModel.definirCategoriaSelecionada(viewModel.receberCategoriaDoItem())
        binding.edtNome.mostrarTeclado()
    }

    private fun initEdtNome() = binding.edtNome.addTextChangedListener {

        if (it != null && it.length > 1) lifecycleScope.launch(Dispatchers.IO) {

            val (produtos, nomes) = viewModel.carregarSugestoes(it.toString())

            val adapter = ArrayAdapter(requireContext(), R.layout.item_droplist, nomes)

            withContext(Dispatchers.Main) {
                val edtNome = (binding.textFieldNome.editText as? AutoCompleteTextView)

                edtNome?.setAdapter(adapter)
                edtNome?.onItemClickListener =
                        AdapterView.OnItemClickListener { _, _, indice, _ ->
                            exibirDadosDoProdutoSugeridoSelecionado(produtos[indice])
                        }
            }
        }
    }



    private fun exibirDadosDoProdutoSugeridoSelecionado(produto: ProdutoDispensa) {
        binding.edtQtd.setText(produto.quantidade.toString())
        binding.edtValor.setText(produto.preco.toString())
        binding.edtDetalhes.setText(produto.detalhes)
        lifecycleScope.launch { viewModel.definirCategoriaSelecionada(viewModel.carregarCategoria(produto.categoriaId)) }
    }

    /**
     * inicia o recyclerview de categorias
     * */
    private fun initRvCategorias() = lifecycleScope.launch {

        categoriaAdapter = CategoriaAdapter(requireParentFragment(),
            withContext(Dispatchers.IO) {
                viewModel.carregarCategorias()
            }, this@FragEditProduto)

        val lManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        binding.rvCategorias.layoutManager = lManager
        binding.rvCategorias.adapter = categoriaAdapter

        viewModel.categoriaSelecionada.observe(viewLifecycleOwner) {
            categoriaAdapter.selecionarItem(it)
            binding.rvCategorias.smoothScroolToPosition(categoriaAdapter.receberItens().indexOf(it))
        }
    }

    private fun initFab() = binding.fabConcluir.setOnClickListener {
        lifecycleScope.launch {
            if (validarEntradasECriarObjeto()) fecharFragmento()
        }
    }

    private suspend fun validarEntradasECriarObjeto(): Boolean {

        val nome = binding.edtNome.text.toString().formatarComoNomeValido()
        val preco = binding.edtValor.text.toString()
        val qtd = binding.edtQtd.text.toString()

        if (nomeInvalido(nome)) {
            indicarErroNaView(binding.textFieldNome)
        } else if (produtoRepetido(nome)) {
            notificarErro(String.format(getString(R.string.produto_ja_existe_na_lista), nome))
        } else if (precoInvalido(preco)) {
            indicarErroNaView(binding.textFieldValor)
        } else if (qtdInvalida(qtd)) {
            indicarErroNaView(binding.textFieldQtd)
        } else if (categoriaNaoFoiSelecionada()) {
            notificarErro(getString(R.string.Selecione_uma_categoria))
        } else {

            viewModel.produto.nome = nome
            viewModel.produto.preco = preco.toFloat()
            viewModel.produto.quantidade = qtd.toInt()
            viewModel.produto.detalhes = binding.edtDetalhes.text.toString()
            viewModel.produto.categoriaId = viewModel.categoriaSelecionada.value!!.id

            return true
        }

        return false
    }

    /**
     * Invocado a partir do dialogo de add categoria
     */
    override fun categoriaSelecionada(categoria: Categoria) =
        viewModel.definirCategoriaSelecionada(categoria)

    /**
     * Invocado a partir do dialogo de add categoria
     */
    override fun adicionarCategoria() {
        val dialogo = AddCategoriaDialog(this@FragEditProduto, ::categoriaAdicionada)
        dialogo.show()
    }

    private fun categoriaAdicionada(categoria: Categoria) {
        categoriaAdapter.adicionarItemeNotificar(categoria)
        viewModel.definirCategoriaSelecionada(categoria)
    }

    private fun nomeInvalido(nome: String) = nome.isEmpty()

    private fun precoInvalido(preco: String) = preco.isEmpty()|| preco.toFloat() < .10f

    private fun qtdInvalida(qtd: String) = qtd.isEmpty()|| qtd.toInt() < 1

    private fun categoriaNaoFoiSelecionada() = viewModel.categoriaSelecionada.value == null

    private suspend fun produtoRepetido(nome: String): Boolean {
        return if (viewModel.produto.nome == viewModel.produtoOriginal.nome) false
        else viewModel.produtoJaExisteNaLista(nome)
    }

    private fun notificarErro(mensagem: String) {
        Snackbar.make(binding.fabConcluir, mensagem, Snackbar.LENGTH_LONG).show()
        Vibrador.vibErro()
    }

    private fun indicarErroNaView(view: TextInputLayout) {
        view.error = " "

        val b = Snackbar.make(binding.fabConcluir,
            String.format(getString(R.string.Campo_x_deve_ser_preenchido),
                view.hint.toString().lowercase())
                .formatarHtml(),
            Snackbar.LENGTH_LONG)
        b.anchorView = binding.fabConcluir
        b.show()
        Vibrador.vibErro()

    }

    private fun fecharFragmento() {
        setFragmentResult("produtoAtualizado",
            bundleOf(
                "produto" to viewModel.produto,
                "produtoOriginal" to viewModel.produtoOriginal))

        findNavController().popBackStack()
        Vibrador.vibSucesso()
    }

}