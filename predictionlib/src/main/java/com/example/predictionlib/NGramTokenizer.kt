package com.example.predictionlib

/**
 * @author Ayhan Salami on 2/7/2017.
 * Email: ayhan.irta@gmail.com
 * Social Networks: ayhansalami
 */
object NGramTokenizer {
    fun ngrams(n: Int, str: String): List<String> {
        val ngrams: MutableList<String> = ArrayList()
        val words: Array<String?> = str.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        for (i in 0 until words.size - n + 1) ngrams.add(concat(words, i, i + n))
        return ngrams
    }

    fun concat(words: Array<String?>, start: Int, end: Int): String {
        val sb = StringBuilder()
        for (i in start until end) sb.append(if (i > start) " " else "").append(words[i])
        return sb.toString()
    }
}

//public class NGramTokenizer {
//    public static List<String> ngrams(int n, String str) {
//        List<String> ngrams = new ArrayList<String>();
//        String[] words = str.split(" ");
//        for (int i = 0; i < words.length - n + 1; i++)
//        ngrams.add(concat(words, i, i+n));
//        return ngrams;
//    }
//
//    public static String concat(String[] words, int start, int end) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = start; i < end; i++)
//        sb.append(i > start ? " " : "").append(words[i]);
//        return sb.toString();
//    }
//}
