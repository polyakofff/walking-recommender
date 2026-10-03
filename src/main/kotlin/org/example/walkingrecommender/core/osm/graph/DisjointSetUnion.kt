package org.example.walkingrecommender.core.osm.graph

class DisjointSetUnion<T> {

    private val parent = mutableMapOf<T, T>()
    private val rank = mutableMapOf<T, Int>()

    fun add(value: T) {
        parent.putIfAbsent(value, value)
        rank.putIfAbsent(value, 1)
    }

    fun find(value: T): T {
        val p = parent[value]!!
        if (value == p) {
            return value
        }
        parent[value] = find(p)
        return parent[value]!!
    }

    fun union(a: T, b: T) {
        val rootA = find(a)
        val rootB = find(b)
        if (rootA == rootB) {
            return
        }
        val rankA = rank[rootA]!!
        val rankB = rank[rootB]!!
        when {
            rankA > rankB -> parent[rootB] = rootA
            rankA < rankB -> parent[rootA] = rootB
            else -> {
                parent[rootB] = rootA
                rank[rootA] = rankA + 1
            }
        }
    }
}