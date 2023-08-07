package operations

import kotlinx.serialization.json.JsonObject
import models.ResponseDTO

interface OperationHandler {
    fun processOperation(receivedData: JsonObject): ResponseDTO
}