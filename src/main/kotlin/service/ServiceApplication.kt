package service

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.google.inject.Injector
import exceptions.ServiceError
import io.dropwizard.jersey.errors.LoggingExceptionMapper
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper
import io.dropwizard.jersey.setup.JerseyEnvironment
import io.dropwizard.setup.Environment
import service.resources.UserPreferenceResource
import service.resources.UserResource
import utils.registerCommonExceptionMappers
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class ServiceApplication : BaseServiceApplication() {
    override fun run(injector: Injector, environment: Environment) {
        environment.jersey().registerResources()
        environment.jersey().registerExceptionMappers()
    }

    /**
     * Register all your resources here
     */
    private fun JerseyEnvironment.registerResources(){
        //register controllers here
        register(getInstance(UserResource::class.java))
        register(getInstance(UserPreferenceResource::class.java))

    }

    /**
     * register exception mappers here
     */
    private fun JerseyEnvironment.registerExceptionMappers() {
         register(JsonProcessingExceptionMapper(true))
         registerCommonExceptionMappers()
         register(InvalidDefinitionExceptionMapper())

    }

    /**
     * Adding mapper for init checks
     */
    private class InvalidDefinitionExceptionMapper : LoggingExceptionMapper<InvalidDefinitionException>() {
        override fun toResponse(exception: InvalidDefinitionException): Response {
            val id = logException(exception)
            val errorMsg = formatErrorMessage(id, exception)
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ServiceError(errorMsg, exception))
                .type(MediaType.APPLICATION_JSON)
                .build()
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ServiceApplication().startWithArgs(args)
        }
    }
}
