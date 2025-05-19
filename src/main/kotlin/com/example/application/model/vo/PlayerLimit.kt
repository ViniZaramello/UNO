package com.example.application.model.vo

data class PlayerLimit(val playerLimit: Int = 4){
    init {
        //Criar um application.message para permitir internacionalização
        check(playerLimit < 8) { "A quantidade maxima de player é de 8!" }
    }
}
