package dev.gmarques.compras.domain

/**
 * Autor: Gilian
 * Data de Criação: 30/12/2024
 * Enumeração para representar diferentes critérios de ordenação.
 */
enum class SortCriteria(val value: Int) {
    NAME(1),
    CREATION_DATE(2),
    CATEGORY(3);

    companion object {

        /**
         * Obtém o enum correspondente a um valor inteiro.
         *
         * @param value O valor inteiro associado ao critério.
         * @return O enum correspondente ou `null` se não for encontrado.
         */
        fun fromValue(value: Int): SortCriteria? {
            return entries.find { it.value == value }
        }
    }
}
