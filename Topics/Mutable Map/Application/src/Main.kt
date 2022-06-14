fun main() {
    val studentsMarks = mutableMapOf<String, Int>()
    var name: String?
    do {
        name = readln()

        if(name == "stop") {
            break
        }else{
            val score = readln().toInt()

            if (!(name in studentsMarks.keys)) {
                studentsMarks[name] = score
            }
        }
    } while (true)
    println(studentsMarks)
}
