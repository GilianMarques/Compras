package dev.gmarques.compras.presenter.stablishments

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.gmarques.compras.data.model.Establishment
import dev.gmarques.compras.databinding.RvItemEstablishmentBinding
import java.util.Collections

/**
 * Autor: Gilian
 * Data de Criação: 27/02/2025
 */

class EstablishmentAdapter(val callback: Callback) :
    ListAdapter<Establishment, EstablishmentAdapter.EstablishmentViewHolder>(EstablishmentDiffCallback()) {
    private lateinit var itemTouchHelper: ItemTouchHelper
    private var utilList = mutableListOf<Establishment>()

    inner class EstablishmentViewHolder(private val binding: RvItemEstablishmentBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(establishment: Establishment) {
            with(binding) {
                tvEstablishmentName.text = establishment.name
                (ivListIcon.background as GradientDrawable).also { it.mutate(); it.setColor(establishment.color) }

                ivRemove.setOnClickListener {
                    callback.rvEstablishmentsOnRemove(establishment)
                }

                ivEdit.setOnClickListener {
                    callback.rvEstablishmentsOnEditItemClick(establishment)
                }

                cvChild.setOnClickListener {
                    callback.rvEstablishmentsOnSelect(establishment)
                }

                ivHandle.setOnTouchListener { it, motionEvent ->

                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            // Inicia o drag and drop
                            itemTouchHelper.startDrag(this@EstablishmentViewHolder)
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstablishmentViewHolder {
        val binding = RvItemEstablishmentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return EstablishmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EstablishmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int = currentList.size

    override fun submitList(list: List<Establishment>?) {
        utilList.clear()
        utilList.addAll(list ?: emptyList())
        super.submitList(list)
    }

    fun attachItemTouchHelper(touchHelper: ItemTouchHelper) {
        this.itemTouchHelper = touchHelper
    }

    fun moveEstablishment(fromPosition: Int, toPosition: Int) {
        Collections.swap(utilList, fromPosition, toPosition)
        submitList(utilList.toList())
    }

    fun updateDraggedEstablishments() {
        for (i in utilList.indices) {
            val establishment = utilList[i]
            if (establishment.position != i) callback.rvEstablishmentsOnDragAndDrop(i, establishment)
        }
    }

    class EstablishmentDiffCallback : DiffUtil.ItemCallback<Establishment>() {

        override fun areItemsTheSame(oldItem: Establishment, newItem: Establishment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Establishment, newItem: Establishment): Boolean {
            // deve comparar toda a informação que é exibida na view pro usuario
            return oldItem == newItem
        }
    }

    interface Callback {
        fun rvEstablishmentsOnDragAndDrop(toPosition: Int, establishment: Establishment)
        fun rvEstablishmentsOnEditItemClick(establishment: Establishment)
        fun rvEstablishmentsOnSelect(establishment: Establishment)
        fun rvEstablishmentsOnRemove(establishment: Establishment)
    }
}
