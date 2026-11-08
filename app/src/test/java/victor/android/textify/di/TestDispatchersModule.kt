package victor.android.textify.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import javax.inject.Singleton

/** Reemplaza [DispatchersModule] en los tests de Hilt por un único dispatcher de prueba. */
@OptIn(ExperimentalCoroutinesApi::class)
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DispatchersModule::class],
)
object TestDispatchersModule {

    @Provides
    @Singleton
    fun provideTestDispatcher(): TestDispatcherHolder =
        TestDispatcherHolder(UnconfinedTestDispatcher())

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(holder: TestDispatcherHolder): CoroutineDispatcher = holder.dispatcher

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(holder: TestDispatcherHolder): CoroutineDispatcher =
        holder.dispatcher
}

class TestDispatcherHolder(val dispatcher: CoroutineDispatcher)
