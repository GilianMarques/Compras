package dev.gmarques.compras.ui.lista_de_compras

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
import dev.gmarques.compras.Extensions.Companion.fromHtml
import dev.gmarques.compras.Extensions.Companion.hideSoftKeyboard
import dev.gmarques.compras.Extensions.Companion.showKeyboard
import dev.gmarques.compras.R
import dev.gmarques.compras.Vibrador
import dev.gmarques.compras.databinding.DialogEditQtdBinding
import dev.gmarques.compras.databinding.DialogEditValorBinding
import dev.gmarques.compras.databinding.DialogEditValorItemBinding
import dev.gmarques.compras.databinding.FragListaDeComprasBinding
import dev.gmarques.compras.io.repositorios.CategoriaRepo
import dev.gmarques.compras.objetos.Categoria
import dev.gmarques.compras.objetos.Item
import dev.gmarques.compras.viewmodel_utils.MutableListLiveData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//adb shell setprop log.tag.FragmentManager DEBUG

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

        viewModel = ViewModelProvider(this)[FragListaDeComprasViewModel::class.java]

        initFragmentResultAddItem()
        initFragmentResultAttItem()
        initRvDeCategorias()
        initRvDeItens()
        initObservadores()
        initFabAddItem()

    }

    private fun initFabAddItem() = binding.fab.setOnClickListener {
        Vibrador.vibInteracao()
        findNavController().navigate(FragListaDeComprasDirections.actionFragListaDeComprasToAddItem(
            viewModel.listaLiveData.value?.id!!))
    }

    /**
     * chamado sempre que um novo item é inserido pelo fragAddItem
     * */
    private fun initFragmentResultAddItem() = setFragmentResultListener("novoItem") { _, bundle ->
        lifecycleScope.launch {
            delay(350)
            val item = bundle.getSerializable("item") as Item
            viewModel.addItem(item)
            val posicaoItem = viewModel.ajustarCategoriasItemAdicionado(item)
            if (posicaoItem != null) itemAdapter.notifyItemInserted(posicaoItem.second)
            Log.d("USUK", "FragListaDeCompras.".plus("initFragmentResultAddItem() "))
        }
    }

    /**
     *  chamado sempre que um novo item é atualizado pelo fragEditItem
     */
    private fun initFragmentResultAttItem() =
        setFragmentResultListener("itemAtualizado") { _, bundle ->
            lifecycleScope.launch {
                delay(350)//espera a animaçao de transicao de telas acabar pra executar as açoes
                Log.d("USUK", "FragListaDeCompras.".plus("initFragmentResultAttItem() "))

                viewModel.attItem(bundle.getSerializable("item") as Item/*, bundle.getInt("pos")*/)
                viewModel.ajustarCategoriasItemEditado()
                viewModel.carregarItensENotificar()// le os dados do DB

            }
        }

    private fun initObservadores() {

        viewModel.categoriaselecionadaLiveData.observe(viewLifecycleOwner) {
            //chamado sempre que a categoriaSelecionada no viewModel fica nula
            //isso avisa ao adapter para des-selecionar a categoria atualmente selecionada la
            categoriaAdapter.removerSelecao()
        }

        viewModel.itensRecarregadosLiveData.observe(viewLifecycleOwner) {
            itemAdapter.atualizarColecao(viewModel.itens)
        }

        viewModel.categoriasLiveData.observe(viewLifecycleOwner) {
            Log.d("USUK",
                "FragListaDeCompras.initObservadores: categoriasLiveData: ${it.evento.toString()}")
            when (it.evento) {
                MutableListLiveData.Evento.ITEM_ADICIONADO -> categoriaAdapter.notifyItemInserted(it.posicao)
                MutableListLiveData.Evento.ITEM_ATUALIZADO -> categoriaAdapter.notifyItemChanged(it.posicao)
                MutableListLiveData.Evento.ITEM_REMOVIDO -> categoriaAdapter.notifyItemRemoved(it.posicao)
                MutableListLiveData.Evento.LISTA_ATUALIZADA -> categoriaAdapter.attLista(viewModel.categorias)
                else -> {}
            }
        }

        viewModel.listaLiveData.observe(viewLifecycleOwner) {
            binding.toolbar.title = it.nome
            binding.toolbar.setNavigationIcon(R.drawable.vec_lista_30_primary)
        }
    }

    private fun initRvDeItens() = lifecycleScope.launch {
        itemAdapter = ItemAdapter(this@FragListaDeCompras, this@FragListaDeCompras)
        binding.rvItens.adapter = itemAdapter
        binding.rvItens.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun initRvDeCategorias() {
        categoriaAdapter =
            CategoriaAdapter(this, viewModel.categorias, ::categoriaSelecionada, viewModel)
        binding.rvCategorias.adapter = categoriaAdapter
        binding.rvCategorias.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

    }

    private fun categoriaSelecionada(categoria: Categoria) {
        Vibrador.vibInteracao()
        lifecycleScope.launch {
            viewModel.categoriaSelecionada(categoria)
            viewModel.carregarItensENotificar()
        }
    }

    override fun itemComprado(itemAtualizado: Item, position: Int) {
        lifecycleScope.launch {
            Vibrador.vibInteracao()
            val posicoes = viewModel.itemComprado(itemAtualizado)

            // item comprado vai pro final ad lista (base em ordem alfabetica)
            itemAdapter.notifyItemMoved(posicoes.first, posicoes.second)
            // verifico o estado da categoria do item
            categoriaAdapter.notifyItemChanged(viewModel.categorias.indexOf(CategoriaRepo.getCategoria(
                itemAtualizado)))

            Log.d("USUK", "FragListaDeCompras.itemComprado: ${posicoes.first}, ${posicoes.second} ")
        }
    }

    override fun itemRemovido(item: Item, position: Int) {
        Vibrador.vibInteracao()

        val msg =
            String.format(getString(R.string.Deseja_mesmo_remover_x_essa_acao_nao_podera_ser_desfeita),
                item.nome).fromHtml()

        MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.Por_favor_confirme))
            .setMessage(msg).setPositiveButton(getString(R.string.Remover)) { _, _ ->
                lifecycleScope.launch {
                    val uiTodaAtualizada = viewModel.removerItem(item)
                    if (!uiTodaAtualizada) itemAdapter.notifyItemRemoved(position)
                }
            }.setNegativeButton(getString(R.string.Cancelar)) { _, _ -> }.setCancelable(false)
            .show()

    }

    override fun editarItem(item: Item, position: Int) {
        Vibrador.vibInteracao()
        findNavController().navigate(FragListaDeComprasDirections.actionFragListaDeComprasToEditItem(
            item,
            position))
    }

    override fun precoEditado(item: Item, position: Int) {
        val binding = DialogEditValorBinding.inflate(layoutInflater)
        var dialog: AlertDialog? = null

        binding.edtValor.hint = item.preco.toString()
        binding.tvHistorico.text =
            String.format(getString(R.string.Historico_de_precos_de_x), item.nome)

        // faz a magica (aplica as alteraçoes no ite e fecha o dialogo)
        fun run(preco: Float) = lifecycleScope.launch {
            binding.edtValor.hideSoftKeyboard()
            item.preco = preco
            viewModel.attItem(item)
            itemAdapter.notifyItemChanged(position)
            delay(300)
            dialog!!.dismiss()
            Vibrador.vibInteracao()
        }
        // ouve o clique no botao de salvar do layout
        binding.btnSalvar.setOnClickListener {
            run(binding.edtValor.text.toString().toFloatOrNull() ?: item.preco)
        }

        // ouve o clique no botao de cancelar do layout
        binding.btnCancelar.setOnClickListener {
            lifecycleScope.launch {
                Vibrador.vibInteracao()
                binding.edtValor.hideSoftKeyboard()
                delay(300)
                dialog!!.dismiss()

            }
        }

        // ouve o clique no botao concluir do teclado
        binding.edtValor.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) run(binding.edtValor.text.toString()
                .toFloatOrNull() ?: item.preco)
            false
        }

        // popula a UI com o historico de preços
        lifecycleScope.launch {
            viewModel.precoItem(item).forEach { item ->
                val itemBinding = DialogEditValorItemBinding.inflate(layoutInflater)
                itemBinding.chip.id = View.generateViewId()
                binding.container.addView(itemBinding.root)
                binding.flow.addView(itemBinding.root)
                itemBinding.chip.text = item.preco.emMoeda()
                itemBinding.chip.setOnClickListener { run(item.preco) }
            }
        }

        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.Atualizar_preco))
            .setView(binding.root)
            .setCancelable(false)
            .show()

        lifecycleScope.launch {
            delay(300)
            binding.edtValor.showKeyboard()
        }


    }

    override fun qtdEditada(item: Item, position: Int) {
        val binding = DialogEditQtdBinding.inflate(layoutInflater)
        var dialog: AlertDialog? = null

        binding.edtQtd.hint = item.qtd.toString()
        binding.tvSugestoes.text = String.format(getString(R.string.Sugestoes_para_x), item.nome)


        // faz a magica (aplica as alteraçoes no ite e fecha o dialogo)
        fun run(valor: Int) = lifecycleScope.launch {
            binding.edtQtd.hideSoftKeyboard()
            item.qtd = valor
            viewModel.attItem(item)
            itemAdapter.notifyItemChanged(position)
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
            run(if (qtd.isEmpty()) item.qtd else qtd.toInt())
        }

        // ouve o clique no botao de cancelar do layout
        binding.btnCancelar.setOnClickListener {
            lifecycleScope.launch {
                Vibrador.vibInteracao()
                binding.edtQtd.hideSoftKeyboard()
                delay(300)
                dialog!!.dismiss()

            }
        }

        // ouve o clique no botao concluir do teclado
        binding.edtQtd.setOnEditorActionListener { tv, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val qtd = tv.text.toString()
                run(if (qtd.isEmpty()) item.qtd else qtd.toInt())
            }
            false
        }

        binding.chip1.setOnClickListener(listener)
        binding.chip2.setOnClickListener(listener)
        binding.chip3.setOnClickListener(listener)
        binding.chip4.setOnClickListener(listener)
        binding.chip5.setOnClickListener(listener)
        binding.chip6.setOnClickListener(listener)

        dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.Atualizar_quantidade))
            .setView(binding.root)
            .setCancelable(false)
            .show()

        lifecycleScope.launch {
            delay(300)
            binding.edtQtd.showKeyboard()
        }
    }


}