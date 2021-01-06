import table.joinProblemArray
import okhttp3.OkHttpClient
import okhttp3.Request
import table.ParallelTable
import java.io.IOException

class TableManager(chatId: Long) {
    var table: ParallelTable? = null
    private val client = OkHttpClient()

    fun updateTable(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val responseBody = response.body()!!.string()
            this.table = ParallelTable(responseBody)
        }
    }

    fun getContestsInformation(): String {
        var result = ""
        for (contest in table?.contests ?: return "Table is not initialized or something is wrong") {
            result += "$contest contains ${contest.container.size} problems\n"
        }
        return result
    }

    fun getAllInformation(username: String): String {
        try {
            val person = table?.getPersonByName(username) ?: return "Table is not initialized or something is wrong"
            val listed = person.getOrderProblems(table?.problems ?: return "Table is not initialized or something is wrong")
            return joinProblemArray(listed)
        } catch (e: Exception){
            return e.toString()
        }
    }

    fun getMagicExamNumber(): String {
        return this.table?.countSuperDumbnessGlobal().toString() ?: return "Table is not initialized or something is wrong"
    }

    fun getPersonalSolveList(person: String, howMuch: Int): String {
        val raw = this.table?.getPersonalMergers(person, howMuch) ?: return "Table is not initialized or something is wrong"
        var result = ""
        for (e in raw) {
            result += "${e.first} points will give the task ${this.table!!.problems[e.second.second]}\n"
        }
        return result
    }

    fun getTheSolveList(howMuch: Int): String {
        val raw = this.table?.getBestMergers(howMuch) ?: return "Table is not initialized or something is wrong"
        var result = ""
        for (e in raw) {
            result += "${e.first} points will give the task ${this.table!!.problems[e.second.second]} by person ${this.table!!.persons[e.second.first + 1]}\n"
        }
        return result
    }

}