package com.brunoshiroma.vibememory.json

/**
 * Small JSON value tree sufficient to represent the flat arrays/objects
 * produced by the native benchmark bridge, without depending on a JSON
 * library.
 */
sealed class JsonValue {
    data class JsonObject(val fields: Map<String, JsonValue>) : JsonValue()
    data class JsonArray(val items: List<JsonValue>) : JsonValue()
    data class JsonString(val value: String) : JsonValue()
    data class JsonNumber(val value: Double) : JsonValue()
    data class JsonBool(val value: Boolean) : JsonValue()
    object JsonNull : JsonValue()
}

class JsonParseException(message: String) : Exception(message)

/** Minimal recursive-descent JSON parser used to decode native benchmark results. */
object MiniJson {

    fun parse(text: String): JsonValue {
        val parser = Parser(text)
        val value = parser.parseValue()
        parser.skipWhitespace()
        if (!parser.isAtEnd()) {
            throw JsonParseException("Unexpected trailing content at ${parser.position}")
        }
        return value
    }

    private class Parser(private val text: String) {
        var position = 0
            private set

        fun isAtEnd() = position >= text.length

        fun skipWhitespace() {
            while (!isAtEnd() && text[position].isWhitespace()) position++
        }

        fun parseValue(): JsonValue {
            skipWhitespace()
            if (isAtEnd()) throw JsonParseException("Unexpected end of input")
            return when (text[position]) {
                '{' -> parseObject()
                '[' -> parseArray()
                '"' -> JsonValue.JsonString(parseString())
                't' -> {
                    expect("true")
                    JsonValue.JsonBool(true)
                }
                'f' -> {
                    expect("false")
                    JsonValue.JsonBool(false)
                }
                'n' -> {
                    expect("null")
                    JsonValue.JsonNull
                }
                else -> JsonValue.JsonNumber(parseNumber())
            }
        }

        private fun expect(literal: String) {
            if (position + literal.length > text.length ||
                text.substring(position, position + literal.length) != literal
            ) {
                throw JsonParseException("Expected '$literal' at $position")
            }
            position += literal.length
        }

        private fun parseObject(): JsonValue.JsonObject {
            position++ // consume '{'
            val fields = LinkedHashMap<String, JsonValue>()
            skipWhitespace()
            if (!isAtEnd() && text[position] == '}') {
                position++
                return JsonValue.JsonObject(fields)
            }
            while (true) {
                skipWhitespace()
                val key = parseString()
                skipWhitespace()
                if (isAtEnd() || text[position] != ':') {
                    throw JsonParseException("Expected ':' at $position")
                }
                position++
                fields[key] = parseValue()
                skipWhitespace()
                if (isAtEnd()) throw JsonParseException("Unexpected end of input in object")
                when (text[position]) {
                    ',' -> position++
                    '}' -> {
                        position++
                        return JsonValue.JsonObject(fields)
                    }
                    else -> throw JsonParseException("Expected ',' or '}' at $position")
                }
            }
        }

        private fun parseArray(): JsonValue.JsonArray {
            position++ // consume '['
            val items = mutableListOf<JsonValue>()
            skipWhitespace()
            if (!isAtEnd() && text[position] == ']') {
                position++
                return JsonValue.JsonArray(items)
            }
            while (true) {
                items.add(parseValue())
                skipWhitespace()
                if (isAtEnd()) throw JsonParseException("Unexpected end of input in array")
                when (text[position]) {
                    ',' -> position++
                    ']' -> {
                        position++
                        return JsonValue.JsonArray(items)
                    }
                    else -> throw JsonParseException("Expected ',' or ']' at $position")
                }
            }
        }

        private fun parseString(): String {
            if (isAtEnd() || text[position] != '"') {
                throw JsonParseException("Expected string at $position")
            }
            position++
            val builder = StringBuilder()
            while (true) {
                if (isAtEnd()) throw JsonParseException("Unterminated string")
                when (val c = text[position]) {
                    '"' -> {
                        position++
                        return builder.toString()
                    }
                    '\\' -> {
                        position++
                        if (isAtEnd()) throw JsonParseException("Unterminated escape")
                        when (val escaped = text[position]) {
                            '"' -> builder.append('"')
                            '\\' -> builder.append('\\')
                            '/' -> builder.append('/')
                            'b' -> builder.append('\b')
                            'f' -> builder.append('\u000C')
                            'n' -> builder.append('\n')
                            'r' -> builder.append('\r')
                            't' -> builder.append('\t')
                            'u' -> {
                                val hex = text.substring(position + 1, position + 5)
                                builder.append(hex.toInt(16).toChar())
                                position += 4
                            }
                            else -> throw JsonParseException("Invalid escape '\\$escaped' at $position")
                        }
                        position++
                    }
                    else -> {
                        builder.append(c)
                        position++
                    }
                }
            }
        }

        private fun parseNumber(): Double {
            val start = position
            if (!isAtEnd() && text[position] == '-') position++
            while (!isAtEnd() && text[position].isDigit()) position++
            if (!isAtEnd() && text[position] == '.') {
                position++
                while (!isAtEnd() && text[position].isDigit()) position++
            }
            if (!isAtEnd() && (text[position] == 'e' || text[position] == 'E')) {
                position++
                if (!isAtEnd() && (text[position] == '+' || text[position] == '-')) position++
                while (!isAtEnd() && text[position].isDigit()) position++
            }
            if (position == start) throw JsonParseException("Expected number at $position")
            return text.substring(start, position).toDouble()
        }
    }
}
