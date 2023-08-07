package utils

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*


fun parseStringToJsonObject(str: String) = Json.parseToJsonElement(str).jsonObject

inline fun <reified T>encodeObjToJsonObject(obj: T) = Json.encodeToJsonElement(obj).jsonObject

inline fun <reified T>encodeObjToString(obj: T) = Json.encodeToString(obj)

inline fun <reified T>decodeJsonObjectToObj(obj: JsonObject) = Json.decodeFromJsonElement<T>(obj)

inline fun <reified T>decodeStringToObj(str: String) = Json.decodeFromString<T>(str)
