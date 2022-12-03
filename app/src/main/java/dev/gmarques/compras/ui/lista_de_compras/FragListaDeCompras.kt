package dev.gmarques.compras.ui.lista_de_compras

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.gmarques.compras.Extensions.Companion.emMoeda
import dev.gmarques.compras.Extensions.Companion.formatarHtml
import dev.gmarques.compras.Extensions.Companion.mostrarTeclado
import dev.gmarques.compras.Extensions.Companion.ocultarTeclado
import dev.gmarques.compras.R
import dev.gmarques.compras.Vibrador
import dev.gmarques.compras.databinding.DialogEditQtdBinding
import dev.gmarques.compras.databinding.DialogEditValorBinding
import dev.gmarques.compras.databinding.DialogEditValorItemBinding
import dev.gmarques.compras.databinding.FragListaDeComprasBinding
import dev.gmarques.compras.entidades.Categoria
import dev.gmarques.compras.entidades.Produto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//adb shell setprop log.tag.FragmentManager DEBUG

class FragListaDeCompras : Fragment(), LifecycleOwner, ItemAdapterCallback {

    private lateinit var viewModel: FragListaDeComprasViewModel
    lateinit var binding: FragListaDeComprasBinding


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

        viewModel.listaLiveData.observe(viewLifecycleOwner) {
            Log.d("USUK", "FragListaDeCompras.onViewCreated: ")
            binding.toolbar.title = it.nome
            binding.toolbar.setNavigationIcon(R.drawable.vec_lista_30_primary)

            initRvDeCategorias()
            initRvDeItens()
            observarCategoriaSelecionada()
            initFragmentResultAddItem()
            initFragmentResultAttItem()
            initFabAddItem()
        }
    }

    private fun observarCategoriaSelecionada() = viewModel.categoriaSelecionadaLiveData
        .observe(viewLifecycleOwner) {
            if (it != null) (binding.rvCategorias.adapter as CategoriaAdapter)
                .selecionarItem(it)
            else (binding.rvCategorias.adapter as CategoriaAdapter)
                .removerSelecao()
        }

    private fun initFabAddItem() = binding.fab.setOnClickListener {
        Vibrador.vibInteracao()
        findNavController().navigate(FragListaDeComprasDirections.actionFragListaDeComprasToAddItem(
            viewModel.listaLiveData.value?.id!!))
    }

    /**
     * chamado sempre que um novo produto é inserido pelo fragAddItem
     * */
    private fun initFragmentResultAddItem() =
        setFragmentResultListener("novoProduto") { _, bundle ->
            val produto = desempacotarProduto(bundle, "produto")
            viewModel.addItemeAplicarAlteracoes(produto)


        }

    /**
     *  chamado sempre que um novo produto é atualizado pelo fragEditItem
     */
    private fun initFragmentResultAttItem() =
        setFragmentResultListener("produtoAtualizado") { _, bundle ->
            lifecycleScope.launch {

                val produtoAtualizado = desempacotarProduto(bundle, "produto")
                val produtoOriginal = desempacotarProduto(bundle, "produtoOriginal")

                viewModel.itemAtualizadoPeloUsuario(produtoAtualizado, produtoOriginal)

            }
        }

    private fun initRvDeItens() {
        val itensAdapter = ProdutoAdapter(this@FragListaDeCompras, this@FragListaDeCompras)
        binding.rvItens.setHasFixedSize(true)
        binding.rvItens.adapter = itensAdapter
        binding.rvItens.layoutManager = LinearLayoutManager(requireContext())

        viewModel.produtosLiveData.observe(viewLifecycleOwner) {
            Log.d("USUK", "FragListaDeCompras.".plus("itensLiveData() ${it.size}"))
            itensAdapter.atualizarColecao(it)
        }
    }

    private fun initRvDeCategorias() {

        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        val categoriasAdapter = CategoriaAdapter(this@FragListaDeCompras,
            ArrayList(), ::categoriaSelecionadaCallback, viewModel)

        binding.rvCategorias.setHasFixedSize(true)
        binding.rvCategorias.adapter = categoriasAdapter

        viewModel.categoriasLiveData.observe(viewLifecycleOwner) { dados ->
            categoriasAdapter.atualizarColecaoDIff(dados)
            if (binding.rvCategorias.layoutManager == null) binding.rvCategorias.layoutManager =
                layoutManager
        }


    }

    private fun categoriaSelecionadaCallback(categoria: Categoria) {
        Vibrador.vibInteracao()
        viewModel.selecionarCategoriaPeloUsuario(categoria)
    }

    override fun produtoComprado(produto: Produto, comprado: Boolean, indice: Int) {
            Vibrador.vibInteracao()
            viewModel.produtoComprado(produto, comprado)
            }

    override fun produtoRemovido(produto: Produto, indice: Int) {
        Vibrador.vibInteracao()

        val msg =
            String.format(getString(R.string.Deseja_mesmo_remover_x_essa_acao_nao_podera_ser_desfeita),
                produto.nome).formatarHtml()

        MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.Por_favor_confirme))
            .setMessage(msg).setPositiveButton(getString(R.string.Remover)) { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.removerItem(produto)
                }
            }.setNegativeButton(getString(R.string.Cancelar)) { _, _ -> }.setCancelable(false)
            .show()

    }

    override fun editarProduto(produto: Produto, indice: Int) {
        Vibrador.vibInteracao()
        findNavController().navigate(FragListaDeComprasDirections
            .actionFragListaDeComprasToEditItem(produto))
    }

    /**
     * Mostra um dialogo de ediçao de preço do produto com historico de preços, baseado no preço
     * desse produto em outras listas
     * Se o usuario aplicar a alteraçao, produto e interface sao atualizados
     * Nota: funçao de callback do recyclerview de itens
     */
    override fun precoEditado(produto: Produto, indice: Int) {
        val binding = DialogEditValorBinding.inflate(layoutInflater)
        var dialog: AlertDialog? = null

        binding.edtValor.hint = produto.preco.toString()
        binding.tvHistorico.text =
            String.format(getString(R.string.Historico_de_precos_de_x), produto.nome)

        // faz a magica (aplica as alteraçoes no produto e fecha o dialogo)
        fun run(preco: Float) = lifecycleScope.launch {
            binding.edtValor.ocultarTeclado()
            viewModel.aplicarPrecoOuQuantidadeeNotificar(produto, preco = preco)
            delay(300)
            dialog!!.dismiss()
            Vibrador.vibInteracao()
        }

        // ouve o clique no botao de salvar do layout
        binding.btnSalvar.setOnClickListener {
            run(binding.edtValor.text.toString().toFloatOrNull() ?: produto.preco)
        }

        // ouve o clique no botao de cancelar do layout
        binding.btnCancelar.setOnClickListener {
            lifecycleScope.launch {
                Vibrador.vibInteracao()
                binding.edtValor.ocultarTeclado()
                delay(300)
                dialog!!.dismiss()

            }
        }

        // ouve o clique no botao concluir do teclado
        binding.edtValor.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) run(binding.edtValor.text.toString()
                .toFloatOrNull() ?: produto.preco)
            false
        }

        // popula a UI com o historico de preços
        lifecycleScope.launch(Dispatchers.IO) {
            val itens = viewModel.buscaEsteItemEmOutrasListas(produto)

            withContext(Dispatchers.Main) {
                itens.forEach { produto ->
                    val produtoBinding = DialogEditValorItemBinding.inflate(layoutInflater)
                    produtoBinding.chip.id = View.generateViewId()
                    binding.container.addView(produtoBinding.root)
                    binding.flow.addView(produtoBinding.root)
                    produtoBinding.chip.text = produto.preco.emMoeda()
                    produtoBinding.chip.setOnClickListener { run(produto.preco) }
                }
            }
        }

        dialog =
            MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.Atualizar_preco))
                .setView(binding.root).setCancelable(false).show()

        lifecycleScope.launch {
            delay(300)
            binding.edtValor.mostrarTeclado()
        }


    }

    /**
     * Mostra um dialogo de ediçao de quantidade do produto com sugestoes de quantidades
     * Se o usuario aplicar a alteraçao, produto e interface sao atualizados
     * Nota: funçao de callback do recyclerview de itens
     */
    override fun qtdEditada(produto: Produto, indice: Int) {
        val binding = DialogEditQtdBinding.inflate(layoutInflater)
        var dialog: AlertDialog? = null

        binding.edtQtd.hint = produto.quantidade.toString()
        binding.tvSugestoes.text = String.format(getString(R.string.Sugestoes_para_x), produto.nome)


        // faz a magica (aplica as alteraçoes no ite e fecha o dialogo)
        fun run(quantidade: Int) = lifecycleScope.launch {
            binding.edtQtd.ocultarTeclado()
            viewModel.aplicarPrecoOuQuantidadeeNotificar(produto, quantidade = quantidade)
            delay(300)
            dialog!!.dismiss()
            Vibrador.vibInteracao()
        }

        // ouve o clique nos chips de sugestao
        val listener = View.OnClickListener { view ->
            run((view as Chip).hint.toString().toInt())
        }

        // ouve o clique no botao de salvar do layout
        binding.btnSalvar.setOnClickListener {
            val qtd = binding.edtQtd.text.toString()
            run(if (qtd.isEmpty()) produto.quantidade else qtd.toInt())
        }

        // ouve o clique no botao de cancelar do layout
        binding.btnCancelar.setOnClickListener {
            lifecycleScope.launch {
                Vibrador.vibInteracao()
                binding.edtQtd.ocultarTeclado()
                delay(300)
                dialog!!.dismiss()

            }
        }

        // ouve o clique no botao concluir do teclado
        binding.edtQtd.setOnEditorActionListener { tv, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val qtd = tv.text.toString()
                run(if (qtd.isEmpty()) produto.quantidade else qtd.toInt())
            }
            false
        }

        binding.chip1.setOnClickListener(listener)
        binding.chip2.setOnClickListener(listener)
        binding.chip3.setOnClickListener(listener)
        binding.chip4.setOnClickListener(listener)
        binding.chip5.setOnClickListener(listener)
        binding.chip6.setOnClickListener(listener)

        dialog =
            MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.Atualizar_quantidade))
                .setView(binding.root).setCancelable(false).show()

        lifecycleScope.launch {
            delay(300)
            binding.edtQtd.mostrarTeclado()
        }
    }

    /**
     * Extrai o objeto do pacote verificando a plataforma e invocando a função correta
     * */
    private fun desempacotarProduto(bundle: Bundle, key: String): Produto =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            bundle.getSerializable(key, Produto::class.java)!!
        } else bundle.getSerializable(key) as Produto


}