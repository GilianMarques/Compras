package dev.gmarques.compras.presenter.fragmentos.lista_de_compras

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
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
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.Extensions.emMoeda
import dev.gmarques.compras.Extensions.formatarHtml
import dev.gmarques.compras.Extensions.mostrarTeclado
import dev.gmarques.compras.Extensions.ocultarTeclado
import dev.gmarques.compras.Extensions.smoothScroolToPosition
import dev.gmarques.compras.R
import dev.gmarques.compras.presenter.Vibrador
import dev.gmarques.compras.databinding.DialogEditQtdBinding
import dev.gmarques.compras.databinding.DialogEditValorBinding
import dev.gmarques.compras.databinding.DialogEditValorItemBinding
import dev.gmarques.compras.databinding.FragListaDeComprasBinding
import dev.gmarques.compras.domain.entidades.Produto
import dev.gmarques.compras.presenter.dialogos.lista_io.AddListaDialog
import dev.gmarques.compras.presenter.entidades.CategoriaUi
import dev.gmarques.compras.presenter.dialogos.categoria_io.EditCategoriaDialog
import dev.gmarques.compras.presenter.fragmentos.lista_de_compras.adapters.CategoriaAdapter
import dev.gmarques.compras.presenter.fragmentos.lista_de_compras.adapters.CategoriaAdapterCallback
import dev.gmarques.compras.presenter.fragmentos.lista_de_compras.adapters.ProdutoAdapter
import dev.gmarques.compras.presenter.fragmentos.lista_de_compras.adapters.ProdutoAdapterCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//adb shell setprop log.tag.FragmentManager DEBUG

