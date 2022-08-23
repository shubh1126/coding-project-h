package service


import com.codahale.metrics.health.HealthCheck
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.inject.*
import com.google.inject.matcher.Matchers
import com.google.inject.spi.BindingScopingVisitor
import com.google.inject.spi.ProvisionListener
import io.dropwizard.Application
import io.dropwizard.Bundle
import io.dropwizard.Configuration
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper
import io.dropwizard.lifecycle.Managed
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.swagger.v3.jaxrs2.integration.resources.AcceptHeaderOpenApiResource
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.File
import java.lang.IllegalArgumentException
import javax.inject.Singleton

/**
 * Base class
 */
@Suppress("unused")
abstract class DropwizardApplication<T : Configuration> : Application<T>() {
    /**
     * Override this to provide additional guice modules to register
     */
    protected open fun getGuiceModules(configuration: T, environment: Environment): List<Module> = emptyList()

    /**
     * Override this to provide additional Jackson modules to register
     */
    protected open fun getJacksonModules(): List<com.fasterxml.jackson.databind.Module> = emptyList()

    /**
     * Override this to provide additional dropwizard bundles to register
     */
    protected open fun getDropwizardBundles(): List<Bundle> = emptyList()

    /**
     * Override this to specify dropwizard resource classes. Instances will be sourced via {@see injector}
     */
    protected open fun getResourceClasses(): List<Class<*>> = emptyList()

    /**
     * Get list of additional healthchecks to register
     */
    protected open fun getHealthchecks(): List<Pair<String, HealthCheck>> = emptyList()

    /**
     * Override this method to call any additional steps during bootstrap initialize
     */
    protected open fun initializeAdditional(bootstrap: Bootstrap<T>) {}

    /**
     * Override this to call any additional steps at the end of bootstrap run() method
     */
    protected open fun runAdditional(configuration: T, environment: Environment) {}

    /**
     * Initialize bootstrap. You cannot override this. Use {@see initializeAdditional} for additional steps
     */
    final override fun initialize(bootstrap: Bootstrap<T>) {
        //disabling fail on unknown properties
        bootstrap.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

        // Register jackson modules
        bootstrap.objectMapper.registerKotlinModule()
        bootstrap.objectMapper.registerModules(getJacksonModules())

        // Call any additional initialization steps
        initializeAdditional(bootstrap)
    }

    /**
     * Start server. You cannot override this. To call any additional steps, {@see runAdditional}
     */
    final override fun run(configuration: T, environment: Environment) {
        // Create Guice injector
        _injector = Guice.createInjector(LifecycleAwareModule(object : AbstractModule() {
            override fun configure() {
                getGuiceModules(configuration, environment).forEach { install(it) }
            }
        }))
        // Register hooks to register and unregister collectors with the registry at the right time
        environment.lifecycle().manage(object : Managed {
            // Register all the collectors when server is starting
            override fun start() {
                logger.info("Starting application")
            }

            // Remove all the collectors when server is stopping
            override fun stop() {
                logger.info("closing application")
            }
        })

        // Register resource classes
        getResourceClasses().forEach { environment.jersey().register(injector.getInstance(it)) }
            //environment.jersey().register(OpenApiResource())
//            environment.jersey().register(AcceptHeaderOpenApiResource())
            environment.jersey().register(JsonProcessingExceptionMapper(true))

        // Call any additional steps
        runAdditional(configuration, environment)
    }

    /**
     * Start the service using the provided application object
     */
    @Suppress("unused")
    fun startWithArgs(args: Array<String>) {
        if (args.isNotEmpty()) {
            run(*args)
        } else {
            val configFile = File.createTempFile("calendly-service", ".yml")

            // Write a temp configuration file, after having eliminated the dcos section
            configFile.writeText(
                this.javaClass.getResourceAsStream("/server-config.yml").bufferedReader()
                    .readText()
            )
            configFile.deleteOnExit()

            // Run the server on this new config
            run("server", configFile.absolutePath)
        }
    }

    private lateinit var _injector: Injector

    @Suppress("WeakerAccess")
    val injector: Injector
        get() = _injector
    protected val logger: Logger by lazy { LoggerFactory.getLogger(name) }
}

class LifeCycleObjectRepo private constructor() : Closeable {
    companion object {
        private val logger = LoggerFactory.getLogger(LifeCycleObjectRepo::class.java)

        private val global by lazy { LifeCycleObjectRepo() }
        fun global() = global

        init {
            Runtime.getRuntime().addShutdownHook(Thread {
                global().close()
            })
        }
    }

    private val closeableObjects: MutableList<SequencedAutoCloseable> = mutableListOf()

    fun register(closeable: AutoCloseable): LifeCycleObjectRepo {
        register(closeable, Short.MAX_VALUE)
        return this
    }

    fun register(closeable: AutoCloseable, sequenceNumber: Short): LifeCycleObjectRepo {
        if (closeable == global) {
            return this
        }
        if (sequenceNumber < 0) {
            throw IllegalArgumentException("sequenceNumber is negative.")
        }
        if (!closeableObjects.contains(closeable) && closeableObjects.add(
                SequencedAutoCloseable(
                    sequenceNumber,
                    closeable
                )
            )
        ) {
            logger.info("Register {} for close at shutdown", closeable)
        }
        return this
    }

    @Synchronized
    override fun close() {
        closeableObjects.sortBy { it.sequenceNumber }
        closeableObjects.forEach { c ->
            try {
                logger.info("Closing {}", c)
                c.close()
            } catch (e: Exception) {
                logger.error("Error closing object", e)
            }
        }
        closeableObjects.clear()
    }

    private data class SequencedAutoCloseable(val sequenceNumber: Short, val closeable: AutoCloseable) : AutoCloseable {
        override fun close() {
            closeable.close()
        }

        override fun toString(): String = "SequencedAutoCloseable [${closeable}]"
    }
}


class LifecycleAwareModule(
    private val module: Module,
    private val lcObjectRepo: LifeCycleObjectRepo = LifeCycleObjectRepo.global()
) : AbstractModule() {
    override fun configure() {
        bindListener(Matchers.any(), GuiceClosableListener(lcObjectRepo::register))
        install(module)
    }
}


class GuiceClosableListener(private val consumer: (AutoCloseable) -> Any) : ProvisionListener {
    override fun <T> onProvision(provisionInvocation: ProvisionListener.ProvisionInvocation<T>) {
        val provision = provisionInvocation.provision()
        if (provision is AutoCloseable && shouldManage(provisionInvocation)) {
            consumer(provision as AutoCloseable)
        }
    }

    private fun shouldManage(provisionInvocation: ProvisionListener.ProvisionInvocation<*>): Boolean {
        return provisionInvocation.binding.acceptScopingVisitor(object : BindingScopingVisitor<Boolean> {
            override fun visitEagerSingleton(): Boolean? {
                return true
            }

            override fun visitScope(scope: Scope): Boolean? {
                return scope === Scopes.SINGLETON
            }

            override fun visitScopeAnnotation(scopeAnnotation: Class<out Annotation>): Boolean? {
                return scopeAnnotation.isAssignableFrom(Singleton::class.java)
            }

            override fun visitNoScoping(): Boolean? {
                return false
            }
        })
    }
}
