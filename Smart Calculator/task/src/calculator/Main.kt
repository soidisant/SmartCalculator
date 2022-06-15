package calculator

import java.math.BigInteger

class Calculator {

    companion object {
        private const val EXIT_CMD: String = "/exit"
        private const val HELP_CMD: String = "/help"
        private const val MEMORY_CMD: String = "/mem"

        private val CMD_FORMAT: Regex = """/\w+""".toRegex()
        private val ASSIGNMENT_FORMAT: Regex = """^(?>(?<identifier>\w+)=(?<value>.+))$""".toRegex()
        private val IDENTIFIER_FORMAT: Regex = "[a-zA-Z]+".toRegex()

        private enum class OPERATOR(val symbol: String, val priority: Int, val canBeUnary: Boolean = false) {
            PLUS("+", 0, true), MINUS("-", 0, true), TIMES(
                "*", 1
            ),
            DIV("/", 1),
            POW("^", 2)
        }

        private interface Token

        private class NumberToken(val value: BigInteger) : Token {
            override fun toString(): String {
                return value.toString()
            }
        }

        private class OperatorToken(val operator: OPERATOR) : Token {

            override fun toString(): String {
                return operator.symbol
            }

            fun binary(a: BigInteger, b: BigInteger): BigInteger =
                when (operator) {
                    OPERATOR.PLUS -> a.add(b)
                    OPERATOR.MINUS -> a.minus(b)
                    OPERATOR.TIMES -> a.times(b)
                    OPERATOR.DIV -> {
                        if (b == BigInteger.valueOf(0)) {
                            throw Exception("Can't divide by zero!")
                        }
                        a / b
                    }
                    OPERATOR.POW -> {
                        if (b > BigInteger.valueOf(Int.MAX_VALUE.toLong())) {
                            throw Exception("power exponent cannot exceed ${Int.MAX_VALUE}. Error at : $a^$b ")
                        }
                        a.pow(b.toInt())
                    }
                }

            fun unary(a: BigInteger): BigInteger =
                when (operator) {
                    OPERATOR.MINUS -> a.unaryMinus()
                    else -> a
                }
        }

        private class VariableToken(val key: String) : Token
        private class LeftParenthesisToken : Token {
            override fun toString(): String {
                return "("
            }
        }

        private class RightParenthesisToken : Token {
            override fun toString(): String {
                return ")"
            }
        }
    }

    private val variables = mutableMapOf<String, BigInteger>()

    private enum class STATE { EXIT, READ }

    private var state = STATE.READ

    // parses the input string
    private fun parse(userInput: String) {
        if (userInput.isEmpty()) {
            return
        }

        // handling special commands
        if (CMD_FORMAT.matches(userInput)) {
            if (userInput == EXIT_CMD) {
                state = STATE.EXIT
                return
            }
            if (userInput == HELP_CMD) {
                println("The program calculates the sum of numbers")
                return
            }
            if (userInput == MEMORY_CMD) {
                if (variables.isEmpty()) {
                    println("No variable set")
                } else {
                    println("Stored variables:")
                    for ((k, v) in variables) {
                        println("$k = $v")
                    }
                }
                return
            }
            println("Unknown command")
            return
        }

        // handling variable assignment
        if (ASSIGNMENT_FORMAT.matches(userInput)) {
            val identifier = ASSIGNMENT_FORMAT.find(userInput)!!.groups["identifier"]!!.value
            if (!identifier.matches(IDENTIFIER_FORMAT)) {
                println("Invalid identifier")
                return
            }
            val value = ASSIGNMENT_FORMAT.find(userInput)!!.groups["value"]!!.value
            if (value.matches("[+-]?\\d+".toRegex())) {
                variables[identifier] = BigInteger(value, 10)
                return
            }
            if (value.matches(IDENTIFIER_FORMAT)) {
                if (variables.keys.contains(value)) {
                    variables[identifier] = variables[value]!!
                } else {
                    println("Unknown variable")
                }
                return
            }
            println("Invalid assignment")
            return
        }

        // parsing equation
        try {
            // converting to postFix notation
            val postfixExpression = postFixNotation(userInput)
            // solving the expression and printing the result
            println(solve(postfixExpression))
        } catch (e: Exception) {
            println(e.message)
        }
    }

    private fun solve(postfixExpression: MutableList<Token>): BigInteger {
        val stack = mutableListOf<BigInteger>()
        while (postfixExpression.isNotEmpty()) {
            when (val token = postfixExpression.removeFirst()) {
                is NumberToken -> stack.add(token.value)
                is OperatorToken -> {
                    if (stack.size >= 2) {
                        val b = stack.removeLast()
                        val a = stack.removeLast()
                        // println("$a ${token.operator.symbol} $b")
                        stack.add(token.binary(a, b))
                    } else if (stack.size == 1 && token.operator.canBeUnary) {
                        stack.add(token.unary(stack.removeLast()))
                    }
                }
            }
        }
        if (stack.size == 1) {
            return stack.last()
        }
        throw Exception("Invalid expression")
    }

