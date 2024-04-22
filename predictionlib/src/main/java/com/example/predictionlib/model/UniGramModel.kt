package com.example.predictionlib.model

import com.orm.SugarRecord

class UniGramModel : SugarRecord<Any?>() {
    var word: String? = null
    var frequency: Long = 0
}
