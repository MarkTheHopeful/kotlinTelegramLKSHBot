package table

import java.io.BufferedReader
import java.io.FileReader

class ParallelTable(rawTableInput: String) {
    val contests: MutableList<Contest> = mutableListOf()
    val problems: MutableList<Problem> = mutableListOf()
    val namesToEjids: MutableMap<String, Int> = mutableMapOf()
    val persons: MutableMap<Int, Person> = mutableMapOf()   //TODO: ejudge id?
    val justTable: MutableList<MutableList<Int>> = mutableListOf()
    val used = mutableListOf<MutableList<Boolean>>()
    val dsu = mutableListOf<Int>()
    val szsz = mutableListOf<Int>()

    private fun parseContests(rawInput: String): MutableList<Contest> {
        val result = mutableListOf<Contest>()
        val lines = rawInput.split("td>")
        var id = 0
        for (i in 0 until lines.size) {
            if ("class=\"contest\"" in lines[i]) {
                val name = lines[i].substringAfter("title=\"").substringBefore("\">")
                val problemsCount = Integer.parseInt(lines[i].substringBefore("\" class").substringAfter("colspan=\""))
                result.add(Contest(id, name, problemsCount))
                id++
            }
        }
        return result
    }

    private fun parseProblems(rawInput: String): MutableList<Problem> {
        val result = mutableListOf<Problem>()
        val lines = rawInput.split("td>")
        var id = 0
        var contestId = 0
        var currentContestProblemsAdded = 0
        for (i in lines.indices) {
            if ("title" !in lines[i]) continue
            val name = lines[i].substringAfter("title=\"").substringBefore("\"")
            if (currentContestProblemsAdded == this.contests[contestId].size) {
                contestId++
                currentContestProblemsAdded = 0
            }
            result.add(Problem(id++, name, contestId, 0))
            currentContestProblemsAdded++
        }
        return result
    }

    private fun parsePerson(rawInput: String): Person {
        val id =
            Integer.parseInt(
                rawInput.substringBefore("\'>").substringAfter("\'")
            ) //TODO: guess there is another way to parse an Integer in Kotlin

        val nameString = rawInput.substringBefore("</nobr>").substringAfter("<nobr>")
        val person = Person(id, nameString.substringBefore(" "), nameString.substringAfter(" "))
        val information = rawInput.split("</td>")
        var currentLine: MutableList<Int> = mutableListOf()
        for (i in 5 until information.size - 2) {
            when (information[i].substringAfter("class=\"").substringBefore("\"")) {
                "ac" -> {
                    currentLine.add(1)
                    this.problems[i - 5].times_solved++
                    person.solvedProblems.add(this.problems[i - 5])
                }
                "pending" -> {
                    currentLine.add(0)
                    this.problems[i - 5].times_solved++
                    person.pendingReview.add(this.problems[i - 5])
                }
                "rj" -> {
                    currentLine.add(0)
                    this.problems[i - 5].times_solved++
                    person.rejectedProblems.add(this.problems[i - 5])
                }
                "wa" -> {
                    currentLine.add(0)
                    person.triedProblems.add(this.problems[i - 5])
                }
                "dq" -> {
                    currentLine.add(0)
                    person.bannedProblems.add(this.problems[i - 5])
                }
                else -> {
                    currentLine.add(0)
                }
            }
        }
        this.justTable.add(currentLine)
        return person
    }

