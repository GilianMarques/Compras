package dev.gmarques.compras.objetos

/*propriedades padrao para todos os modelos de objeto*/
interface Sinc {
    var ultimaAtualizacao: Long
    var id: String
    var nome: String
    var removido: Boolean
}