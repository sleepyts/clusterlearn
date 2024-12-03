package org.ts;

/**
 * 数据点
 * 在真正聚类是改数据可能是经过处理后且能代表数据特征的数据
 * 不一定是点
 */
class Point implements Cloneable {
    double x, y; // 假设是二维数据点
    int clusterId; // 所属簇的标识

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // 计算当前点与另一个点的欧几里得距离
    public double distance(Point p) {
        return Math.sqrt(Math.pow(this.x - p.x, 2) + Math.pow(this.y - p.y, 2));
    }

    @Override
    protected Point clone() {
        return new Point(x, y);
    }
}