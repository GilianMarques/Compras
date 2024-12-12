package dev.gmarques.compras.ui.main_activity

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.compras.R
import dev.gmarques.compras.data.data.model.ShopList
import dev.gmarques.compras.databinding.RvItemListBinding
import dev.gmarques.compras.utils.App

class ShopListAdapter : ListAdapter<ShopList, ShopListAdapter.ListViewHolder>(ShopListDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {

        val binding = DataBindingUtil.inflate<RvItemListBinding>(
            LayoutInflater.from(parent.context), R.layout.rv_item_list, parent, false
        )


        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ListViewHolder(private val binding: RvItemListBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(list: ShopList) {

            itemView.alpha = 0f // Inicia a view invisível

            binding.tvListName.text = list.name

            val saturatedColor = adjustSaturation(list.color, 2.99f)
            binding.ivListIcon.background?.mutate()?.apply {
                if (this is GradientDrawable) setColor(saturatedColor)
            }

            binding.cvChild.setBackgroundColor(list.color)

            // Anima a entrada da view
            itemView.animate()
                .alpha(1f)
                .setDuration(150)
                .setStartDelay(2L * adapterPosition) // Adiciona um atraso baseado na posição
                .start()

        }

        private fun adjustSaturation(color: Int, @Suppress("SameParameterValue") factor: Float): Int {
            // Converte a cor para HSL
            val hsl = FloatArray(3)
            Color.colorToHSV(color, hsl)

            // Ajusta a saturação (clamp entre 0 e 1)
            hsl[1] = (hsl[1] * factor).coerceIn(0f, 1f)

            // Converte de volta para a cor RGB
            return Color.HSVToColor(hsl)
        }
    }
}
