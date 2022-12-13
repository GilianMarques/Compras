package dev.gmarques.compras.domain

import dev.gmarques.compras.Extensions.capitalizar


object ConvencaoNome {

    /**
     * Formata a string como um nome de objeto valido.
     * Se o resultado dessa função for uma string vazia, o valor desta é invalido o que significa que é composta
     * por caracteres invalidos. No caso de ser uma entrada do usuario é necessario atualizar
     * a interface com a string formatada ja que as regras de formatação removem e modificam
     * caracteres fazendo que com o nome inserido pelo ususario fique (possivelmente) bem diferente
     * do que ele digitou.
     * */
    fun String.formatarComoNomeValido() = this
        .replace(Regex("""[^a-zA-Z0-9. ]"""), "")
        .replace(Regex("[ ]+"), " ")
        .trim().capitalizar()

}