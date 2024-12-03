package org.ts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.hutool.core.util.RandomUtil;

/**
 * K-means聚类
 * 核心流程 ：
 * 选择簇中心（随机选取或使用K-means++）
 * 给每个点分配到最近的簇中心
 * 根据分配的点来重新更新簇中心（取平均值）
 * 判断簇中心是否收敛
 * 
 * 聚类后还可通过分析聚类的结果找到最佳的K值（使用肘部法则，计算轮廓系数等等）
 */
public class KMeans {
    private int k; // 簇的数量
    private List<Point> points; // 数据点列表
    private List<Point> centroids; // 簇的中心点（质心）

    public KMeans(int k, List<Point> points) {
        this.k = k;
        this.points = points;
        this.centroids = new ArrayList<>();
    }

    /**
     * 随机初始化簇中心
     * 性能更高 误差较大
     */
    private void randoIinitializeCentroids() {
        Random rand = new Random();
        centroids.clear();
        while (centroids.size() < k) {
            Point p = points.get(rand.nextInt(points.size()));
            if (!centroids.contains(p)) {
                centroids.add(p.clone());
            }
        }
    }

    /**
     * K-means++选择簇中心
     * 比较稳定 开销更大
     */
    private void kMeansPlusInitializeCentroids() {
        Random rand = new Random();
        centroids.clear();

        // 选择第一个簇中心
        centroids.add(points.get(rand.nextInt(points.size())));

        while (centroids.size() < k) {
            double[] distSq = new double[points.size()];

            // 对每个点计算距离最近的簇中心的距离平方
            for (int i = 0; i < points.size(); i++) {
                distSq[i] = Double.MAX_VALUE;
                for (Point centroid : centroids) {
                    double dist = points.get(i).distance(centroid);
                    distSq[i] = Math.min(distSq[i], dist * dist);
                }
            }

            // 按照距离的平方选择下一个簇中心
            double sum = 0.0;
            for (double dist : distSq) {
                sum += dist;
            }

            double randVal = rand.nextDouble() * sum;
            double cumulativeDist = 0.0;
            for (int i = 0; i < points.size(); i++) {
                cumulativeDist += distSq[i];
                if (cumulativeDist >= randVal) {
                    centroids.add(points.get(i).clone());
                    break;
                }
            }
        }
    }

    // 2. 对每个点分配到最近的簇中心
    private void assignClusters() {
        for (Point p : points) {
            double minDist = Double.MAX_VALUE;
            int closestCentroidIndex = -1;
            for (int i = 0; i < k; i++) {
                double dist = p.distance(centroids.get(i));
                if (dist < minDist) {
                    minDist = dist;
                    closestCentroidIndex = i;
                }
            }
            p.clusterId = closestCentroidIndex;
        }
    }

    // 3. 更新簇中心
    private void updateCentroids() {
        for (int i = 0; i < k; i++) {
            double sumX = 0, sumY = 0;
            int count = 0;
            for (Point p : points) {
                if (p.clusterId == i) {
                    sumX += p.x;
                    sumY += p.y;
                    count++;
                }
            }
            if (count > 0) {
                centroids.get(i).x = sumX / count;
                centroids.get(i).y = sumY / count;
            }
        }
    }

    // 4. 判断是否收敛（簇中心是否不再变化）
    private boolean isConverged(List<Point> previousCentroids) {
        for (int i = 0; i < k; i++) {
            if (centroids.get(i).distance(previousCentroids.get(i)) > 0.0001) {
                return false;
            }
        }
        return true;
    }

    // 计算轮廓系数
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
            for (int i = 0; i < k; i++) {
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

    public void run() {
        kMeansPlusInitializeCentroids();
        List<Point> previousCentroids = new ArrayList<>();

        while (true) {
            // 记录当前的簇中心
            previousCentroids.clear();
            for (Point p : centroids) {
                previousCentroids.add(new Point(p.x, p.y));
            }

            assignClusters();
            updateCentroids();

            // 检查是否收敛
            if (isConverged(previousCentroids)) {
                break;
            }
        }
    }

    // 输出聚类结果
    public void printResults() {
        System.out.println("Final Centroids:");
        for (int i = 0; i < k; i++) {
            Point centroid = centroids.get(i);
            System.out.printf("Centroid%d: %.3f,%.3f \n", i, centroid.x, centroid.y);
        }

        System.out.println("\nData Points and their Cluster IDs:");
        for (Point p : points) {
            System.out.printf("Point (%.3f,%.3f) -> Cluster %d \n", p.x, p.y, p.clusterId);
        }

        // 计算并打印轮廓系数
        double silhouetteScore = calculateSilhouetteScore();
        System.out.printf("\n轮廓系数 %.3f\n", silhouetteScore);
    }

    // 主函数测试
    public static void main(String[] args) {
        List<Point> dataPoints = new ArrayList<>();
        int n = 30;
        int k = 5;
        // 创建一些示例数据点
        for (int i = 0; i < n; i++) {
            dataPoints.add(new Point(RandomUtil.randomDouble(n), RandomUtil.randomDouble(n)));
        }
        // 创建 KMeans 实例
        KMeans kMeans = new KMeans(k, dataPoints); // 假设要分为 3 个簇
        kMeans.run(); // 运行 KMeans 算法
        kMeans.printResults(); // 打印结果
    }

    public void setNumClusters(int i) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setNumClusters'");
    }
}
