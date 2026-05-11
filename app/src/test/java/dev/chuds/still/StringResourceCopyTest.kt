package dev.chuds.still

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertFalse
import org.junit.Test

class StringResourceCopyTest {
    @Test
    fun user_facing_string_resources_do_not_contain_uppercase_letters() {
        val stringsFile = listOf(
            File("src/main/res/values/strings.xml"),
            File("app/src/main/res/values/strings.xml"),
        ).first { it.isFile }
        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(stringsFile)
        val strings = document.getElementsByTagName("string")

        for (index in 0 until strings.length) {
            val node = strings.item(index)
            val name = node.attributes.getNamedItem("name")?.nodeValue.orEmpty()
            if (name == "app_name") continue

            val value = node.textContent.orEmpty()
            assertFalse(
                "$name contains uppercase copy: $value",
                value.any { it.isLetter() && it.isUpperCase() },
            )
        }
    }
}
