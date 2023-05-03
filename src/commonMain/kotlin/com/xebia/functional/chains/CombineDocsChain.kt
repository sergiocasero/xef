package com.xebia.functional.chains

import arrow.core.Either
import com.xebia.functional.Document
import com.xebia.functional.llm.openai.OpenAIClient
import com.xebia.functional.prompt.PromptTemplate

interface CombineDocsChain : Chain {
    suspend fun combine(documents: List<Document>): Map<String, String>
}

@Suppress("LongParameterList")
suspend fun CombineDocsChain(
    llm: OpenAIClient,
    promptTemplate: PromptTemplate,
    documents: List<Document>,
    documentVariableName: String,
    outputVariable: String,
    chainOutput: Chain.ChainOutput = Chain.ChainOutput.OnlyOutput
): CombineDocsChain = object : CombineDocsChain {

    private val inputKeys: Set<String> = promptTemplate.inputKeys.toSet() - setOf(documentVariableName)
    private val outputKeys: Set<String> = setOf("answer")

    override val config: Chain.Config = Chain.Config(inputKeys, outputKeys, chainOutput)

    override suspend fun combine(documents: List<Document>): Map<String, String> {
        val mergedDocs = documents.joinToString("\n") { it.content }
        return mapOf(documentVariableName to mergedDocs)
    }

    override suspend fun call(inputs: Map<String, String>): Either<Chain.Error, Map<String, String>> {
        val llmChain = LLMChain(
            llm,
            promptTemplate,
            outputVariable = outputVariable,
            chainOutput = chainOutput
        )

        val totalInputs = combine(documents) + inputs
        return llmChain.run(totalInputs)
    }
}