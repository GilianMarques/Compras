package dev.gmarques.compras.presenter.fragmentos.lista_de_compras

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.compras.Extensions.emMoeda
import dev.gmarques.compras.Extensions.smoothScroolToPosition
import dev.gmarques.compras.R
import dev.gmarques.compras.databinding.FragListaDeComprasBinding
import dev.gmarques.compras.domain.entidades.Lista
import dev.gmarques.compras.domain.entidades.Produto
import dev.gmarques.compras.presenter.Vibrador
import dev.gmarques.compras.presenter.entidades.CategoriaUi
import dev.gmarques.compras.presenter.fragmentos.lista_de_compras.adapters.CategoriaAdapter
import dev.gmarques.compras.presenter.fragmentos.lista_de_compras.adapters.CategoriaAdapterCallback
import dev.gmarques.compras.presenter.fragmentos.lista_de_compras.adapters.ProdutoAdapter
import dev.gmarques.compras.presenter.fragmentos.lista_de_compras.adapters.ProdutoAdapterCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


//adb shell setprop log.tag.FragmentManager DEBUG

class FragListaDeCompras : Fragment(), LifecycleOwner, ProdutoAdapterCallback,
    CategoriaAdapterCallback, Toolbar.OnMenuItemClickListener {

    private lateinit var dialogos: Dialogos
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

        dialogos = Dialogos(this, viewModel)

        val observadorUnico = object : Observer<Lista> {
            override fun onChanged(it: Lista) {
                viewModel.listaLiveData.removeObserver(this)

                Log.d("USUK", "FragListaDeCompras.onViewCreated: ")
                binding.toolbar.setNavigationIcon(R.drawable.vec_lista_30_primary)
                binding.toolbar.inflateMenu(R.menu.menu_principal)
                binding.toolbar.setOnMenuItemClickListener(this@FragListaDeCompras)
                obervarPrecos()
                initRvDeCategorias()
                initRvDeItens()
                initFragmentResultAddItem()
                initFragmentResultAttItem()
                initFabAddItem()

                // na 1° invocaçao do observer esse segundo sera definido para ficar ouvindo sempre que
                // houver alterações na lista  como uma edição de nome por exemplo, apenas para atualizar
                // o toolbar
                viewModel.listaLiveData.observe(viewLifecycleOwner) {
                    binding.toolbar.title = it.nome
                }

            }
        }

        viewModel.listaLiveData.observe(viewLifecycleOwner, observadorUnico)

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

        val layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)

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

    override fun produtoRemovido(produto: Produto) = dialogos.produtoRemovido(produto)

    override fun editarProduto(produto: Produto) {
        Vibrador.vibInteracao()
        findNavController().navigate(FragListaDeComprasDirections.actionFragListaDeComprasToEditItem(
            produto))
    }

    override fun precoEditado(produto: Produto) = dialogos.precoEditado(produto)

    override fun quantidadeEditada(produto: Produto) = dialogos.quantidadeEditada(produto)

    override fun categoriaSelecionada(holderCategoria: CategoriaUi) {
        Vibrador.vibInteracao()
        viewModel.selecionarCategoriaPeloUsuario(holderCategoria)
    }

    override fun categoriaPressionada(holderCategoria: CategoriaUi) =
            dialogos.categoriaPressionada(holderCategoria)

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

    @Override
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.add_lista -> dialogos.exibirDialogoAddLista()
            R.id.editar_lista -> dialogos.exibirDialogoEditLista()
            R.id.remover_lista -> dialogos.exibirDialogoConfirmarRemocaoDeLista()
            R.id.alternar_listas -> dialogos.exibirDialogoAlternarListas()
        }

        return true

    }


}