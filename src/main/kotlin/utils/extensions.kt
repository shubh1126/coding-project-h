package utils

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import exceptions.ServiceError
import io.dropwizard.jersey.errors.LoggingExceptionMapper
import io.dropwizard.jersey.setup.JerseyEnvironment
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

fun Long.dayOfWeek(): Int = ZonedDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneOffset.UTC).dayOfWeek.value

fun JerseyEnvironment.registerCommonExceptionMappers() {
    this.register(IllegalArgumentExceptionMapper())
    this.register(RuntimeExceptionMapper())
    this.register(InitialisationIllegalArgumentExceptionMapper())
}

fun Long.milliSecondsToMinutes() : Long = TimeUnit.MILLISECONDS.toMinutes(this)
fun Long.minutesToMilliSeconds() : Long = TimeUnit.MINUTES.toMillis(this)


private class IllegalArgumentExceptionMapper : LoggingExceptionMapper<IllegalArgumentException>() {
    override fun toResponse(exception: IllegalArgumentException): Response {
        if (exception is WebApplicationException) {
            return super.toResponse(exception)
        }
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(ServiceError(exception.message?:"Unknown error", exception))
            .type(MediaType.APPLICATION_JSON)
            .build()
    }
}

/**
 * handles invalidArgumentException thrown in init of data models and other initializers
 */
private class InitialisationIllegalArgumentExceptionMapper : LoggingExceptionMapper<InvalidDefinitionException>() {
    override fun toResponse(exception: InvalidDefinitionException): Response {
        if (exception.cause !is IllegalArgumentException) {
            return super.toResponse(exception)
        }
        val id = logException(exception)
        val errorMsg = formatErrorMessage(id, exception)
        return Response.status(Response.Status.BAD_REQUEST)
            .entity(ServiceError(exception.message?:errorMsg, exception))
            .type(MediaType.APPLICATION_JSON)
            .build()
    }
}

private class RuntimeExceptionMapper : LoggingExceptionMapper<RuntimeException>() {
    override fun toResponse(exception: RuntimeException): Response {
        if (exception is WebApplicationException) {
            return super.toResponse(exception)
        }
        val id = logException(exception)
        val errorMsg = formatErrorMessage(id, exception)
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(ServiceError(exception.message?:errorMsg, exception))
            .type(MediaType.APPLICATION_JSON)
            .build()
    }
}

