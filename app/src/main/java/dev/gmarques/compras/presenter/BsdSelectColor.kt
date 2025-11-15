package dev.gmarques.compras.presenter

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.gmarques.compras.R
import dev.gmarques.compras.databinding.BsdSelectColorDialogBinding
import dev.gmarques.compras.databinding.RvItemCategoryColorBinding
import androidx.core.graphics.toColorInt

class BsdSelectColor private constructor(
    targetActivity: Activity,
    private val onConfirmListener: (Int) -> Unit,
    private val dismissListener: (() -> Unit)?,
    private val vividColors: List<Int>,
) {

    private val binding = BsdSelectColorDialogBinding.inflate(targetActivity.layoutInflater)
    private val dialog: BottomSheetDialog = BottomSheetDialog(targetActivity)

    init {
        dialog.setContentView(binding.root)
        dialog.setOnDismissListener {
            dismissListener?.invoke()
        }
        setupAdapter()
    }

    private fun setupAdapter() {
        val adapter = ColorAdapter(vividColors) { selectedColor ->
            onConfirmListener(selectedColor) // Passa a cor selecionada para o listener
            dialog.dismiss() // Fecha o diálogo após a seleção
        }

        with(binding.rv) {
            this.adapter = adapter
            setHasFixedSize(true)

            // Configura o gerenciador de layout para ser uma grade com ajuste automático usando Flexbox
            val layoutManager = FlexboxLayoutManager(context).apply {
                flexDirection = FlexDirection.ROW
                justifyContent = JustifyContent.SPACE_EVENLY
            }

            this.layoutManager = layoutManager
        }
    }

    fun show() = dialog.show()

    class Builder(private val targetActivity: Activity, private val colorForShopList: Boolean) {

        private var onConfirmListener: ((Int) -> Unit)? = null
        private var onDismissListener: (() -> Unit)? = null
        private var colors: List<Int> =
            targetActivity.resources.getStringArray(if (colorForShopList) R.array.pastel_colors else R.array.vivid_colors)
                .asList().map { it.toColorInt() }

        fun setOnConfirmListener(listener: (Int) -> Unit) = apply {
            this.onConfirmListener = listener
        }

        fun setOnDismissListener(listener: (() -> Unit)?) = apply {
            this.onDismissListener = listener
        }

        fun build(): BsdSelectColor {
            // Verifica se o listener de confirmação foi configurado antes de criar a instância
            requireNotNull(onConfirmListener) { "onConfirmListener deve ser definido!" }

            return BsdSelectColor(
                targetActivity,
                onConfirmListener!!, // Atribui com segurança após a verificação
                onDismissListener,
                colors
            )
        }
    }

    class ColorAdapter(
        private val colors: List<Int>,
        private val onColorSelected: (Int) -> Unit,
    ) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

        inner class ColorViewHolder(private val binding: RvItemCategoryColorBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(color: Int) {
                (binding.ivColor.background as GradientDrawable).setColor(color)

                val lp: ViewGroup.LayoutParams = binding.root.layoutParams
                if (lp is FlexboxLayoutManager.LayoutParams) {
                    lp.flexGrow = 2.0f
                    lp.alignSelf = AlignItems.STRETCH
                }

                binding.ivColor.setOnClickListener {
                    onColorSelected(color) // Chama o listener com a cor selecionada
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
            val binding = RvItemCategoryColorBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ColorViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
            holder.bind(colors[position])
        }

        override fun getItemCount(): Int = colors.size
    }
}
