package dev.gmarques.compras.presenter.fragmentos.lista_de_compras

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dev.gmarques.compras.Extensions.emMoeda
import dev.gmarques.compras.Extensions.formatarHtml
import dev.gmarques.compras.Extensions.mostrarTeclado
import dev.gmarques.compras.Extensions.ocultarTeclado
import dev.gmarques.compras.R
import dev.gmarques.compras.databinding.*
import dev.gmarques.compras.domain.entidades.Produto
import dev.gmarques.compras.presenter.Vibrador
import dev.gmarques.compras.presenter.dialogos.categoria_io.EditCategoriaDialog
import dev.gmarques.compras.presenter.dialogos.lista_io.AddListaDialog
import dev.gmarques.compras.presenter.dialogos.lista_io.AlternarListasDialog
import dev.gmarques.compras.presenter.dialogos.lista_io.EditListaDialog
import dev.gmarques.compras.presenter.entidades.CategoriaUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Serve para reduzir o excesso de codigo no fragmento.
 * Todos os dialogos exibidos a partir do @See FragListaDeCompras devem ser exibidos a partir
 * dessa classe. */
class Dialogos(
    private val fragListaDeCompras: FragListaDeCompras,
    private val viewModel: FragListaDeComprasViewModel,
) {

    private fun mostrarDialogoDeEdicaoDeCategoria(holderCategoria: CategoriaUi) {
        EditCategoriaDialog(holderCategoria.categoria,
            fragListaDeCompras,
            viewModel::categoriaEditada).show()
    }

    private fun confirmarRemocaoDeCategoria(holderCategoria: CategoriaUi) {
        Vibrador.vibInteracao()

        val msg =
                String.format(fragListaDeCompras.getString(R.string.Deseja_mesmo_remover_x_essa_acao_nao_podera_ser_desfeita),
                    holderCategoria.categoria.nome).formatarHtml()

        MaterialAlertDialogBuilder(fragListaDeCompras.requireContext()).setTitle(fragListaDeCompras.getString(R.string.Por_favor_confirme))
            .setMessage(msg)
            .setPositiveButton(fragListaDeCompras.getString(R.string.Remover)) { _, _ ->
                viewModel.removerCategoria(holderCategoria)
            }.setNegativeButton(fragListaDeCompras.getString(R.string.Cancelar)) { _, _ -> }
            .setCancelable(false)
            .show()

    }

    fun exibirDialogoConfirmarRemocaoDeLista() {
        Vibrador.vibInteracao()
        val lista = viewModel.listaLiveData.value!!
        val msg =
                String.format(fragListaDeCompras.getString(R.string.Deseja_mesmo_remover_x_essa_acao_nao_podera_ser_desfeita),
                    lista.nome).formatarHtml()

        MaterialAlertDialogBuilder(fragListaDeCompras.requireContext()).setTitle(fragListaDeCompras.getString(R.string.Por_favor_confirme))
            .setMessage(msg)
            .setPositiveButton(fragListaDeCompras.getString(R.string.Remover)) { _, _ ->
                fragListaDeCompras.lifecycleScope.launch { viewModel.removerLista(lista) }
                exibirDialogoAlternarListas()
            }.setNegativeButton(fragListaDeCompras.getString(R.string.Cancelar)) { _, _ -> }
            .setCancelable(false)
            .show()

    }

    fun categoriaPressionada(holderCategoria: CategoriaUi) {
        MaterialAlertDialogBuilder(fragListaDeCompras.requireContext())
            .setTitle(fragListaDeCompras.getString(R.string.Como_deseja_prosseguir))
            .setMessage(String.format(fragListaDeCompras.getString(R.string.O_que_deseja_fazer_com_x),
                holderCategoria.categoria.nome)
                .formatarHtml())
            .setPositiveButton(fragListaDeCompras.getString(R.string.Editar)) { _, _ ->
                mostrarDialogoDeEdicaoDeCategoria(holderCategoria)
            }
            .setNegativeButton(fragListaDeCompras.getString(R.string.Remover)) { _, _ ->
                fragListaDeCompras.lifecycleScope.launch {
                    if (viewModel.categoriaEstaEmUso(holderCategoria.categoria)) {

                        Snackbar
                            .make(fragListaDeCompras.binding.root,
                                String.format(fragListaDeCompras.getString(R.string.categoria_esta_em_uso_e_nao_pode_ser_removida),
                                    holderCategoria.categoria.nome),
                                Snackbar.LENGTH_LONG)
                            .show()
                    } else confirmarRemocaoDeCategoria(holderCategoria)
                }
            }
            .setNeutralButton(fragListaDeCompras.getString(R.string.Cancelar)) { _, _ -> }
            .setCancelable(false)
            .show()
    }

    fun produtoRemovido(produto: Produto) {
        Vibrador.vibInteracao()

        val msg =
                String.format(fragListaDeCompras.getString(R.string.Deseja_mesmo_remover_x_essa_acao_nao_podera_ser_desfeita),
                    produto.nome).formatarHtml()

        MaterialAlertDialogBuilder(fragListaDeCompras.requireContext()).setTitle(fragListaDeCompras.getString(R.string.Por_favor_confirme))
            .setMessage(msg)
            .setPositiveButton(fragListaDeCompras.getString(R.string.Remover)) { _, _ ->
                viewModel.removerProduto(produto)

            }.setNegativeButton(fragListaDeCompras.getString(R.string.Cancelar)) { _, _ -> }
            .setCancelable(false)
            .show()

    }

    /**
     * Mostra um dialogo de ediçao de preço do produto com historico de preços, baseado no preço
     * desse produto em outras listas
     * Se o usuario aplicar a alteraçao, produto e interface sao atualizados
     * Nota: funçao de callback do recyclerview de itens
     */
    fun precoEditado(produto: Produto) {
        val binding = DialogEditValorBinding.inflate(fragListaDeCompras.layoutInflater)
        var dialog: AlertDialog? = null

        binding.edtValor.hint = produto.preco.toString()
        binding.tvHistorico.text =
                String.format(fragListaDeCompras.getString(R.string.Historico_de_precos_de_x), produto.nome)

        // faz a magica (aplica as alteraçoes no produto e fecha o dialogo)
        fun run(preco: Float) = fragListaDeCompras.lifecycleScope.launch {
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
            fragListaDeCompras.lifecycleScope.launch {
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
        fragListaDeCompras.lifecycleScope.launch(Dispatchers.IO) {
            val itens = viewModel.buscarItemEmOutrasListas(produto)

            withContext(Dispatchers.Main) {
                itens.forEach { produto ->
                    val produtoBinding =
                            DialogEditValorItemBinding.inflate(fragListaDeCompras.layoutInflater)
                    produtoBinding.chip.id = View.generateViewId()
                    binding.container.addView(produtoBinding.root)
                    binding.flow.addView(produtoBinding.root)
                    produtoBinding.chip.text = produto.preco.emMoeda()
                    produtoBinding.chip.setOnClickListener { run(produto.preco) }
                }
            }
        }

        dialog =
                MaterialAlertDialogBuilder(fragListaDeCompras.requireContext()).setTitle(fragListaDeCompras.getString(R.string.Atualizar_preco))
                    .setView(binding.root).setCancelable(false).show()

        fragListaDeCompras.lifecycleScope.launch {
            delay(300)
            binding.edtValor.mostrarTeclado()
        }


    }

    /**
     * Mostra um dialogo de ediçao de quantidade do produto com sugestoes de quantidades
     * Se o usuario aplicar a alteraçao, produto e interface sao atualizados
     * Nota: funçao de callback do recyclerview de itens
     */
    fun quantidadeEditada(produto: Produto) {
        val binding = DialogEditQtdBinding.inflate(fragListaDeCompras.layoutInflater)
        var dialog: AlertDialog? = null

        binding.edtQtd.hint = produto.quantidade.toString()
        binding.tvSugestoes.text =
                String.format(fragListaDeCompras.getString(R.string.Sugestoes_para_x), produto.nome)


        // faz a magica (aplica as alteraçoes no ite e fecha o dialogo)
        fun run(quantidade: Int) = fragListaDeCompras.lifecycleScope.launch {
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
            fragListaDeCompras.lifecycleScope.launch {
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
                MaterialAlertDialogBuilder(fragListaDeCompras.requireContext()).setTitle(fragListaDeCompras.getString(R.string.Atualizar_quantidade))
                    .setView(binding.root).setCancelable(false).show()

        fragListaDeCompras.lifecycleScope.launch {
            delay(300)
            binding.edtQtd.mostrarTeclado()
        }
    }

    fun exibirDialogoAddLista() {
        AddListaDialog(fragListaDeCompras) { novaLista ->
            fragListaDeCompras.lifecycleScope.launch {
                viewModel.addListaOuAtualizarListaNoBancoDeDados(novaLista)
                viewModel.definirListaAtual(novaLista)
                viewModel.desselecionarCategoriaAntesDeAlternarLista()
                viewModel.carregarDadosDaLista()
            }
        }.show()

    }

    fun exibirDialogoEditLista() {
        EditListaDialog(fragListaDeCompras, viewModel.listaLiveData.value!!) { lista ->
            fragListaDeCompras.lifecycleScope.launch {
                viewModel.addListaOuAtualizarListaNoBancoDeDados(lista)
                viewModel.definirListaAtual(lista)
            }
        }.show()
    }

    fun exibirDialogoAlternarListas() {
        AlternarListasDialog(fragListaDeCompras, viewModel) { listaSelecionada ->
            fragListaDeCompras.lifecycleScope.launch {
                viewModel.desselecionarCategoriaAntesDeAlternarLista()
                viewModel.definirListaAtual(listaSelecionada)
                viewModel.carregarDadosDaLista()

            }
        }.show()
    }
}