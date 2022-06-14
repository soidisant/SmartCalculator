fun main() {
    val report = readLine()!!
    // write your code here.
    print(report.matches("\\d wrong answers?".toRegex()))
}
