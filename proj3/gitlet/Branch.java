package gitlet;

import java.io.Serializable;
/**Represents a branch.
 * @author Xuanyi Zhang
 * */
public class Branch implements Serializable {
    /** the commit that this branch points to. */
    private Commit _commit;
    /** the name of the branch.*/
    private String _name;
    /**whether this branch is the current active branch.*/
    private Boolean _head;
    /**a branch constructor.
     * @param commit *
     * @param name *
     * */
    public Branch(Commit commit, String name) {
        _commit = commit;
        _name = name;
    }

    /**return this commit.
     * @return Commit *
     */
    public Commit getCommit() {
        return this._commit;
    }

    /**return this name.
     * @return String *
     */
    public String getName() {
        return this._name;
    }

    /**return this head.
     * @return Boolean *
     */
    public Boolean getHead() {
        return this._head;
    }

    /**set the head instance of this branch.
     * @param ruhead *
     */
    public void setHead(Boolean ruhead) {
        if (ruhead) {
            _head = true;
        } else {
            _head = false;
        }
    }

}
