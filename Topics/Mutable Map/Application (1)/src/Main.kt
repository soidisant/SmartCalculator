fun addUser(userMap: Map<String, String>, login: String, password: String): MutableMap<String, String> {
    // write your code here
    return userMap.toMutableMap().also {
        if (it.keys.contains(login)) {
            println("User with this login is already registered!")
        } else {
            it[login] = password
        }
    }
}
