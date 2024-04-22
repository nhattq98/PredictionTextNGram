package com.example.predictionlib.tasks

import com.example.predictionlib.Predictor
import com.example.predictionlib.listener.OnPredictListener

class PredictNextWordAsyncTask(
    val predictor: Predictor,
    val listener: OnPredictListener,
    val sentence: String,
    val number: Int,
) :
    CoroutinesAsyncTask<Unit, Unit, List<String>?>("PredictNextWordAsyncTask") {
    override fun doInBackground(vararg params: Unit?): List<String>? {
        return try {
            predictor.predictNextWord(number, sentence)
        } catch (ex: Exception) {
            null
        }
    }

    override fun onPostExecute(result: List<String>?) {
        super.onPostExecute(result)
        listener.onPredict(result)
    }
}