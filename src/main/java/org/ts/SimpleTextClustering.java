package org.ts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;

public class SimpleTextClustering {

    public static void main(String[] args) {
        // 示例文档
        List<String> documents = Arrays.asList(
                "The quick brown fox jumps over the lazy dog",
                "A quick brown dog outfoxes a lazy fox",
                "The lazy dog sleeps in the sun",
                "The sun shines brightly in the sky",
                "The quick rabbit jumps over the fence");

        // 计算词频
        Map<String, Map<String, Integer>> docWordFreq = calculateWordFrequencies(documents);

        // 创建特征向量
        List<DoublePoint> points = createFeatureVectors(docWordFreq);

        // 使用K-means聚类
        int k = 2; // 聚类数
        KMeansPlusPlusClusterer<DoublePoint> clusterer = new KMeansPlusPlusClusterer<>(k, 1000);
        List<CentroidCluster<DoublePoint>> clusters = clusterer.cluster(points);

        // 打印结果
        for (int i = 0; i < clusters.size(); i++) {
            System.out.println("Cluster " + (i + 1) + ":");
            for (DoublePoint point : clusters.get(i).getPoints()) {
                int docIndex = points.indexOf(point);
                System.out.println("  - " + documents.get(docIndex));
            }
            System.out.println();
        }
    }

    private static Map<String, Map<String, Integer>> calculateWordFrequencies(List<String> documents) {
        Map<String, Map<String, Integer>> docWordFreq = new HashMap<>();
        for (String doc : documents) {
            Map<String, Integer> wordFreq = new HashMap<>();
            for (String word : doc.toLowerCase().split("\\s+")) {
                wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
            }
            docWordFreq.put(doc, wordFreq);
        }
        return docWordFreq;
    }

    private static List<DoublePoint> createFeatureVectors(Map<String, Map<String, Integer>> docWordFreq) {
        Set<String> allWords = new HashSet<>();
        for (Map<String, Integer> wordFreq : docWordFreq.values()) {
            allWords.addAll(wordFreq.keySet());
        }

        List<DoublePoint> points = new ArrayList<>();
        for (Map<String, Integer> wordFreq : docWordFreq.values()) {
            double[] vector = new double[allWords.size()];
            int i = 0;
            for (String word : allWords) {
                vector[i++] = wordFreq.getOrDefault(word, 0);
            }
            points.add(new DoublePoint(vector));
        }
        return points;
    }
}