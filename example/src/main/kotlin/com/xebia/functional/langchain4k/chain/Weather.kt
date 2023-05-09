package com.xebia.functional.langchain4k.chain

import arrow.core.getOrElse
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.raise.recover
import arrow.fx.coroutines.resourceScope
import com.xebia.functional.Document
import com.xebia.functional.chains.VectorQAChain
import com.xebia.functional.embeddings.OpenAIEmbeddings
import com.xebia.functional.env.OpenAIConfig
import com.xebia.functional.llm.openai.KtorOpenAIClient
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.loaders.BaseLoader
import com.xebia.functional.loaders.TextLoader
import com.xebia.functional.vectorstores.LocalVectorStore
import io.github.oshai.KLogger
import io.github.oshai.KotlinLogging
import okio.Path
import okio.Path.Companion.toPath
import java.io.File
import java.net.URL
import kotlin.time.ExperimentalTime

data class WeatherExampleError(val reason: String)

suspend fun main() {
    val logger = KotlinLogging.logger("Weather")

    val documentPath = "/documents/weather.csv"
    val question = "Knowing this forecast, what clothes do you recommend I should wear?"
    val answer: Map<String, String> = getQuestionAnswer(documentPath, question, logger)

    logger.info { answer }
}

@OptIn(ExperimentalTime::class)
private suspend fun getQuestionAnswer(
    documentPath: String,
    question: String,
    logger: KLogger
): Map<String, String> =
    resourceScope {
        either {
            val token = "<YOUR_OPENAI_TOKEN>"
            val openAIConfig = recover({
                OpenAIConfig(token)
            }) { raise(WeatherExampleError(it.joinToString(", "))) }

            val openAiClient: OpenAIClient = KtorOpenAIClient(openAIConfig)
            val embeddings = OpenAIEmbeddings(openAIConfig, openAiClient, logger)
            val vectorStore = LocalVectorStore(embeddings)

            val resource: URL = ensureNotNull(javaClass.getResource(documentPath)) {
                WeatherExampleError("Resource not found")
            }

            val path: Path = File(resource.file).path.toPath()

            val textLoader: BaseLoader = TextLoader(path)
            val docs: List<Document> = textLoader.load()
            vectorStore.addDocuments(docs)

            val numOfDocs = 10
            val outputVariable = "answer"
            val chain = VectorQAChain(
                openAiClient,
                vectorStore,
                numOfDocs,
                outputVariable
            )

            chain.run(question).getOrElse { raise(WeatherExampleError(it.reason)) }

        }.getOrElse { throw IllegalStateException(it.reason) }
    }