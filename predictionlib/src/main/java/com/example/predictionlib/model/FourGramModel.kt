package com.example.predictionlib.model

import com.orm.SugarRecord

class FourGramModel : SugarRecord<Any?>() {
    var firstWord: String? = null
    var secondWord: String? = null
    var thirdWord: String? = null
    var fourthWord: String? = null
    var frequency: Long = 0
}
