fun helpingTheRobot(purchases: Map<String, Int>, addition: Map<String, Int>): MutableMap<String, Int> {
    // write your code here
    val map = purchases.toMutableMap()
    for (key in addition.keys) {
        map[key] =  map.getOrPut(key) {
            0
        }!! + addition[key]!!
    }
    return map
}
