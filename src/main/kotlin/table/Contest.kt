package table

class Contest(val id: Int, val name: String) {
    val container: MutableList<Problem> = mutableListOf()

    fun addProblem(problem: Problem) {
        this.container.add(problem)
    }

    fun getProblemsInformation(): String {
        var result = ""
        for (problem in this.container) {
            result += (problem.toString() + '\n')
        }
        return result
    }

    override fun toString(): String {
        return "Contest with id $id and name $name."
    }
}