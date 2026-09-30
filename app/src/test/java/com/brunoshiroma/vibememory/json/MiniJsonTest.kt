package com.brunoshiroma.vibememory.json

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MiniJsonTest {

    @Test
    fun `parses flat object with string and number fields`() {
        val value = MiniJson.parse(
            """{"operation":"read","sizeBytes":64,"elapsedSeconds":0.5}""",
        ) as JsonValue.JsonObject

        assertEquals(JsonValue.JsonString("read"), value.fields["operation"])
        assertEquals(JsonValue.JsonNumber(64.0), value.fields["sizeBytes"])
        assertEquals(JsonValue.JsonNumber(0.5), value.fields["elapsedSeconds"])
    }

    @Test
    fun `parses array of objects`() {
        val value = MiniJson.parse(
            """[{"a":1},{"a":2}]""",
        ) as JsonValue.JsonArray

        assertEquals(2, value.items.size)
        val first = value.items[0] as JsonValue.JsonObject
        assertEquals(JsonValue.JsonNumber(1.0), first.fields["a"])
    }

    @Test
    fun `parses empty array and object`() {
        assertEquals(JsonValue.JsonArray(emptyList()), MiniJson.parse("[]"))
        assertEquals(JsonValue.JsonObject(emptyMap()), MiniJson.parse("{}"))
    }

    @Test
    fun `parses negative and scientific numbers`() {
        assertEquals(JsonValue.JsonNumber(-12.5), MiniJson.parse("-12.5"))
        assertEquals(JsonValue.JsonNumber(1.5e3), MiniJson.parse("1.5e3"))
    }

    @Test
    fun `parses escaped strings`() {
        val parsed = MiniJson.parse(""""line1\nline2\t\"quoted\""""") as JsonValue.JsonString
        assertEquals("line1\nline2\t\"quoted\"", parsed.value)
    }

    @Test
    fun `parses booleans and null`() {
        assertEquals(JsonValue.JsonBool(true), MiniJson.parse("true"))
        assertEquals(JsonValue.JsonBool(false), MiniJson.parse("false"))
        assertEquals(JsonValue.JsonNull, MiniJson.parse("null"))
    }

    @Test(expected = JsonParseException::class)
    fun `rejects trailing content`() {
        MiniJson.parse("{} extra")
    }

    @Test(expected = JsonParseException::class)
    fun `rejects malformed input`() {
        MiniJson.parse("{\"a\":}")
    }

    @Test
    fun `sanity checks equals behavior used above`() {
        assertTrue(JsonValue.JsonNumber(1.0) == JsonValue.JsonNumber(1.0))
        assertFalse(JsonValue.JsonNumber(1.0) == JsonValue.JsonNumber(2.0))
    }
}