    private fun parsePersonNewTable(rawInput: String): Person {
        val id = Integer.parseInt(rawInput.substringBefore("</td>").substringAfter("\"rank\">"))

        val nameString = rawInput.substringBefore("</td><td class=\"solved\">").substringAfter("class=\"name\">")
        val person = Person(id, nameString.substringBefore(" "), nameString.substringAfter(" "))
        val information = rawInput.split("</td>")
        var currentLine: MutableList<Int> = mutableListOf()
        for (i in 4 until information.size - 1) {
            when (information[i].substringBefore("\" title=").substringAfter("class=\"")) {
                "verdict OK" -> {
                    currentLine.add(1)
                    this.problems[i - 4].times_solved++
                    person.solvedProblems.add(this.problems[i - 4])

                }
                "verdict PR" -> {
                    currentLine.add(0)
                    this.problems[i - 4].times_solved++
                    person.pendingReview.add(this.problems[i - 4])
                }
                "verdict TL", "verdict WA", "verdict RT", "verdict ML", "verdict PE", "verdict CE" -> {
                    currentLine.add(0)
                    person.triedProblems.add(this.problems[i - 4])
                }
                "verdict NO" -> {
                    currentLine.add(0)
                }
                "verdict DQ" -> {
                    currentLine.add(0)
                    println("F...")
                }
                "verdict RJ" -> {
                    currentLine.add(0)
                    this.problems[i - 4].times_solved++
                    person.rejectedProblems.add(this.problems[i - 4])
                }
                else -> {
                    throw Exception(
                        "Unknown verdict: ${
                            information[i].substringBefore("\" title=").substringAfter("class=\"")
                        }"
                    )
                }
            }
        }
        this.justTable.add(currentLine)
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
        var rawInput = normalizeOutputText(rawTableInput.substringAfter("<tr>"))

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
        println(this.justTable)
    }

    constructor(
        filename: String,
        isFile: Boolean
    ) : this(joinStringArray(BufferedReader(FileReader(filename)).readLines()))

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

    fun getDSU(a: Int): Int {
        if (dsu[a] != a) {
            dsu[a] = getDSU(dsu[a])
            return dsu[a]
        }
        return dsu[a]
    }

    fun unite(a: Int, b: Int) {
        val aa = getDSU(a)
        val bb = getDSU(b)
        if (aa == bb) return
        if (szsz[aa] > szsz[bb]) {
            dsu[bb] = aa
            szsz[aa] += szsz[bb]
        } else {
            dsu[aa] = bb
            szsz[bb] += szsz[aa]
        }
    }

    fun sizeDfs(vx: Int, vy: Int): Int {
        if (this.justTable[vx][vy] == 0) {
            return 0
        }
        used[vx][vy] = true
        szsz[vx * justTable[vx].size + vy] = 1
        var size = 1
        if (vx + 1 >= 0 && vx + 1 < this.justTable.size) {
            if (!used[vx + 1][vy] && this.justTable[vx + 1][vy] == 1) {
                size += sizeDfs(vx + 1, vy)
                unite((vx + 1) * justTable[0].size + vy, vx * justTable[0].size + vy)
            }
        }
        if (vx - 1 >= 0 && vx - 1 < this.justTable.size) {
            if (!used[vx - 1][vy] && this.justTable[vx - 1][vy] == 1) {
                size += sizeDfs(vx - 1, vy)
                unite((vx - 1) * justTable[0].size + vy, vx * justTable[0].size + vy)
            }
        }
        if (vy + 1 >= 0 && vy + 1 < this.justTable[0].size) {
            if (!used[vx][vy + 1] && this.justTable[vx][vy + 1] == 1) {
                size += sizeDfs(vx, vy + 1)
                unite(vx * justTable[0].size + vy + 1, vx * justTable[0].size + vy)
            }
        }
        if (vy - 1 >= 0 && vy - 1 < this.justTable[0].size) {
            if (!used[vx][vy - 1] && this.justTable[vx][vy - 1] == 1) {
                size += sizeDfs(vx, vy - 1)
                unite(vx * justTable[0].size + vy - 1, vx * justTable[0].size + vy)
            }
        }

        return size
    }

    fun getBestMergers(howMuch: Int): List<Pair<Int, Pair<Int, Int>>> {
        if (dsu.isEmpty()) {
            countSuperDumbnessGlobal()
        }
        val allMergers = mutableListOf<Pair<Int, Pair<Int, Int>>>()
        for (i in 0 until this.justTable.size) {
            for (j in 0 until this.justTable[0].size) {
                if (justTable[i][j] == 1)
                    continue
                var connected = 1
                var old = 0
                var map = mutableMapOf<Int, Pair<Int, Int>>()
                if (i + 1 in 0 until this.justTable.size) {
                    if (getDSU(i * this.justTable[0].size + j) != getDSU((i + 1) * this.justTable[0].size + j)) {
                        map[getDSU((i + 1) * this.justTable[0].size + j)] = (i + 1 to j)
//                        connected += szsz[(i + 1) * this.justTable[0].size + j]
//                        old += szsz[(i + 1) * this.justTable[0].size + j] * szsz[(i + 1) * this.justTable[0].size + j]
                    }
                }
                if (i - 1 in 0 until this.justTable.size) {
                    if (getDSU(i * this.justTable[0].size + j) != getDSU((i - 1) * this.justTable[0].size + j)) {
                        map[getDSU((i - 1) * this.justTable[0].size + j)] = (i - 1 to j)
//                        connected += szsz[(i - 1) * this.justTable[0].size + j]
//                        old += szsz[(i - 1) * this.justTable[0].size + j] * szsz[(i - 1) * this.justTable[0].size + j]
                    }
                }
                if (j + 1 in 0 until this.justTable[0].size) {
                    if (getDSU(i * this.justTable[0].size + j) != getDSU(i * this.justTable[0].size + j + 1)) {
                        map[getDSU((i) * this.justTable[0].size + j + 1)] = (i to j + 1)
//                        connected += szsz[i * this.justTable[0].size + j + 1]
//                        old += szsz[i * this.justTable[0].size + j + 1] * szsz[i * this.justTable[0].size + j + 1]
                    }
                }
                if (j - 1 in 0 until this.justTable[0].size) {
                    if (
                        getDSU(i * this.justTable[0].size + j) != getDSU(i * this.justTable[0].size + j - 1)) {
                        map[getDSU((i) * this.justTable[0].size + j - 1)] = (i to j - 1)
//                        connected += szsz[i * this.justTable[0].size + j - 1]
//                        old += szsz[i * this.justTable[0].size + j - 1] * szsz[i * this.justTable[0].size + j - 1]
                    }
                }
                for (e in map) {
                    old += szsz[e.key] * szsz[e.key]
                    connected += szsz[e.key]
                }
                allMergers.add((connected * connected - old) to (i to j))
            }
        }
        allMergers.sortBy { -it.first }
        return allMergers.take(howMuch)
    }

    fun countSuperDumbnessGlobal(): Int {
        used.clear()
        for (i in 0 until this.justTable.size) {
            val curr = mutableListOf<Boolean>()
            for (j in 0 until this.justTable[0].size) {
                curr.add(false)
                dsu.add(justTable[0].size * i + j)
                szsz.add(0)
            }
            used.add(curr)
        }
        var answer = 0
        for (i in 0 until this.justTable.size) {
            for (j in 0 until this.justTable[0].size) {
                if (!used[i][j]) {
                    val sz = sizeDfs(i, j)
                    answer += sz * sz
                }
            }
        }
        return answer
    }
}