class FragListaDeCompras : Fragment(), LifecycleOwner, ProdutoAdapterCallback,
    CategoriaAdapterCallback, Toolbar.OnMenuItemClickListener {

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
            binding.toolbar.inflateMenu(R.menu.menu_principal)
            binding.toolbar.setOnMenuItemClickListener(this)
            obervarPrecos()
            initRvDeCategorias()
            initRvDeItens()
            initFragmentResultAddItem()
            initFragmentResultAttItem()
            initFabAddItem()
        }
    }

    private fun obervarPrecos() {
        viewModel.valoresLiveData.observe(viewLifecycleOwner) {
            binding.valores.tvValorCarrinho.text = it.first.emMoeda()
            binding.valores.tvValorCategoria.text = it.second.emMoeda()
            binding.valores.tvValorLista.text = it.third.emMoeda()

            binding.valores.llValorCategorias.visibility =
                    if (it.second > 0) View.VISIBLE else View.GONE
            binding.valores.llValorLista.visibility =
                    if (it.second > 0) View.GONE else View.VISIBLE
        }
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
                viewModel.addProduto(produto)


            }

    /**
     *  chamado sempre que um novo produto é atualizado pelo fragEditItem
     */
    private fun initFragmentResultAttItem() =
            setFragmentResultListener("produtoAtualizado") { _, bundle ->
                val produtoAtualizado = desempacotarProduto(bundle, "produto")
                val produtoOriginal = desempacotarProduto(bundle, "produtoOriginal")
                viewModel.attProduto(produtoAtualizado, produtoOriginal)
            }

    private fun initRvDeItens() {
        val itensAdapter = ProdutoAdapter(this@FragListaDeCompras, this@FragListaDeCompras)
        binding.rvItens.setHasFixedSize(true)
        binding.rvItens.adapter = itensAdapter
        binding.rvItens.layoutManager = LinearLayoutManager(requireContext())

        viewModel.produtosLiveData.observe(viewLifecycleOwner) {
            itensAdapter.atualizarColecao(it)
        }
    }

    private fun initRvDeCategorias() {

        val layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

        val categoriasAdapter =
                CategoriaAdapter(this@FragListaDeCompras, ArrayList(), this@FragListaDeCompras)

        binding.rvCategorias.setHasFixedSize(true)
        binding.rvCategorias.adapter = categoriasAdapter

        viewModel.categoriasLiveData.observe(viewLifecycleOwner) { dados ->
            categoriasAdapter.atualizarColecaoDiff(dados)
            if (binding.rvCategorias.layoutManager == null) binding.rvCategorias.layoutManager =
                    layoutManager
            lifecycleScope.launch {
                delay(300)
                val indice = viewModel.indiceDaCategoriaSelecionada()
                if (indice >= 0) binding.rvCategorias.smoothScroolToPosition(indice)
            }
        }


    }

    override fun produtoComprado(produto: Produto, comprado: Boolean) {
        Vibrador.vibInteracao()
        viewModel.produtoComprado(produto, comprado)
    }

    override fun produtoRemovido(produto: Produto) {
        Vibrador.vibInteracao()

        val msg =
                String.format(getString(R.string.Deseja_mesmo_remover_x_essa_acao_nao_podera_ser_desfeita),
                    produto.nome).formatarHtml()

        MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.Por_favor_confirme))
            .setMessage(msg).setPositiveButton(getString(R.string.Remover)) { _, _ ->
                viewModel.removerProduto(produto)

            }.setNegativeButton(getString(R.string.Cancelar)) { _, _ -> }.setCancelable(false)
            .show()

    }

    override fun editarProduto(produto: Produto) {
        Vibrador.vibInteracao()
        findNavController().navigate(FragListaDeComprasDirections.actionFragListaDeComprasToEditItem(
            produto))
    }

    /**
     * Mostra um dialogo de ediçao de preço do produto com historico de preços, baseado no preço
     * desse produto em outras listas
     * Se o usuario aplicar a alteraçao, produto e interface sao atualizados
     * Nota: funçao de callback do recyclerview de itens
     */
    override fun precoEditado(produto: Produto) {
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
            val itens = viewModel.buscarItemEmOutrasListas(produto)

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
    override fun quantidadeEditada(produto: Produto) {
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

    override fun categoriaSelecionada(holderCategoria: CategoriaUi) {
        Vibrador.vibInteracao()
        viewModel.selecionarCategoriaPeloUsuario(holderCategoria)
    }

    override fun categoriaPressionada(holderCategoria: CategoriaUi) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.Como_deseja_prosseguir))
            .setMessage(String.format(getString(R.string.O_que_deseja_fazer_com_x), holderCategoria.categoria.nome)
                .formatarHtml())
            .setPositiveButton(getString(R.string.Editar)) { _, _ -> mostrarDialogoDeEdicaoDeCategoria(holderCategoria) }
            .setNegativeButton(getString(R.string.Remover)) { _, _ ->
                lifecycleScope.launch {
                    if (viewModel.categoriaEstaEmUso(holderCategoria.categoria)) {

                        Snackbar
                            .make(binding.root, String.format(getString(R.string.categoria_esta_em_uso_e_nao_pode_ser_removida), holderCategoria.categoria.nome), Snackbar.LENGTH_LONG)
                            .show()
                    } else confirmarRemocao(holderCategoria)
                }
            }
            .setNeutralButton(getString(R.string.Cancelar)) { _, _ -> }
            .setCancelable(false)
            .show()
    }

    private fun mostrarDialogoDeEdicaoDeCategoria(holderCategoria: CategoriaUi) {
        EditCategoriaDialog(holderCategoria.categoria, this@FragListaDeCompras, viewModel::categoriaEditada).show()
    }

    private fun confirmarRemocao(holderCategoria: CategoriaUi) {
        Vibrador.vibInteracao()

        val msg =
                String.format(getString(R.string.Deseja_mesmo_remover_x_essa_acao_nao_podera_ser_desfeita),
                    holderCategoria.categoria.nome).formatarHtml()

        MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.Por_favor_confirme))
            .setMessage(msg).setPositiveButton(getString(R.string.Remover)) { _, _ ->
                viewModel.removerCategoria(holderCategoria)
            }.setNegativeButton(getString(R.string.Cancelar)) { _, _ -> }.setCancelable(false)
            .show()

    }

    /**
     * Extrai o objeto do pacote verificando a plataforma e invocando a função correta
     * */
    private fun desempacotarProduto(bundle: Bundle, key: String): Produto =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                bundle.getSerializable(key, Produto::class.java)!!
            } else {
                @Suppress("DEPRECATION")
                bundle.getSerializable(key) as Produto
            }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.add_lista -> exibirDialogoAddLista()
        }

        return true

    }

    private fun exibirDialogoAddLista() {
        AddListaDialog(this) { novaLista ->
            //viewModel.addLista(novaLista)
            //viewmode.mudarListaAtual(lista)

        }.show()

    }


}