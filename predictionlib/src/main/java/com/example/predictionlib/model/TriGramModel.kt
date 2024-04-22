package com.example.predictionlib.model

import com.orm.SugarRecord

class TriGramModel : SugarRecord<Any?>() {
    var firstWord: String? = null
    var secondWord: String? = null
    var thirdWord: String? = null
    var frequency: Long = 0
}
