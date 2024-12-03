package org.ts;

import java.util.ArrayList;
import java.util.List;

import cn.hutool.core.util.RandomUtil;

// 定义簇类
class Cluster {
    List<Point> points;

    public Cluster(List<Point> points) {
        this.points = new ArrayList<>(points);
    }

    // 计算两个簇之间的最小距离（单链接）
    public double distance(Cluster other) {
        double minDistance = Double.MAX_VALUE;
        for (Point p1 : this.points) {
            for (Point p2 : other.points) {
                double dist = p1.distance(p2);
                minDistance = Math.min(minDistance, dist);
            }
        }
        return minDistance;
    }

    // 打印簇的内容
    public void printCluster() {
        System.out.print("[");
        for (Point p : points) {
            System.out.printf("(%.2f, %.2f) ", p.x, p.y);
        }
        System.out.println("]");
    }
}

/**
 * 自底向上的层次聚类
 */
public class HierarchicalClustering {
    private List<Point> points;
    private List<Cluster> clusters;
    private double distanceThreshold; // 距离阈值

    public HierarchicalClustering(List<Point> points, double distanceThreshold) {
        this.points = points;
        this.clusters = new ArrayList<>();
        this.distanceThreshold = distanceThreshold; // 初始化距离阈值
        // 每个点初始化为一个簇
        for (Point p : points) {
            List<Point> pointList = new ArrayList<>();
            pointList.add(p);
            clusters.add(new Cluster(pointList));
        }
    }

    // 执行层次聚类
    public void run() {
        while (clusters.size() > 1) {
            // 计算所有簇之间的距离
            double minDistance = Double.MAX_VALUE;
            int cluster1Index = -1;
            int cluster2Index = -1;

            // 找到距离最小的两个簇
            for (int i = 0; i < clusters.size(); i++) {
                for (int j = i + 1; j < clusters.size(); j++) {
                    double distance = clusters.get(i).distance(clusters.get(j));
                    if (distance < minDistance) {
                        minDistance = distance;
                        cluster1Index = i;
                        cluster2Index = j;
                    }
                }
            }

            // 如果最小距离大于距离阈值，则停止合并
            if (minDistance > distanceThreshold) {
                System.out.println("最小距离超过距离阈值，停止合并簇");
                break;
            }

            // 打印调试信息，查看找到的簇的内容
            System.out.println("Merging clusters " + cluster1Index + " and " + cluster2Index);
            System.out.print("Cluster " + cluster1Index + " before merge: ");
            clusters.get(cluster1Index).printCluster();
            System.out.print("Cluster " + cluster2Index + " before merge: ");
            clusters.get(cluster2Index).printCluster();

            // 合并这两个簇
            Cluster cluster1 = clusters.get(cluster1Index);
            Cluster cluster2 = clusters.get(cluster2Index);
            List<Point> mergedPoints = new ArrayList<>(cluster1.points);
            mergedPoints.addAll(cluster2.points);
            Cluster mergedCluster = new Cluster(mergedPoints);

            // 移除被合并的簇并添加新簇
            clusters.remove(cluster1Index);
            clusters.remove(cluster2Index > cluster1Index ? cluster2Index - 1 : cluster2Index);
            clusters.add(mergedCluster);

            // 打印合并后的簇
            System.out.print("Cluster after merge: ");
            mergedCluster.printCluster();
            System.out.println("Remaining clusters: " + clusters.size());
        }
    }

    // 打印最终簇
    public void printClusters() {
        for (int i = 0; i < clusters.size(); i++) {
            System.out.print("Cluster " + i + ": ");
            clusters.get(i).printCluster();
        }
    }

    public double calculateSilhouetteScore() {
        double totalSilhouetteScore = 0.0;

        // 遍历所有数据点
        for (Point p : points) {
            // 计算 a(i)：点 p 到同簇其他点的平均距离
            double a = 0.0;
            int sameClusterCount = 0;
            for (Point other : points) {
                if (other != p && other.clusterId == p.clusterId) {
                    a += p.distance(other);
                    sameClusterCount++;
                }
            }
            a /= sameClusterCount;

            // 计算 b(i)：点 p 到其他簇的最小平均距离
            double b = Double.MAX_VALUE;
            for (int i = 0; i < clusters.size(); i++) {
                if (i != p.clusterId) {
                    double sumDist = 0.0;
                    int count = 0;
                    for (Point other : points) {
                        if (other.clusterId == i) {
                            sumDist += p.distance(other);
                            count++;
                        }
                    }
                    b = Math.min(b, sumDist / count);
                }
            }

            // 计算点 p 的轮廓系数
            totalSilhouetteScore += (b - a) / Math.max(a, b);
        }

        // 返回所有点的平均轮廓系数
        return totalSilhouetteScore / points.size();
    }

    // 主函数
    public static void main(String[] args) {
        List<Point> dataPoints = new ArrayList<>();

        // 随机生成一些示例数据点
        for (int i = 0; i < 10; i++) {
            dataPoints.add(new Point(RandomUtil.randomDouble(200), RandomUtil.randomDouble(200)));
        }

        // 打印初始数据点
        System.out.println("Initial data points:");
        for (Point p : dataPoints) {
            System.out.printf("(%.3f, %.3f)\n", p.x, p.y);
        }

        // 设置距离阈值
        double distanceThreshold = 40;

        // 创建层次聚类实例并运行
        HierarchicalClustering hc = new HierarchicalClustering(dataPoints, distanceThreshold);
        hc.run(); // 运行层次聚类
        hc.printClusters(); // 打印聚类结果
        System.out.println(hc.calculateSilhouetteScore());
    }
}
