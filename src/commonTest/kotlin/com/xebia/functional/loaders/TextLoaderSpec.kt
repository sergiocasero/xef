package com.xebia.functional.loaders

import com.xebia.functional.domain.Document
import com.xebia.functional.textsplitters.CharacterTextSplitter
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem

class TextLoaderSpec : StringSpec({
    "should return a list of documents with the contents of each line of the specified file" {
        val fileSystem = FakeFileSystem().apply {
            val templates = "templates".toPath()
            createDirectory(templates)
            val example = templates / "example.txt"
            write(example) {
                writeUtf8(
                    """
                        |Lorem ipsum dolor sit amet, consectetur adipiscing elit.
                        |Sed do eiusmod tempor incididunt, ut labore et dolore magna aliqua.
                    """.trimMargin()
                )
            }
        }
        val textLoader = TextLoader("templates/example.txt".toPath(), fileSystem)
        val documentList = textLoader.load()

        documentList shouldBe listOf(
            Document("Lorem ipsum dolor sit amet, consectetur adipiscing elit."),
            Document("Sed do eiusmod tempor incididunt, ut labore et dolore magna aliqua.")
        )
    }

    "should return a list of documents with the contents of each trim of the specified file" {
        val fileSystem = FakeFileSystem().apply {
            val templates = "templates".toPath()
            createDirectory(templates)
            val example = templates / "example.txt"
            write(example) {
                writeUtf8(
                    """
                        |Lorem ipsum dolor sit amet, consectetur adipiscing elit.
                        |Sed do eiusmod tempor incididunt, ut labore et dolore magna aliqua.
                    """.trimMargin()
                )
            }
        }
        val textLoader = TextLoader("templates/example.txt".toPath(), fileSystem)
        val textSplitter = CharacterTextSplitter(", ")
        val documentList = textLoader.loadAndSplit(textSplitter)

        documentList shouldBe listOf(
            Document("Lorem ipsum dolor sit amet"),
            Document("consectetur adipiscing elit."),
            Document("Sed do eiusmod tempor incididunt"),
            Document("ut labore et dolore magna aliqua.")
        )
    }
})
