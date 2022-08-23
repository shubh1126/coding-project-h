package service.modules

import `data-stores`.UserStore
import com.google.inject.AbstractModule
import com.google.inject.Provides
import javax.inject.Named
import javax.inject.Singleton

class ServiceModule : AbstractModule() {
    override fun configure() {
    }

    //Define singletons here

    @Provides
    @Named("userDB")
    @Singleton
    fun getUserDB(): UserStore {
        return UserStore()
    }
}