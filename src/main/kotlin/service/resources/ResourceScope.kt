package service.resources

import kotlinx.coroutines.*
import javax.ws.rs.container.AsyncResponse
import javax.ws.rs.core.Response
import org.slf4j.LoggerFactory

/**
 * scope of resources
 */
abstract class ResourceScope(override val coroutineContext: CoroutineDispatcher = Dispatchers.IO) : CoroutineScope {
    /**
     * async response mapping block
     */
    fun <T : Any?> withAsyncResponse(asyncResponse: AsyncResponse, block: suspend () -> T) {
        asyncResponse.with(block)
    }


    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun <T> AsyncResponse.with(block: suspend () -> T) {
        launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                val response = block()
                this@with.resume(when (response) {
                    is Unit -> Response.noContent().build()
                    null -> Response.status(Response.Status.NOT_FOUND).build()
                    else -> response
                })
            } catch (t: Throwable) {
                logger.warn("Exception while processing request, ", t)
                this@with.resume(t)
            }
        }
    }
}


