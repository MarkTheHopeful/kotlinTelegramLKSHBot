package table

import java.io.BufferedReader
import java.io.FileReader

class ParallelTable(rawTableInput: String) {
    val contests: MutableList<Contest> = mutableListOf()
    val problems: MutableList<Problem> = mutableListOf()
    val namesToEjids: MutableMap<String, Int> = mutableMapOf()
    val persons: MutableMap<Int, Person> = mutableMapOf()   //TODO: ejudge id?

    private fun parseContests(rawInput: String): MutableList<Contest> {
        val result = mutableListOf<Contest>()
        val lines = rawInput.split("td>")
        var id = 0
        for (i in 0 until lines.size) {
            if ("class=\"contest\"" in lines[i]) {
                val name = lines[i].substringAfter("title=\"").substringBefore("\">")
                result.add(Contest(id, name))
                id++
            }
        }
        return result
    }

    private fun parseProblems(rawInput: String): MutableList<Problem> {
        val result = mutableListOf<Problem>()
        val lines = rawInput.split("td>")
        var id = 0
        var contest_id = -1
        for (i in 0 until lines.size) {
            if ("title" !in lines[i]) continue
            val name = lines[i].substringAfter("title=\"").substringBefore("\"")
            if (name[0] == 'A') contest_id++
            result.add(Problem(id++, name, contest_id, 0))
        }
        return result
    }

    private fun parsePerson(rawInput: String): Person {
        val id =
            Integer.parseInt(rawInput.substringBefore("\'>").substringAfter("\'")) //TODO: guess there is another way to parse an Integer in Kotlin

        val nameString = rawInput.substringBefore("</nobr>").substringAfter("<nobr>")
        val person = Person(id, nameString.substringBefore(" "), nameString.substringAfter(" "))
        val information = rawInput.split("</td>")
        for (i in 5 until information.size - 2) {
            when (information[i].substringAfter("class=\"").substringBefore("\"")) {
                "ac" -> {
                    this.problems[i - 5].times_solved++
                    person.solvedProblems.add(this.problems[i - 5])
                }
                "pending" -> {
                    this.problems[i - 5].times_solved++
                    person.pendingReview.add(this.problems[i - 5])
                }
                "rj" -> {
                    this.problems[i - 5].times_solved++
                    person.rejectedProblems.add(this.problems[i - 5])
                }
                "wa" -> person.triedProblems.add(this.problems[i - 5])
                "dq" -> person.bannedProblems.add(this.problems[i - 5])
            }
        }
        return person
    }

    private fun parsePersonNewTable(rawInput: String): Person {
        val id = Integer.parseInt(rawInput.substringBefore("</td>").substringAfter("\"rank\">"))

        val nameString = rawInput.substringBefore("</td><td class=\"solved\">").substringAfter("class=\"name\">")
        val person = Person(id, nameString.substringBefore(" "), nameString.substringAfter(" "))
        val information = rawInput.split("</td>")
        for (i in 4 until information.size - 1) {
            when (information[i].substringBefore("\" title=").substringAfter("class=\"")) {
                "verdict OK" -> {
                    this.problems[i - 4].times_solved++
                    person.solvedProblems.add(this.problems[i - 4])

                }
                "verdict PR" -> {
                    this.problems[i - 4].times_solved++
                    person.pendingReview.add(this.problems[i - 4])
                }
                "verdict TL", "verdict WA", "verdict RT", "verdict ML" -> {
                    person.triedProblems.add(this.problems[i - 4])
                }
                "verdict NO" -> {

                }
                "verdict DQ" -> {
                    println("F...")
                }
                "verdict RJ" -> {
                    this.problems[i - 4].times_solved++
                    person.rejectedProblems.add(this.problems[i - 4])
                }
                else -> {
                    throw Exception("Unknown verdict: ${information[i].substringBefore("\" title=").substringAfter("class=\"")}")
                }
            }
        }
        return person
    }

    private fun parsePersons(rawInput: String): MutableList<Person> {
        val result = mutableListOf<Person>()
        val splitInput = rawInput.split("</tr>")
        for (portion in splitInput) {
            if ("<tr ejid=" in portion) {
                result.add(parsePerson(portion))
            } else if ("class=\"rank\"" in portion) {
                result.add(parsePersonNewTable(portion))
            }
        }
        return result
    }

    init {
        var rawInput = rawTableInput.substringAfter("<tr>")

        val contestsParsed = parseContests(rawInput.substringBefore("</tr>"))
        this.contests.addAll(contestsParsed)
        rawInput = rawInput.substringAfter("</tr>")

        val problemsParsed = parseProblems(rawInput.substringBefore("</tr>"))
        this.problems.addAll(problemsParsed)
        for (problem in problemsParsed) {
            this.contests[problem.contest_id].addProblem(problem)
        }
        rawInput = rawInput.substringAfter("</tr>")

        val personsParsed = parsePersons(rawInput)
        for (person in personsParsed) {
            this.namesToEjids[person.name] = person.id
            this.persons[person.id] = person
        }
    }

    constructor(filename: String, isFile: Boolean) : this(joinStringArray(BufferedReader(FileReader(filename)).readLines()))

    @Synchronized
    fun getPersonByName(name: String): Person {
        if (this.namesToEjids[name] == null) {
            throw Exception("No such user")
        }
        val id: Int = this.namesToEjids[name]!!
        if (this.persons[this.namesToEjids[name]] == null) {
            throw Error("Fatal error. Вырубай компуктер.")
        } else {
            return this.persons[this.namesToEjids[name]]!!
        }
    }
}