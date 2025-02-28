package dev.gmarques.compras.ui.markets

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.compras.data.model.Market
import dev.gmarques.compras.databinding.RvItemMarketBinding
import java.util.Collections

/**
 * Autor: Gilian
 * Data de Criação: 27/02/2025
 */

class MarketAdapter(val callback: Callback) :
    ListAdapter<Market, MarketAdapter.MarketViewHolder>(MarketDiffCallback()) {
    private lateinit var itemTouchHelper: ItemTouchHelper
    private var utilList = mutableListOf<Market>()

    inner class MarketViewHolder(private val binding: RvItemMarketBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(market: Market) {
            with(binding) {
                tvMarketName.text = market.name
                (ivListIcon.background as GradientDrawable).also { it.mutate(); it.setColor(market.color) }

                ivRemove.setOnClickListener {
                    callback.rvMarketsOnRemove(market)
                }

                ivEdit.setOnClickListener {
                    callback.rvMarketsOnEditItemClick(market)
                }

                cvChild.setOnClickListener {
                    callback.rvMarketsOnSelect(market)
                }

                ivHandle.setOnTouchListener { it, motionEvent ->

                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            // Inicia o drag and drop
                            itemTouchHelper.startDrag(this@MarketViewHolder)
                            return@setOnTouchListener true
                        }

                        MotionEvent.ACTION_UP -> {
                            // Garante que performClick seja chamado para acessibilidade
                            it.performClick()
                            return@setOnTouchListener true
                        }

                        else -> {
                            // Permite que outros manipuladores tratem o evento
                            return@setOnTouchListener false
                        }
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarketViewHolder {
        val binding = RvItemMarketBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MarketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MarketViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int = currentList.size

    override fun submitList(list: List<Market>?) {
        utilList.clear()
        utilList.addAll(list ?: emptyList())
        super.submitList(list)
    }

    fun attachItemTouchHelper(touchHelper: ItemTouchHelper) {
        this.itemTouchHelper = touchHelper
    }

    fun moveMarket(fromPosition: Int, toPosition: Int) {
        Collections.swap(utilList, fromPosition, toPosition)
        submitList(utilList.toList())
    }

    fun updateDraggedMarkets() {
        for (i in utilList.indices) {
            val market = utilList[i]
            if (market.position != i) callback.rvMarketsOnDragAndDrop(i, market)
        }
    }

    class MarketDiffCallback : DiffUtil.ItemCallback<Market>() {

        override fun areItemsTheSame(oldItem: Market, newItem: Market): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Market, newItem: Market): Boolean {
            // deve comparar toda a informação que é exibida na view pro usuario
            return oldItem == newItem
        }
    }

    interface Callback {
        fun rvMarketsOnDragAndDrop(toPosition: Int, market: Market)
        fun rvMarketsOnEditItemClick(market: Market)
        fun rvMarketsOnSelect(market: Market)
        fun rvMarketsOnRemove(market: Market)
    }
}