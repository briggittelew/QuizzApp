package com.briggittelew.quizzapp

data class QuestionsDC(val id: String, val difficulty: Int, val text: String,
                       val answers: ArrayList<String>)