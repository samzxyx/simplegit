package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;
/**Represents a commit.
 * @author Xuanyi Zhang
 * */
public class Commit implements Serializable {
    /** the message that this commit takes in. */
    private String message;
    /** the date when the commit is made. */
    private String date;
    /** all files tracked by this commit. */
    private Hashhh filestracking;
    /** the commit's parent's commit id. */
    private String _parent1id;
    /** the commit's merge parent's commit id if it has one. */
    private String _parent2id;
    /** the commit's commit id. */
    private String _commitid;

    /** constructing a commit.
     *
     * @param msg *
     * @param parent1 *
     * @param parent2 *
     * @param myfiles *
     * */
    public Commit(String msg, String parent1, String parent2, Hashhh myfiles) {
        this.message = msg;
        this._parent1id = parent1;
        this._parent2id = parent2;
        if (this._parent1id == null) {
            this.date = "00:00:00 UTC, Thursday, 1 January 1970";
            filestracking = new Hashhh();
        } else {
            long millis = System.currentTimeMillis();
            Timestamp current = new Timestamp(millis);
            SimpleDateFormat myformat = new SimpleDateFormat(
                    "EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
            this.date = myformat.format(current);

            filestracking = myfiles;
        }
        _commitid = Utils.sha1(Utils.serialize(this));
    }

    /** the commit's commit id.
     *
     * @return String *
     */
    public String getMessage() {
        return this.message;
    }

    /** the commit's commit id.
     *
     * @return String
     */
    public String getDate() {
        return this.date;
    }

    /** the commit's commit id.
     *
     * @return String *
     */
    public String getParentoneId() {
        return _parent1id;
    }

    /** the commit's commit id.
     *
     * @return String *
     */
    public String getParenttwoID() {
        return _parent2id;
    }

    /** the commit's commit id.
     *
     * @return String[] *
     */
    public String[] getParentIds() {
        if (getParenttwoID() != null && getParentoneId() != null) {
            return new String[]{getParentoneId(), getParenttwoID()};
        } else if (getParenttwoID() == null && getParentoneId() != null) {
            return new String[]{getParentoneId()};
        } else {
            return new String[]{};
        }
    }

    /** the commit's commit id.
     *
     * @return hashhh *
     */
    public Hashhh getFilestracking() {
        return this.filestracking;
    }

    /** the commit's commit id.
     *
     * @return String *
     */
    public String getCommitId() {
        return this._commitid;
    }

    /** the commit's commit id.
     *
     * @throws IOException
     */
    public void saveCommit() throws IOException {
        File newcommit = Utils.join(CommandClass.COMMIT_DIR, this._commitid);
        if (!newcommit.exists()) {
            newcommit.createNewFile();
        }
        Utils.writeObject(newcommit, this);
    }

    /** the commit's commit id.
     *
     * @param name *
     * @return Commit *
     */
    public static Commit fromFile(String name) {
        File wantedcommit = Utils.join(CommandClass.COMMIT_DIR, name);
        if (wantedcommit.exists()) {
            return Utils.readObject(wantedcommit, Commit.class);
        } else {
            throw new IllegalArgumentException("Commit with "
                    + "name passed in doesn't exit");
        }
    }
}
