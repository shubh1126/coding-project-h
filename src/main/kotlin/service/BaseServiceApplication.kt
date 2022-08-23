package service
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.google.inject.AbstractModule
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.Module
import com.google.inject.matcher.Matchers
import com.google.inject.name.Names
import io.dropwizard.Configuration
import io.dropwizard.forms.MultiPartBundle
import io.dropwizard.lifecycle.Managed
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import service.modules.ServiceModule
import java.text.SimpleDateFormat

@JsonIgnoreProperties(ignoreUnknown = true)
class BaseServiceConfig : Configuration()

abstract class BaseServiceApplication : DropwizardApplication<BaseServiceConfig>() {
    override fun initializeAdditional(bootstrap: Bootstrap<BaseServiceConfig>) {
        super.initializeAdditional(bootstrap)

        bootstrap.addBundle(MultiPartBundle())

        bootstrap.objectMapper
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .registerModule(JavaTimeModule())
            .also {
                it.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                it.dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
            }
    }

    override fun getGuiceModules(configuration: BaseServiceConfig, environment: Environment): List<Module> {
        return getModules(environment).toList()
    }

    override fun runAdditional(configuration: BaseServiceConfig, environment: Environment) {
        super.runAdditional(configuration, environment)
        run(injector, environment)
    }

    protected abstract fun run(injector: Injector, environment: Environment)

    protected open fun getModules(environment: Environment): Array<Module> {
        val promoModule = DropwizardAwareModule(ServiceModule(), environment)
        return arrayOf(promoModule)
    }

    internal fun <T> getInstance(klass: Class<T>) = injector.getInstance(klass)
    internal fun <T> getInstance(named: String, klass: Class<T>) =
        injector.getInstance(Key.get(klass, Names.named(named)))
}



class DropwizardAwareModule(
    private val module: Module,
    private val environment: Environment)
    : AbstractModule() {
    override fun configure() {
        bindListener(Matchers.any(), GuiceClosableListener(environment::register))
        install(module)
    }
}

fun Environment.register(closeable: AutoCloseable) = apply {
    lifecycle().manage(closeable.toManaged())
}

fun AutoCloseable.toManaged() = object : Managed {
    override fun stop() {
        close()
    }
    override fun start() {
    }
}
