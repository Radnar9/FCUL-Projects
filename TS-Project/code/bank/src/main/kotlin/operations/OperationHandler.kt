package operations

import kotlinx.serialization.json.JsonObject
import models.CommitInfo
import models.ResponseDTO

interface OperationHandler {
    fun processOperation(operation: String, receivedData: JsonObject): CommitInfo
    fun commit(data: JsonObject)
}