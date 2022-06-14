fun main() {
    val text = readln()
    println(text.replace("[aA]+".toRegex(),"a"))
}