package io.bandroid.simplefetch

/**
 * Lightweight, zero-dependency JSON parser and serializer.
 * Translates JSON directly to Map<String, Any?> and List<Any?> for seamless untyped REST calls.
 */
object SimpleJson {

    /**
     * Parses a JSON string into Map<String, Any?>, List<Any?>, String, Number, Boolean, or null.
     */
    fun parse(jsonStr: String): Any? {
        val trimmed = jsonStr.trim()
        if (trimmed.isEmpty()) return null
        val parser = JsonParser(trimmed)
        return parser.parseValue()
    }

    /**
     * Parses a JSON string strictly into a Map<String, Any?>.
     */
    @Suppress("UNCHECKED_CAST")
    fun parseObject(jsonStr: String): Map<String, Any?> {
        val parsed = parse(jsonStr)
        return (parsed as? Map<String, Any?>) ?: emptyMap()
    }

    /**
     * Parses a JSON string strictly into a List<Any?>.
     */
    @Suppress("UNCHECKED_CAST")
    fun parseArray(jsonStr: String): List<Any?> {
        val parsed = parse(jsonStr)
        return (parsed as? List<Any?>) ?: emptyList()
    }

    /**
     * Serializes any Kotlin object (Map, List, Primitives) into a JSON string.
     */
    fun stringify(value: Any?, pretty: Boolean = false, indentLevel: Int = 0): String {
        val indent = if (pretty) "  ".repeat(indentLevel) else ""
        val nextIndent = if (pretty) "  ".repeat(indentLevel + 1) else ""
        val nl = if (pretty) "\n" else ""
        val sp = if (pretty) " " else ""

        return when (value) {
            null -> "null"
            is Boolean -> value.toString()
            is Number -> value.toString()
            is CharSequence -> escapeString(value.toString())
            is Map<*, *> -> {
                if (value.isEmpty()) return "{}"
                val entries = value.entries.joinToString(",$nl") { (k, v) ->
                    val keyEscaped = escapeString(k?.toString() ?: "")
                    val valStr = stringify(v, pretty, indentLevel + 1)
                    "$nextIndent$keyEscaped:$sp$valStr"
                }
                "{$nl$entries$nl$indent}"
            }
            is Iterable<*> -> {
                val list = value.toList()
                if (list.isEmpty()) return "[]"
                val items = list.joinToString(",$nl") { item ->
                    "$nextIndent${stringify(item, pretty, indentLevel + 1)}"
                }
                "[$nl$items$nl$indent}"
                    .let { if (it.endsWith("}")) it.dropLast(1) + "]" else it } // safe bracket
            }
            is Array<*> -> stringify(value.toList(), pretty, indentLevel)
            else -> escapeString(value.toString())
        }
    }

