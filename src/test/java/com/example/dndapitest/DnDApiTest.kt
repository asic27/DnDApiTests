package com.example.dndapitest

import org.junit.Test
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.junit.Before
import org.junit.jupiter.api.Assertions
import io.qameta.allure.*

@Serializable
data class MonstersData(val index:String,
                        val name: String,
                        val size: String,
                        val type: String,
                        val alignment: String,
                        val hit_points: Int,
                        val hit_dice: String,
                        val hit_points_roll: String){
}
@Serializable
data class RacesData(
    val count:Int,
    val results: List<RaceData>
){}
@Serializable
data class RaceData(
    val index: String,
    val name: String,
    val url: String
){}
@Serializable
data class SpellsData(
    val count:Int,
    val results: List<SpellData>
){}
@Serializable
data class SpellData(
    val index: String,
    val name: String,
    val level: Int,
    val url: String
){}
@Serializable
data class ErrorMessage(
    val error: String,
    val details: List<ErrorDetails>
){}
@Serializable
data class ErrorDetails(
    val origin: String,
    val code: String,
    val format: String,
    val pattern: String,
    val path: List<String>,
    val message: String
){}


private lateinit var client: HttpClient
class DnDApiTest {
    @Before
    fun setup() {
        client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
        }
    }

    @Test
    @Description("Проверка, что метод /api/2014/monsters/:monster_index возвращает информацию о запрошенном существе")
    fun `test get info about existing monster`() = runTest{
        val monster_index = "bugbear"
        val response = client.request {
            url {
                protocol = URLProtocol.HTTPS
                host = "www.dnd5eapi.co"
                Allure.step ("Вызов метода для запроса информации о существе $monster_index")
                path("/api/2014/monsters/$monster_index")
            }
            headers {
                append(HttpHeaders.Accept, "application/json")
            }
            method = HttpMethod.Get
        }.apply {
            Allure.step("Проверить статус ответа - ожидаемый 200")
            Assertions.assertEquals(200, status.value)
            Allure.step("Проверить, что в ответе содержиться JSON")
            Assertions.assertEquals("application/json; charset=utf-8", headers[HttpHeaders.ContentType])
        }

        val monster: MonstersData = response.body()
        Allure.step("Проверить, что в поле name в теле ответа на запрос содержит Bugbear")
        Assertions.assertEquals("Bugbear", monster.name)
        Allure.addAttachment("Bugbear info","application/json",response.body())
    }

    @Test
    @Description("Проверка, что метод /api/2014/monsters/:monster_index возвращает ошибку, если инофрмация о существе отсутствует")
    fun `test get info about non existing monster`() = runTest{
        val monster_index = "toster"
        val response = client.request {
            url {
                protocol = URLProtocol.HTTPS
                host = "www.dnd5eapi.co"
                Allure.step("Вызов метода для запроса информации о существе  $monster_index")
                path("/api/2014/monsters/$monster_index")
            }
            headers {
                append(HttpHeaders.Accept, "application/json")
            }
            method = HttpMethod.Get
        }.apply {
            Allure.step("Проверить статус ответа - ожидаемый 404")
            Assertions.assertEquals(404, status.value)
            Assertions.assertEquals("text/plain; charset=utf-8", headers[HttpHeaders.ContentType])

        }
        Allure.addAttachment("Error message","text/plain",response.body())
    }

    @Test
    @Description("Проверка, что метод /api/2014/races возвращает информацию о всех расах")
    fun `test get info about races`() = runTest{
        val response = client.request {
            url {
                protocol = URLProtocol.HTTPS
                host = "www.dnd5eapi.co"
                Allure.step("Вызов метода для запроса информации о расах в DnD")
                path("/api/2014/races")
            }
            headers {
                append(HttpHeaders.Accept, "application/json")
            }
            method = HttpMethod.Get
        }.apply {
            Allure.step("Проверить статус ответа - ожидаемый 200")
            Assertions.assertEquals(200, status.value)
            Allure.step("Проверить, что в ответе содержиться JSON")
            Assertions.assertEquals("application/json; charset=utf-8", headers[HttpHeaders.ContentType])
        }
        val races: RacesData = response.body()
        Allure.step("Проверить, что в ответе информация о всех расах")
        Assertions.assertEquals(9, races.count)
        Assertions.assertEquals(9,races.results.size)
        Allure.addAttachment("Races info","application/json",response.body())
    }

    @Test
    @Description("Проверка, что метод /api/2014/races/:race_index возвращает информацию о запрошенной расе")
    fun `test get info about race`() = runTest{
        val race_index = "elf"
        val response = client.request {
            url {
                protocol = URLProtocol.HTTPS
                host = "www.dnd5eapi.co"
                Allure.step("Вызов метода для запроса информации о расе $race_index")
                path("/api/2014/races/$race_index")
            }
            headers {
                append(HttpHeaders.Accept, "application/json")
            }
            method = HttpMethod.Get
        }.apply {
            Allure.step("Проверить статус ответа - ожидаемый 200")
            Assertions.assertEquals(200, status.value)
            Allure.step("Проверить, что в ответе содержиться JSON")
            Assertions.assertEquals("application/json; charset=utf-8", headers[HttpHeaders.ContentType])
        }
        val race: RaceData = response.body()
        Allure.step("Проверить, что в ответе информация расе $race_index")
        Assertions.assertEquals("Elf", race.name)
        Allure.addAttachment("Race info","application/json",response.body())
    }

    @Test
    @Description("Проверка, что метод /api/2014/races/:race_index возвращает информацию о запрошенной расе")
    fun `test get info about not existing race`() = runTest{
        val response = client.request {
            val race_index = "tofu"
            url {
                protocol = URLProtocol.HTTPS
                host = "www.dnd5eapi.co"
                Allure.step("Вызов метода для запроса информации о расе $race_index")
                path("/api/2014/races/$race_index")
            }
            headers {
                append(HttpHeaders.Accept, "application/json")
            }
            method = HttpMethod.Get
        }.apply {
            Allure.step("Проверить статус ответа - ожидаемый 404")
            Assertions.assertEquals(404, status.value)
            Assertions.assertEquals("text/plain; charset=utf-8", headers[HttpHeaders.ContentType])
        }
        Allure.addAttachment("Error message","text/plain",response.body())
    }

    @Test
    @Description("Проверка, что метод /api/2014/spells?level=:num_level&school=:school_name возвращает отфильтрованный список заклинаний")
    fun `test get info about spells`() = runTest{
        Allure.step("Задаём в фильтр несколько уровней, как указано в документации на API https://5e-bits.github.io/docs/api/get-list-of-spells-with-optional-filtering")
        val num_level = "1,2"
        val school_name = "evocation"
        val response = client.request {
            url {
                protocol = URLProtocol.HTTPS
                host = "www.dnd5eapi.co"
                Allure.step("Вызов метода для запроса списка заклинаний уровня(ей) $num_level школ(ы) $school_name")
                path("/api/2014/spells")
                parameters.append("level", num_level)
                parameters.append("school",school_name)
            }
            headers {
                append(HttpHeaders.Accept, "application/json")
            }
            method = HttpMethod.Get
        }.apply {
            Allure.step("Проверить статус ответа - ожидаемый 200")
            Assertions.assertEquals(200, status.value)
            Allure.step("Проверить, что в ответе содержиться JSON")
            Assertions.assertEquals("application/json; charset=utf-8", headers[HttpHeaders.ContentType])
        }
        val spells: SpellsData = response.body()
        Allure.step("Проверить, что в ответе список заклинаний $num_level школ(ы) $school_name")
        Assertions.assertEquals(20, spells.count)
        Assertions.assertEquals(20,spells.results.size)
        for(spell in spells.results){
            Assertions.assertTrue(spell.level==1 || spell.level==2)
        }
        Allure.addAttachment("Spells info","application/json",response.body())
    }

    @Test
    @Description("Проверка, что метод /api/2014/spells?level=:num_level&school=:school_name возвращает отфильтрованный список заклинаний")
    fun `test get info about spells with bad filter's options`() = runTest{
        Allure.step("Задаём в фильтр по уровню не число")
        val num_level = "abc"
        val school_name = "evocation"
        val response = client.request {
            url {
                protocol = URLProtocol.HTTPS
                host = "www.dnd5eapi.co"
                Allure.step("Вызов метода для запроса списка заклинаний уровня(ей) $num_level школ(ы) $school_name")
                path("/api/2014/spells")
                parameters.append("level", num_level)
                parameters.append("school",school_name)
            }
            headers {
                append(HttpHeaders.Accept, "application/json")
            }
            method = HttpMethod.Get
        }.apply {
            Allure.step("Проверить статус ответа - ожидаемый 400")
            Assertions.assertEquals(400, status.value)
            Allure.step("Проверить, что в ответе содержиться JSON")
            Assertions.assertEquals("application/json; charset=utf-8", headers[HttpHeaders.ContentType])
        }
        val err_msg: ErrorMessage = response.body()
        Allure.step("Проверить, что в ответе информация об ошибке формата запроса")
        Assertions.assertEquals("Invalid query parameters", err_msg.error)
        Allure.addAttachment("Error message","application/json",response.body())
    }
    @Test
    @Description("Проверка, что метод /api/2014/spells?level=:num_level&school=:school_name возвращает отфильтрованный список заклинаний")
    fun `test get info about spells not API documentation style`() = runTest{
        Allure.step("Задаём в фильтр несколько уровней, для каждого уровня свой параметр, что противоречит документации на API https://5e-bits.github.io/docs/api/get-list-of-spells-with-optional-filtering")
        val num_level_1 = "1"
        val num_level_2 = "2"
        val school_name = "evocation"
        val response = client.request {
            url {
                protocol = URLProtocol.HTTPS
                host = "www.dnd5eapi.co"
                Allure.step("Вызов метода для запроса списка заклинаний уровня(ей) $num_level_1,$num_level_2 школ(ы) $school_name")
                path("/api/2014/spells")
                parameters.append("level", num_level_1)
                parameters.append("level", num_level_2)
                parameters.append("school",school_name)
            }
            headers {
                append(HttpHeaders.Accept, "application/json")
            }
            method = HttpMethod.Get
        }.apply {
            Allure.step("Проверить статус ответа - ожидаемый 200")
            Assertions.assertEquals(200, status.value)
            Allure.step("Проверить, что в ответе содержиться JSON")
            Assertions.assertEquals("application/json; charset=utf-8", headers[HttpHeaders.ContentType])
        }
        val spells: SpellsData = response.body()
        Allure.step("Проверить, что в ответе список заклинаний $num_level_1,$num_level_2 школ(ы) $school_name")
        Assertions.assertEquals(20, spells.count)
        Assertions.assertEquals(20,spells.results.size)
        for(spell in spells.results){
            Assertions.assertTrue(spell.level==1 || spell.level==2)
        }
        Allure.addAttachment("Spells info","application/json",response.body())
    }

}