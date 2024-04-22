package com.example.predictionlib

import android.content.Context
import com.example.predictionlib.database.EnDictionaryDatabaseHelper
import com.example.predictionlib.listener.OnPredictListener
import com.example.predictionlib.model.BiGramModel
import com.example.predictionlib.model.FourGramModel
import com.example.predictionlib.model.TriGramModel
import com.example.predictionlib.model.UniGramModel
import com.example.predictionlib.tasks.PredictCurrentWordAsyncTask
import com.orm.SugarRecord
import java.io.IOException
import java.util.LinkedList
import java.util.Locale

class Predictor(private val context: Context) {
    private val enDbHelper: EnDictionaryDatabaseHelper = EnDictionaryDatabaseHelper(context)
    val PACKAGE_NAME = context.packageName

    init {
        try {
            enDbHelper.createDataBase()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun learnSentence(sentence: String) {
        learnEnSentence(sentence.lowercase(Locale.getDefault()).trim())
    }

    fun predictCurrentWord(number: Int, word: String): List<String>? {
        return predictCurrentEnWord(number, word.lowercase(Locale.getDefault()).trim())
    }

    fun selectWord(word: String) {
        selectWordEn(word.lowercase(Locale.getDefault()).trim())
    }

    fun addWord(word: String) {
        enDbHelper.addWord(word.lowercase(Locale.getDefault()).trim())
    }

    fun isWordExistInDictionary(word: String): Boolean {
        return enDbHelper.wordExistInDictionary(word.lowercase(Locale.getDefault()).trim())
    }

    // predict current
    fun predictCurrentWordAsync(
        number: Int,
        word: String?,
        onPredictListener: OnPredictListener
    ) {
        if (word.isNullOrEmpty()) return
        PredictCurrentWordAsyncTask(this, onPredictListener, word, number).execute()
    }

    fun predictNextWord(number: Int, sentence: String): List<String> {
        return predictNextEnWord(number, sentence.lowercase(Locale.getDefault()).trim())
    }

    //
//    //Async Functions
//    fun predictCurrentWordAsync(
//        language: LanguageEnum?,
//        number: Int,
//        word: String?,
//        onPredictListener: OnPredictListener?
//    ) {
//        val asyncTask = PredictCurrentWordAsyncTask(this, onPredictListener, word, number, language)
//        asyncTask.execute()
//    }
//
//    fun predictNextWordAsync(
//        language: LanguageEnum?,
//        number: Int,
//        sentence: String?,
//        onPredictListener: OnPredictListener?
//    ) {
//        val asyncTask =
//            PredictNextWordAsyncTask(this, onPredictListener, sentence, number, language)
//        asyncTask.execute()
//    }
//
//    fun isWordExistInDictionaryAsync(
//        language: LanguageEnum?,
//        word: String?,
//        onWordCheck: OnWordCheck?
//    ) {
//        val asyncTask = WordExistInDictionaryAsyncTask(this, onWordCheck, word, language)
//        asyncTask.execute()
//    }
//
    private fun predictNextEnWord(number: Int, sentence: String?): List<String> {
        if (sentence == null) {
            return LinkedList()
        }
        val words =
            listOf(*sentence.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        val predictedWords: MutableList<String> = LinkedList()
        if (words.size >= 3) { //can use four gram
            val fourGramPredictedWords: List<FourGramModel> = SugarRecord.find(
                FourGramModel::class.java,
                "first_word = ? AND second_word = ? AND third_word = ?",
                arrayOf(words[words.size - 3], words[words.size - 2], words[words.size - 1]),
                null,
                "frequency DESC",
                null
            )
            for (fourGram in fourGramPredictedWords) {
                if (predictedWords.size < number && !predictedWords.contains(fourGram.fourthWord)) {
                    fourGram.fourthWord?.let {
                        predictedWords.add(it)
                    }
                } else if (predictedWords.size >= number) {
                    return predictedWords
                }
            }
        }
        if (words.size >= 2 && predictedWords.size < number) { //can use tri gram
            val triGramPredictedWords: List<TriGramModel> = SugarRecord.find(
                TriGramModel::class.java,
                "first_word = ? AND second_word = ?",
                arrayOf(words[words.size - 2], words[words.size - 1]),
                null,
                "frequency DESC",
                null
            )
            for (triGram in triGramPredictedWords) {
                if (predictedWords.size < number && !predictedWords.contains(triGram.thirdWord)) {
                    triGram.thirdWord?.let {
                        predictedWords.add(it)
                    }
                } else if (predictedWords.size >= number) {
                    return predictedWords
                }
            }
        }
        if (words.isNotEmpty() && predictedWords.size < number) { //can use bi gram
            val biGramPredictedWords: List<BiGramModel> = SugarRecord.find(
                BiGramModel::class.java,
                "first_word = ?",
                arrayOf(words[words.size - 1]),
                null,
                "frequency DESC",
                null
            )
            for (biGram in biGramPredictedWords) {
                if (predictedWords.size < number && !predictedWords.contains(biGram.secondWord)) {
                    biGram.secondWord?.let {
                        predictedWords.add(it)
                    }

                } else if (predictedWords.size >= number) {
                    return predictedWords
                }
            }
        }
        if (predictedWords.size < number) { //can use uni gram
            val uniGramPredictedWords: List<UniGramModel> = SugarRecord.find(
                UniGramModel::class.java,
                null,
                arrayOf(),
                null,
                "frequency DESC",
                null
            )
            for (uniGram in uniGramPredictedWords) {
                if (predictedWords.size < number && !predictedWords.contains(uniGram.word)) {
                    uniGram.word?.let {
                        predictedWords.add(it)
                    }
                } else if (predictedWords.size >= number) {
                    return predictedWords
                }
            }
        }
        return predictedWords
    }

    private fun learnEnSentence(sentence: String) {
        val sentences: MutableList<String> = LinkedList()
        if (sentence.contains(".")) {
            sentences.addAll(
                listOf(*sentence.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray())
            )
        } else {
            sentences.add(sentence)
        }
        for (itemSentence in sentences) {
            val singleSentence = itemSentence.replace("[^a-zA-Z0-9\\s]".toRegex(), "")
            //UniGram
            val uniGrams = NGramTokenizer.ngrams(1, singleSentence)
            for (uniGram in uniGrams) {
                val uniGramModelList: List<UniGramModel>? = SugarRecord.find(
                    UniGramModel::class.java, "word = ?", uniGram
                )
                if (uniGramModelList != null && uniGramModelList.size == 1) {
                    val uniGramModel: UniGramModel = uniGramModelList[0]
                    uniGramModel.apply {
                        frequency += 1
                        save()
                    }
                } else if (uniGramModelList == null || uniGramModelList.size == 0) {
                    val uniGramModel = UniGramModel()
                    uniGramModel.apply {
                        word = uniGram
                        frequency = 1
                        save()
                    }
                }
            }
            //BiGram
            val biGrams = NGramTokenizer.ngrams(2, singleSentence)
            for (biGram in biGrams) {
                if (biGram == null || !biGram.contains(" ")) {
                    continue
                }
                val biGramParts =
                    biGram.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val biGramModelList: List<BiGramModel>? = SugarRecord.find(
                    BiGramModel::class.java,
                    "first_word = ? AND second_word = ?",
                    biGramParts[0],
                    biGramParts[1]
                )
                if (biGramModelList != null && biGramModelList.size == 1) {
                    val biGramModel: BiGramModel = biGramModelList[0]
                    biGramModel.apply {
                        frequency += 1
                        save()
                    }
                } else if (biGramModelList == null || biGramModelList.size == 0) {
                    val biGramModel = BiGramModel()
                    biGramModel.apply {
                        firstWord = biGramParts[0]
                        secondWord = biGramParts[1]
                        frequency = 1
                        save()
                    }
                }
            }
            //TriGram
            val triGrams = NGramTokenizer.ngrams(3, singleSentence)
            for (triGram in triGrams) {
                if (triGram == null || !triGram.contains(" ")) {
                    continue
                }
                val triGramParts =
                    triGram.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val triGramModelList: List<TriGramModel>? = SugarRecord.find(
                    TriGramModel::class.java,
                    "first_word = ? AND second_word = ? AND third_word = ?",
                    triGramParts[0], triGramParts[1], triGramParts[2]
                )
                if (triGramModelList != null && triGramModelList.size == 1) {
                    val triGramModel: TriGramModel = triGramModelList[0]
                    triGramModel.apply {
                        frequency += 1
                        save()
                    }
                } else if (triGramModelList == null || triGramModelList.size == 0) {
                    val triGramModel = TriGramModel()
                    triGramModel.apply {
                        firstWord = triGramParts[0]
                        secondWord = triGramParts[1]
                        thirdWord = triGramParts[2]
                        frequency = 1
                        save()
                    }
                }
            }
            //FourGram
            val fourGrams = NGramTokenizer.ngrams(4, singleSentence)
            for (fourGram in fourGrams) {
                if (fourGram == null || !fourGram.contains(" ")) {
                    continue
                }
                val fourGramParts =
                    fourGram.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val fourGramModelList: List<FourGramModel>? = SugarRecord.find(
                    FourGramModel::class.java,
                    "first_word = ? AND second_word = ? AND third_word = ? AND fourth_word = ?",
                    fourGramParts[0], fourGramParts[1], fourGramParts[2], fourGramParts[3]
                )
                if (fourGramModelList != null && fourGramModelList.size == 1) {
                    val fourGramModel: FourGramModel = fourGramModelList[0]
                    fourGramModel.apply {
                        frequency += 1
                        save()
                    }
                } else if (fourGramModelList == null || fourGramModelList.size == 0) {
                    val fourGramModel = FourGramModel()
                    fourGramModel.apply {
                        firstWord = fourGramParts[0]
                        secondWord = fourGramParts[1]
                        thirdWord = fourGramParts[2]
                        secondWord = fourGramParts[3]
                        frequency = 1
                        save()
                    }

                }
            }
        }
    }

    private fun predictCurrentEnWord(number: Int, word: String): List<String>? {
        return enDbHelper.predictCurrentWord(number, word)
    }

    private fun selectWordEn(word: String) {
        enDbHelper.selectWord(word)
    }
}
