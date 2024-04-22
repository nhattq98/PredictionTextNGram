package com.example.predictionlib.tasks

import com.example.predictionlib.Predictor
import com.example.predictionlib.listener.OnPredictListener

class PredictCurrentWordAsyncTask(
    val predictor: Predictor,
    val listener: OnPredictListener,
    val currentWord: String,
    val number: Int,
) :
    CoroutinesAsyncTask<Unit, Unit, List<String>?>("PredictCurrentWordAsyncTask") {
    override fun doInBackground(vararg params: Unit?): List<String>? {
        return try {
            predictor.predictCurrentWord(number, currentWord)
        } catch (ex: Exception) {
            null
        }
    }

    override fun onPostExecute(result: List<String>?) {
        super.onPostExecute(result)
        listener.onPredict(result)
    }
}