package com.github.rey5137.robotrunnerplugin.editors.xml

import org.junit.Test

class ArgumentsParserTest {

    @Test
    fun parseArguments__withSingleValue() {
        val arguments = "\${a1}='a\\'b | c \"q' | \${a2}=None | \${a3}=234 | \${a4}=5.67 | \${a5}=True | \${a6}=False".parseArguments()
        arguments.forEach { println(it) }
    }

    @Test
    fun parseArguments__withDictValue() {
        val arguments = "\${a1}='qwe' | \${a2}='abc' | &{dic}={'a\\'5': True, \"a'4\": 5.1, 'a3': 4} ".parseArguments()
        arguments.forEach { println(it) }
    }
}