package victor.android.textify.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class NoteDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: NoteDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.noteDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `base vacia emite lista vacia`() = runTest {
        dao.observeAll().test {
            assertEquals(emptyList<NoteEntity>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `insertar genera id y la nota se puede leer`() = runTest {
        val id = dao.insert(NoteEntity(text = "Hola", createdAt = 100))

        assertTrue(id > 0)
        dao.observeAll().test {
            assertEquals(listOf(NoteEntity(id = id, text = "Hola", createdAt = 100)), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `las notas se leen ordenadas por fecha descendente`() = runTest {
        dao.insert(NoteEntity(text = "media", createdAt = 200))
        dao.insert(NoteEntity(text = "vieja", createdAt = 100))
        dao.insert(NoteEntity(text = "nueva", createdAt = 300))

        dao.observeAll().test {
            assertEquals(listOf("nueva", "media", "vieja"), awaitItem().map { it.text })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `borrar elimina solo la nota indicada`() = runTest {
        val keep = dao.insert(NoteEntity(text = "queda", createdAt = 100))
        val remove = dao.insert(NoteEntity(text = "se va", createdAt = 200))

        val deleted = dao.deleteById(remove)

        assertEquals(1, deleted)
        dao.observeAll().test {
            assertEquals(listOf(keep), awaitItem().map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `borrar un id inexistente no borra nada`() = runTest {
        dao.insert(NoteEntity(text = "queda", createdAt = 100))

        assertEquals(0, dao.deleteById(999))
    }

    @Test
    fun `restaurar reinserta la nota con su id y fecha originales`() = runTest {
        val id = dao.insert(NoteEntity(text = "volvera", createdAt = 100))
        val original = NoteEntity(id = id, text = "volvera", createdAt = 100)
        dao.deleteById(id)

        dao.restore(original)

        dao.observeAll().test {
            assertEquals(listOf(original), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `el flujo emite de nuevo al insertar y borrar`() = runTest {
        dao.observeAll().test {
            assertEquals(emptyList<NoteEntity>(), awaitItem())

            val id = dao.insert(NoteEntity(text = "a", createdAt = 1))
            assertEquals(listOf("a"), awaitItem().map { it.text })

            dao.deleteById(id)
            assertEquals(emptyList<NoteEntity>(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
