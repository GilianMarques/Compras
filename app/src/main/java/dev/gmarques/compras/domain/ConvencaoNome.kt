package dev.gmarques.compras.domain

import dev.gmarques.compras.Extensions.capitalizar


object ConvencaoNome {
   
    private val espacosMultiplos = Regex("[ ][ ]+")
    private val caracteresEspeciais = Regex("""[\\!@$%¨&*()_+="'°|¬¢£§;:/<>()\[\]{}]""")

    /**
     * Formata a string como um nome de objeto valido.
     * Se o resultado dessa função for uma string vazia, o valor desta é invalido o que significa que é composta
     * por caracteres invalidos. No caso de ser uma entrada do usuario é necessario atualizar
     * a interface com a string formatada ja que as regras de formatação removem e modificam
     * caracteres fazendo que com o nome inserido pelo ususario fique (possivelmente) bem diferente
     * do que ele digitou.
     * */
    fun String.formatarComoNomeValido(): String {
        var nome: String = this
        
        nome = nome.replace(caracteresEspeciais, "")
        nome = nome.replace(espacosMultiplos, " ")
        
        if (nome.startsWith(" ")) nome = nome.drop(1)
        if (nome.endsWith(" ")) nome = nome.dropLast(1)
        
        return nome.capitalizar()
    }
}