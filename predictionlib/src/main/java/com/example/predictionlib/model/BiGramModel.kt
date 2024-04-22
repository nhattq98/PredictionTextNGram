package com.example.predictionlib.model

import com.orm.SugarRecord

class BiGramModel : SugarRecord<Any?>() {
    var firstWord: String? = null
    var secondWord: String? = null
    var frequency: Long = 0
}
