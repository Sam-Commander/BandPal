package com.sam.tippy

class Message {
    var message: String ?= null
    var idOfSender: String ?= null

    constructor(){}

    constructor(message: String?, idOfSender: String?){
        this.message = message
        this.idOfSender = idOfSender
    }
}