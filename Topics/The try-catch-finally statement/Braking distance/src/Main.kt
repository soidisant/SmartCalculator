fun calculateBrakingDistance(v1: String, a: String): Int {
    return try {
        -(v1.toInt() * v1.toInt()) / (2 * a.toInt())
    } catch (e: ArithmeticException) {
        println("The car does not slow down!")
        -1
    } catch (e: Exception) {
        println(e.message)
        -1
    }
}
