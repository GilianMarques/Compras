package dev.gmarques.compras.ui.produto_io.add_produto

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
import com.google.android.material.textfield.TextInputLayout
import dev.gmarques.compras.Extensions.Companion.formatarComoNomeValido
import dev.gmarques.compras.Extensions.Companion.formatarHtml
import dev.gmarques.compras.Extensions.Companion.mostrarTeclado
import dev.gmarques.compras.Extensions.Companion.smoothScroolToPosition
import dev.gmarques.compras.R
import dev.gmarques.compras.Vibrador
import dev.gmarques.compras.databinding.FragAddProdutoBinding
import dev.gmarques.compras.entidades.Categoria
import dev.gmarques.compras.ui.categoria_io.AddCategoriaDialog
import dev.gmarques.compras.ui.produto_io.CategoriaAdapter
import dev.gmarques.compras.ui.produto_io.CategoriaAdapterCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FragAddProduto : Fragment(), CategoriaAdapterCallback {

    private lateinit var lManager: LinearLayoutManager
    private lateinit var categoriaAdapter: CategoriaAdapter
    private lateinit var viewModel: AddProdutoViewModel
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

        viewModel = ViewModelProvider(this)[AddProdutoViewModel::class.java]

        binding.toolbar.setNavigationOnClickListener {
            Vibrador.vibInteracao()
            findNavController().popBackStack()
        }

        val args: FragAddProdutoArgs = FragAddProdutoArgs.fromBundle(requireArguments())
        viewModel.listaId = args.listaId

        initEdtNome()
        initFab()
        initRvCategorias()
        addtextListeners()

        binding.edtNome.mostrarTeclado()

    }

    private fun addtextListeners() {
        binding.edtNome.addTextChangedListener {
            binding.textFieldNome.error = null
        }
        binding.edtValor.addTextChangedListener {
            binding.textFieldValor.error = null
        }
        binding.edtQtd.addTextChangedListener {
            binding.textFieldQtd.error = null
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
            viewModel.produto.listaId = viewModel.listaId
            viewModel.produto.categoriaId = viewModel.categoriaSelecionada.value!!.id
            return true
        }

        return false
    }

    private fun fecharFragmento() {
        setFragmentResult("novoProduto", bundleOf("produto" to viewModel.produto))
        findNavController().popBackStack()
        Vibrador.vibSucesso()
    }

    private fun initEdtNome() = binding.edtNome.addTextChangedListener {

        if (it != null && it.length > 1) lifecycleScope.launch(Dispatchers.IO) {

            val adapter = ArrayAdapter(requireContext(),
                R.layout.item_droplist,
                viewModel.carregarSugestoes(it.toString()))

            withContext(Dispatchers.Main) {
                (binding.textFieldNome.editText as? AutoCompleteTextView)?.setAdapter(adapter)
            }
        }
    }

    private fun initRvCategorias() = lifecycleScope.launch {

        val lista = async(Dispatchers.IO) { viewModel.carregarCategorias() }

        categoriaAdapter =
                CategoriaAdapter(requireParentFragment(), lista.await(), this@FragAddProduto)
        lManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        binding.rvCategorias.layoutManager = lManager
        binding.rvCategorias.adapter = categoriaAdapter

        viewModel.categoriaSelecionada.observe(viewLifecycleOwner) {
            categoriaAdapter.selecionarItem(it)
            binding.rvCategorias.smoothScroolToPosition(categoriaAdapter.receberItens().indexOf(it))
        }
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
        val dialogo = AddCategoriaDialog(this@FragAddProduto, ::categoriaAdicionada)
        dialogo.show()
    }

    private fun categoriaAdicionada(categoria: Categoria) {
        categoriaAdapter.adicionarItemeNotificar(categoria)
        viewModel.definirCategoriaSelecionada(categoria)
    }

    private fun nomeInvalido(nome: String) = nome.isEmpty()

    private fun precoInvalido(preco: String) = preco.isEmpty() || preco.toFloat() < .10f

    private fun qtdInvalida(qtd: String) = qtd.isEmpty() || qtd.toInt() < 1

    private fun categoriaNaoFoiSelecionada() = viewModel.categoriaSelecionada.value == null

    private suspend fun produtoRepetido(nome: String): Boolean =
            viewModel.produtoJaExisteNaLista(nome)

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

}