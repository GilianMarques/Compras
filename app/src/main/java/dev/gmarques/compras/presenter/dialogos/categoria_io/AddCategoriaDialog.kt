package dev.gmarques.compras.presenter.dialogos.categoria_io

import android.annotation.SuppressLint
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.gmarques.compras.App
 
 import dev.gmarques.compras.Extensions.mostrarTeclado
import dev.gmarques.compras.R
import dev.gmarques.compras.presenter.Vibrador
import dev.gmarques.compras.databinding.DialogAddCategoriaBinding
import dev.gmarques.compras.domain.entidades.Categoria
import dev.gmarques.compras.data.repositorios.CategoriaRepo
import dev.gmarques.compras.domain.ConvencaoNome.formatarComoNomeValido
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AddCategoriaDialog(
    private val fragment: Fragment,
    private val callback: (categoria: Categoria) -> Unit,
) {

    @Suppress("JoinDeclarationAndAssignment")
    private val binding: DialogAddCategoriaBinding
    private val dialog: BottomSheetDialog
    private val categoria = Categoria()
    private lateinit var adapter: IconesAdapter

    init {
        binding = DialogAddCategoriaBinding.inflate(fragment.layoutInflater)

        dialog = BottomSheetDialog(fragment.requireContext())
        dialog.setContentView(binding.root)
        dialog.setCancelable(false)
        dialog.behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED

        initToolbar()
        initRecyclerView()
        initEditTextNome()
        initBotoesSalvarECancelar()
    }

    private fun initBotoesSalvarECancelar() {

        binding.btnSalvar.setOnClickListener {
            salvarCategoria()
        }

        binding.btnCancelar.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun initEditTextNome() {

        // ouve o clique no botao concluir do teclado
        binding.edtNome.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            false
        }

        binding.edtNome.mostrarTeclado()

    }

    fun show() = dialog.show()

    @SuppressLint("NotifyDataSetChanged")
    private fun initRecyclerView() = runBlocking {
        val layoutManager = FlexboxLayoutManager(App.get.applicationContext)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.justifyContent = JustifyContent.SPACE_EVENLY

        adapter = IconesAdapter(getIcones(), fragment)

        binding.rvIcones.layoutManager = layoutManager
        binding.rvIcones.adapter = adapter
        (binding.rvIcones.adapter as IconesAdapter).notifyDataSetChanged()
    }

    private fun initToolbar() {

        binding.toolbar.setNavigationOnClickListener {
            Vibrador.vibInteracao()
            dialog.dismiss()
        }

    }

    private fun getIcones() = ArrayList<Int>().also {
        it.add(R.drawable.vec_cat_1)
        it.add(R.drawable.vec_cat_2)
        it.add(R.drawable.vec_cat_3)
        it.add(R.drawable.vec_cat_4)
        it.add(R.drawable.vec_cat_5)
        it.add(R.drawable.vec_cat_6)
        it.add(R.drawable.vec_cat_7)
        it.add(R.drawable.vec_cat_8)
        it.add(R.drawable.vec_cat_9)
        it.add(R.drawable.vec_cat_10)
        it.add(R.drawable.vec_cat_11)
        it.add(R.drawable.vec_cat_12)
        it.add(R.drawable.vec_cat_13)
        it.add(R.drawable.vec_cat_14)
        it.add(R.drawable.vec_cat_15)
        it.add(R.drawable.vec_cat_16)
        it.add(R.drawable.vec_cat_17)
        it.add(R.drawable.vec_cat_18)
        it.add(R.drawable.vec_cat_19)
        it.add(R.drawable.vec_cat_20)
        it.add(R.drawable.vec_cat_21)
        it.add(R.drawable.vec_cat_22)
        it.add(R.drawable.vec_cat_23)
        it.add(R.drawable.vec_cat_24)
        it.add(R.drawable.vec_cat_25)
        it.add(R.drawable.vec_cat_26)
        it.add(R.drawable.vec_cat_27)
        it.add(R.drawable.vec_cat_28)
        it.add(R.drawable.vec_cat_29)
        it.add(R.drawable.vec_cat_30)
        it.add(R.drawable.vec_cat_31)
        it.add(R.drawable.vec_cat_32)
        it.add(R.drawable.vec_cat_33)
        it.add(R.drawable.vec_cat_34)
        it.add(R.drawable.vec_cat_35)
        it.add(R.drawable.vec_cat_36)
        it.add(R.drawable.vec_cat_37)
        it.add(R.drawable.vec_cat_38)
        it.add(R.drawable.vec_cat_39)
        it.add(R.drawable.vec_cat_40)
        it.add(R.drawable.vec_cat_41)
        it.add(R.drawable.vec_cat_42)
        it.add(R.drawable.vec_cat_43)
        it.add(R.drawable.vec_cat_44)
        it.add(R.drawable.vec_cat_45)
        it.add(R.drawable.vec_cat_46)
        it.add(R.drawable.vec_cat_47)
        it.add(R.drawable.vec_cat_48)
        it.add(R.drawable.vec_cat_49)
        it.add(R.drawable.vec_cat_50)
        it.add(R.drawable.vec_cat_51)
        it.add(R.drawable.vec_cat_52)
        it.add(R.drawable.vec_cat_53)
        it.add(R.drawable.vec_cat_54)
        it.add(R.drawable.vec_cat_55)
        it.add(R.drawable.vec_cat_56)
        it.add(R.drawable.vec_cat_57)
        it.add(R.drawable.vec_cat_58)
        it.add(R.drawable.vec_cat_59)
        it.add(R.drawable.vec_cat_60)
        it.add(R.drawable.vec_cat_61)
        it.add(R.drawable.vec_cat_62)
        it.add(R.drawable.vec_cat_63)
        it.add(R.drawable.vec_cat_64)
        it.add(R.drawable.vec_cat_65)
        it.add(R.drawable.vec_cat_66)
        it.add(R.drawable.vec_cat_67)
        it.add(R.drawable.vec_cat_68)
        it.add(R.drawable.vec_cat_69)
        it.add(R.drawable.vec_cat_70)
        it.add(R.drawable.vec_cat_71)
        it.add(R.drawable.vec_cat_72)
        it.add(R.drawable.vec_cat_73)
        it.add(R.drawable.vec_cat_74)
        it.add(R.drawable.vec_cat_75)
        it.add(R.drawable.vec_cat_76)
        it.add(R.drawable.vec_cat_77)
        it.add(R.drawable.vec_cat_78)
        it.add(R.drawable.vec_cat_79)
        it.add(R.drawable.vec_cat_80)
        it.add(R.drawable.vec_cat_81)
        it.add(R.drawable.vec_cat_82)
        it.add(R.drawable.vec_cat_83)
        it.add(R.drawable.vec_cat_84)
        it.add(R.drawable.vec_cat_85)
        it.add(R.drawable.vec_cat_86)
        it.add(R.drawable.vec_cat_87)
        it.add(R.drawable.vec_cat_88)
        it.add(R.drawable.vec_cat_89)
        it.add(R.drawable.vec_cat_90)
        it.add(R.drawable.vec_cat_91)
        it.add(R.drawable.vec_cat_92)
        it.add(R.drawable.vec_cat_93)
        it.add(R.drawable.vec_cat_94)
        it.add(R.drawable.vec_cat_95)
        it.add(R.drawable.vec_cat_96)
        it.add(R.drawable.vec_cat_97)
        it.add(R.drawable.vec_cat_98)
        it.add(R.drawable.vec_cat_99)
        it.add(R.drawable.vec_cat_100)
        it.add(R.drawable.vec_cat_101)
        it.add(R.drawable.vec_cat_102)
        it.add(R.drawable.vec_cat_103)
        it.add(R.drawable.vec_cat_104)
        it.add(R.drawable.vec_cat_105)
        it.add(R.drawable.vec_cat_106)
        it.add(R.drawable.vec_cat_107)
        it.add(R.drawable.vec_cat_108)
        it.add(R.drawable.vec_cat_109)
        it.add(R.drawable.vec_cat_110)
        it.add(R.drawable.vec_cat_111)
        it.add(R.drawable.vec_cat_112)
        it.add(R.drawable.vec_cat_113)
        it.add(R.drawable.vec_cat_114)
        it.add(R.drawable.vec_cat_115)
        it.add(R.drawable.vec_cat_116)
        it.add(R.drawable.vec_cat_117)
        it.add(R.drawable.vec_cat_118)
        it.add(R.drawable.vec_cat_119)
        it.add(R.drawable.vec_cat_120)
        it.add(R.drawable.vec_cat_121)
        it.add(R.drawable.vec_cat_122)
        it.add(R.drawable.vec_cat_123)
        it.add(R.drawable.vec_cat_124)
        it.add(R.drawable.vec_cat_125)
        it.add(R.drawable.vec_cat_126)
        it.add(R.drawable.vec_cat_127)
        it.add(R.drawable.vec_cat_128)
        it.add(R.drawable.vec_cat_129)
        it.add(R.drawable.vec_cat_130)
        it.add(R.drawable.vec_cat_131)
        it.add(R.drawable.vec_cat_132)
        it.add(R.drawable.vec_cat_133)
        it.add(R.drawable.vec_cat_134)
        it.add(R.drawable.vec_cat_135)
        it.add(R.drawable.vec_cat_136)
        it.add(R.drawable.vec_cat_137)
        it.add(R.drawable.vec_cat_138)
        it.add(R.drawable.vec_cat_139)
        it.add(R.drawable.vec_cat_140)
        it.add(R.drawable.vec_cat_141)
        it.add(R.drawable.vec_cat_142)
        it.add(R.drawable.vec_cat_143)
        it.add(R.drawable.vec_cat_144)
        it.add(R.drawable.vec_cat_145)
        it.add(R.drawable.vec_cat_146)
        it.add(R.drawable.vec_cat_147)
        it.add(R.drawable.vec_cat_148)
        it.add(R.drawable.vec_cat_149)
        it.add(R.drawable.vec_cat_150)

    }

    private fun salvarCategoria() {
        val nome = binding.edtNome.text.toString().formatarComoNomeValido()

        if (nome.isEmpty()) notificarErro(R.string.Digite_um_nome_valido)
        else if (categoriaRepetida(nome)) notificarErro(R.string.Essa_categoria_ja_existe_mude)
        else if (iconeNaoSelecionado()) notificarErro(R.string.Selecione_um_icone_para)
        else runBlocking {
            categoria.setIcone(adapter.receberItemSelecionado()!!)
            categoria.nome = nome
            CategoriaRepo.addAttCategoria(categoria)
            dialog.dismiss()
            callback(categoria)
        }


    }

    private fun iconeNaoSelecionado(): Boolean = adapter.receberItemSelecionado() == null

    private fun categoriaRepetida(nome: String): Boolean = runBlocking {
        return@runBlocking CategoriaRepo.getCategoriaPorNome(nome) != null
    }

    private fun notificarErro(mensagem: Int) {
        Toast.makeText(fragment.context, fragment.getString(mensagem), Toast.LENGTH_LONG).show()
        Vibrador.vibErro()
    }



}