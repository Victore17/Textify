package victor.android.textify.di

import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import victor.android.textify.data.local.AppDatabase
import victor.android.textify.data.local.NoteDao
import victor.android.textify.data.repository.NotesRepositoryImpl
import victor.android.textify.domain.repository.NotesRepository
import java.time.Clock
import javax.inject.Singleton

/** Provee la base de datos Room, sus DAOs y el reloj del sistema. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME).build()

    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao = database.noteDao()

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemUTC()
}

/** Enlaza las interfaces de repositorio del dominio con sus implementaciones. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNotesRepository(impl: NotesRepositoryImpl): NotesRepository
}