    private fun escapeString(s: String): String {
        val sb = StringBuilder("\"")
        for (ch in s) {
            when (ch) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\b' -> sb.append("\\b")
                '\u000C' -> sb.append("\\f")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> {
                    if (ch < ' ') {
                        sb.append("\\u").append(ch.code.toString(16).padStart(4, '0'))
                    } else {
                        sb.append(ch)
                    }
                }
            }
        }
        sb.append("\"")
        return sb.toString()
    }

    private class JsonParser(private val src: String) {
        private var idx = 0

        fun parseValue(): Any? {
            skipWhitespace()
            if (idx >= src.length) return null

            return when (val ch = src[idx]) {
                '{' -> parseObject()
                '[' -> parseArray()
                '"' -> parseString()
                't', 'f' -> parseBoolean()
                'n' -> parseNull()
                '-', in '0'..'9' -> parseNumber()
                else -> throw IllegalArgumentException("Unexpected character '$ch' at position $idx in JSON")
            }
        }

        private fun parseObject(): Map<String, Any?> {
            expect('{')
            skipWhitespace()
            val map = LinkedHashMap<String, Any?>()
            if (idx < src.length && src[idx] == '}') {
                idx++
                return map
            }

            while (idx < src.length) {
                skipWhitespace()
                val key = parseString()
                skipWhitespace()
                expect(':')
                skipWhitespace()
                val value = parseValue()
                map[key] = value

                skipWhitespace()
                if (idx < src.length && src[idx] == '}') {
                    idx++
                    break
                }
                expect(',')
            }
            return map
        }

        private fun parseArray(): List<Any?> {
            expect('[')
            skipWhitespace()
            val list = ArrayList<Any?>()
            if (idx < src.length && src[idx] == ']') {
                idx++
                return list
            }

            while (idx < src.length) {
                skipWhitespace()
                val value = parseValue()
                list.add(value)

                skipWhitespace()
                if (idx < src.length && src[idx] == ']') {
                    idx++
                    break
                }
                expect(',')
            }
            return list
        }

        private fun parseString(): String {
            expect('"')
            val sb = StringBuilder()
            while (idx < src.length) {
                val ch = src[idx++]
                if (ch == '"') return sb.toString()
                if (ch == '\\') {
                    if (idx >= src.length) break
                    when (val esc = src[idx++]) {
                        '"' -> sb.append('"')
                        '\\' -> sb.append('\\')
                        '/' -> sb.append('/')
                        'b' -> sb.append('\b')
                        'f' -> sb.append('\u000C')
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'u' -> {
                            if (idx + 4 <= src.length) {
                                val hex = src.substring(idx, idx + 4)
                                sb.append(hex.toInt(16).toChar())
                                idx += 4
                            }
                        }
                        else -> sb.append(esc)
                    }
                } else {
                    sb.append(ch)
                }
            }
            return sb.toString()
        }

        private fun parseNumber(): Number {
            val start = idx
            if (src[idx] == '-') idx++
            while (idx < src.length && src[idx] in '0'..'9') idx++
            var isFloatingPoint = false
            if (idx < src.length && src[idx] == '.') {
                isFloatingPoint = true
                idx++
                while (idx < src.length && src[idx] in '0'..'9') idx++
            }
            if (idx < src.length && (src[idx] == 'e' || src[idx] == 'E')) {
                isFloatingPoint = true
                idx++
                if (idx < src.length && (src[idx] == '+' || src[idx] == '-')) idx++
                while (idx < src.length && src[idx] in '0'..'9') idx++
            }

            val numStr = src.substring(start, idx)
            return if (isFloatingPoint) {
                numStr.toDoubleOrNull() ?: 0.0
            } else {
                numStr.toLongOrNull() ?: numStr.toDoubleOrNull() ?: 0L
            }
        }

        private fun parseBoolean(): Boolean {
            return if (src.startsWith("true", idx)) {
                idx += 4
                true
            } else if (src.startsWith("false", idx)) {
                idx += 5
                false
            } else {
                throw IllegalArgumentException("Expected boolean at position $idx")
            }
        }

        private fun parseNull(): Any? {
            return if (src.startsWith("null", idx)) {
                idx += 4
                null
            } else {
                throw IllegalArgumentException("Expected null at position $idx")
            }
        }

        private fun skipWhitespace() {
            while (idx < src.length && src[idx].isWhitespace()) {
                idx++
            }
        }

        private fun expect(expected: Char) {
            if (idx >= src.length || src[idx] != expected) {
                val found = if (idx < src.length) "'${src[idx]}'" else "EOF"
                throw IllegalArgumentException("Expected '$expected' but found $found at position $idx")
            }
            idx++
        }
    }
}

// -------------------------------------------------------------
// Fluent Map and Collection Extensions for untyped JSON handling
// -------------------------------------------------------------

fun Map<String, Any?>.getString(key: String, default: String = ""): String {
    return this[key]?.toString() ?: default
}

fun Map<String, Any?>.getInt(key: String, default: Int = 0): Int {
    val value = this[key] ?: return default
    return when (value) {
        is Number -> value.toInt()
        is String -> value.toIntOrNull() ?: default
        else -> default
    }
}

fun Map<String, Any?>.getLong(key: String, default: Long = 0L): Long {
    val value = this[key] ?: return default
    return when (value) {
        is Number -> value.toLong()
        is String -> value.toLongOrNull() ?: default
        else -> default
    }
}

fun Map<String, Any?>.getDouble(key: String, default: Double = 0.0): Double {
    val value = this[key] ?: return default
    return when (value) {
        is Number -> value.toDouble()
        is String -> value.toDoubleOrNull() ?: default
        else -> default
    }
}

fun Map<String, Any?>.getBoolean(key: String, default: Boolean = false): Boolean {
    val value = this[key] ?: return default
    return when (value) {
        is Boolean -> value
        is String -> value.toBoolean()
        else -> default
    }
}

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.getObject(key: String): Map<String, Any?>? {
    return this[key] as? Map<String, Any?>
}

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.getArray(key: String): List<Any?>? {
    return this[key] as? List<Any?>
}

/**
 * Access nested properties with dot notation e.g. "user.profile.name".
 */
@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.path(dotPath: String): Any? {
    val parts = dotPath.split(".")
    var current: Any? = this
    for (part in parts) {
        if (current is Map<*, *>) {
            current = (current as Map<String, Any?>)[part]
        } else {
            return null
        }
    }
    return current
}

fun Map<String, Any?>.toJsonString(pretty: Boolean = false): String {
    return SimpleJson.stringify(this, pretty = pretty)
}

fun List<Any?>.toJsonString(pretty: Boolean = false): String {
    return SimpleJson.stringify(this, pretty = pretty)
}
