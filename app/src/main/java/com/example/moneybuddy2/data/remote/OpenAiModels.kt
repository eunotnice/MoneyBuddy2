package com.example.moneybuddy2.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenAiResponseCreateRequest( //request body sent to openAI
    val model: String,
    val input: String, //prompt text
    val store: Boolean = false,
    val text: TextConfig? = null
) {
    @Serializable
    data class TextConfig(
        val format: Format
    )

    @Serializable
    data class Format(
        val type: String, //json_schema
        @SerialName("json_schema") //tell API to return output as JSON matching a schema
        val jsonSchema: JsonSchema
    )

    @Serializable
    data class JsonSchema(
        val name: String,
        val strict: Boolean = true,
        val schema: SchemaObject
    )

    @Serializable
    data class SchemaObject(
        val type: String = "object", //output must be a JSON object
        @SerialName("additionalProperties")
        val additionalProperties: Boolean = false, //no add properties allowed
        val properties: Map<String, SchemaProperty>,
        val required: List<String>
    )

    @Serializable
    data class SchemaProperty(
        val type: List<String>? = null,
        val description: String? = null,
        val items: SchemaItems? = null
    )

    @Serializable
    data class SchemaItems(
        val type: String = "string"
    )
}

@Serializable
data class OpenAiResponseCreateResponse(
    @SerialName("output_text") //map JSON key "output_text" to kotlin property "outputText"
    val outputText: String? = null
)

@Serializable
data class AiReceiptResult(
    val merchant: String,
    val amount: Double? = null,
    val dateIso: String? = null,
    val category: String? = null,
    val merchantCandidates: List<String>? = emptyList()
)

