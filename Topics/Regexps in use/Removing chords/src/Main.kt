fun main() {
    val text = readln()
    println("""(\s*(Am|A|Em|E|Dm|D|G|C)\b)""".toRegex().replace(text, ""))
}
