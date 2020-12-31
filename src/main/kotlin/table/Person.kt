package table

class Person(val id: Int, val groupPrefix: String, val name: String) {
    val solvedProblems = mutableSetOf<Problem>()
    val rejectedProblems = mutableSetOf<Problem>()
    val pendingReview = mutableSetOf<Problem>()
    val triedProblems = mutableSetOf<Problem>()
    val bannedProblems = mutableSetOf<Problem>()

    override fun toString(): String {
        return "User with id $id, in $groupPrefix name $name."
    }

    fun getInformation(): String {
        return """$name solved ${solvedProblems.size} problems, get rejected ${rejectedProblems.size},
            | and banned ${bannedProblems.size}. Waiting for review on ${pendingReview.size} problems""".trimMargin()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Person) return false      //TODO: should it throw an exception?
        return this.id == other.id
    }

    override fun hashCode(): Int {
        return id
    }

    fun isContestClosed(contest: Contest): Boolean {
        return solvedProblems.containsAll(contest.container)
    }

    fun getOrderProblems(problems: List<Problem>): List<Problem> {
        val result = mutableListOf<Problem>()
        result.addAll(rejectedProblems)
        result.addAll(triedProblems)            //TODO: check if tried ^ rejected == 0
        val toSort = mutableListOf<Problem>()
        for (problem in problems) {
            if (problem !in rejectedProblems && problem !in solvedProblems && problem !in pendingReview && problem !in triedProblems && problem !in bannedProblems) {
                toSort.add(problem)
            }
        }
        toSort.sortBy { -it.times_solved }
        result.addAll(toSort)
        return result
    }
}