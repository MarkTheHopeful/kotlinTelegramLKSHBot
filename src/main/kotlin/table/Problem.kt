package table

class Problem(val id: Int, val name: String, val contest_id: Int, var times_solved: Int) {
    override fun toString() =
        "Problem with id $id, name $name, in contest number ${contest_id + 1}. Solved $times_solved times"

    override fun equals(other: Any?): Boolean {
        if (other !is Problem) return false      //TODO: should it throw an exception?
        return this.id == other.id
    }

    override fun hashCode(): Int {
        return this.id
    }
}