package dev.gmarques.compras.domain.entidades

/*propriedades padrao para todos os modelos de objeto sincronizaveis*/
interface Sincronizavel {
    var ultimaAtualizacao: Long
    var id: String
    var removido: Boolean
}