    private fun getToken(rawToken: String): Token {
        if (rawToken.matches("\\d+".toRegex())) {
            // it's a number
            return NumberToken(value = BigInteger(rawToken, 10))
        } else if (rawToken == ")") {
            return RightParenthesisToken()
        } else if (rawToken == "(") {
            return LeftParenthesisToken()
        } else if (rawToken.matches("[-+/*^]+".toRegex())) {
            // handling --- or +++ instruction
            val correctedToken =
                if (rawToken.length == 1) {
                    rawToken
                } else if (rawToken.matches("\\++".toRegex())) {
                    "+"
                } else if (rawToken.matches("-+".toRegex())) {
                    // only minus signs -- = + || --- = -
                    if (rawToken.length % 2 == 0) {
                        "+"
                    } else {
                        "-"
                    }
                } else {
                    throw Exception("Invalid expression")
                }

            return when (OPERATOR.values().first { it.symbol == correctedToken }) {
                OPERATOR.PLUS -> OperatorToken(OPERATOR.PLUS)
                OPERATOR.MINUS -> OperatorToken(OPERATOR.MINUS)
                OPERATOR.TIMES -> OperatorToken(OPERATOR.TIMES)
                OPERATOR.DIV -> OperatorToken(OPERATOR.DIV)
                OPERATOR.POW -> OperatorToken(OPERATOR.POW)
            }
        } else {
            // last case, token may be a variable name, check will come later
            return VariableToken(rawToken)
        }
    }

    // create a list of tokens from the expression
    private fun tokenize(expression: String): MutableList<Token> {
        val tokens = mutableListOf<Token>()
        var currentToken: String? = null
        // parsing the expression by character
        for (c in expression) {
            if (c.toString().matches("[-+/*^]".toRegex())) {
                if (currentToken != null) {
                    if (currentToken.matches("[-+/*^]+".toRegex())) {
                        currentToken += c
                    } else {
                        tokens.add(getToken(currentToken))
                        currentToken = c
                            .toString()
                    }
                } else
                    currentToken = c.toString()
            }
            if (c.toString().matches("[()]".toRegex())) {
                if (currentToken != null) {
                    tokens.add(getToken(currentToken))
                    currentToken = null
                }
                tokens.add(getToken(c.toString()))
            }
            if (c.toString().matches("[\\da-zA-Z]".toRegex())) {
                if (currentToken != null) {
                    if (currentToken.matches("[\\da-zA-Z]+".toRegex())) {
                        currentToken += c
                    } else {
                        tokens.add(getToken(currentToken))
                        currentToken = c.toString()
                    }
                } else
                    currentToken = c.toString()
            }
        }
        if (currentToken != null) {
            tokens.add(getToken(currentToken))
        }

        return tokens
    }

    // Create a list of Token corresponding to a postfix notation of the input
    private fun postFixNotation(userInput: String): MutableList<Token> {

        val stack = mutableListOf<Token>()
        val operatorStack = mutableListOf<Token>()

        val tokens = tokenize(userInput)

        for (token in tokens) {
            when (token) {
                is NumberToken -> stack.add(token)
                is VariableToken -> {
                    // checking variable name
                    if (variables.keys.contains(token.key)) {
                        stack.add(NumberToken(variables[token.key]!!))
                    } else {
                        throw Exception("Unknown variable")
                    }
                }
                is OperatorToken -> {
                    if (operatorStack.isEmpty() || operatorStack.last() is LeftParenthesisToken) {
                        operatorStack.add(token)
                    } else if (token.operator.priority > (operatorStack.last() as OperatorToken).operator.priority) {
                        operatorStack.add(token)
                    } else if (token.operator.priority <= (operatorStack.last() as OperatorToken).operator.priority) {
                        do {
                            if (operatorStack.isEmpty())
                                break
                            val last = operatorStack.removeLast()
                            if (last is LeftParenthesisToken) {
                                operatorStack.add(last)
                                break
                            } else if (last is OperatorToken && last.operator.priority < token.operator.priority) {
                                operatorStack.add(last)
                                break
                            } else {
                                stack.add(last)
                            }
                        } while (true)
                        operatorStack.add(token)
                    }
                }
                is LeftParenthesisToken -> operatorStack.add(token)
                is RightParenthesisToken -> {
                    do {
                        if (operatorStack.isEmpty()) {
                            throw java.lang.Exception("Invalid expression")
                        }
                        val last = operatorStack.removeLast()
                        if (last is LeftParenthesisToken) {
                            break
                        } else {
                            stack.add(last)
                        }
                    } while (true)
                }
            }
        }

        if (operatorStack.filterIsInstance<LeftParenthesisToken>().isNotEmpty()) {
            throw java.lang.Exception("Invalid expression")
        }

        stack.addAll(operatorStack.reversed())
        return stack
    }

    fun start() {
        var userInput: String?
        do {
            userInput = readln()
            parse(userInput.trim().replace("\\s".toRegex(), ""))
        } while (state != STATE.EXIT)
        print("Bye!")
    }
}

fun main() {
    val calculator = Calculator()
    calculator.start()
}
