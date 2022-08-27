package gitlet;



import java.io.File;

import java.io.IOException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Queue;

/** Command Execution.
 *  @author Xuanyi Zhang
 */

public class CommandClass {

    /** Current Working Directory. */
    static final File CWD = new File(".");

    /** Main metadata folder. */
    static final File GITLET_FOLDER = Utils.join(CWD, ".gitlet");
    /** All commits folder. */
    static final File COMMIT_DIR = Utils.join(GITLET_FOLDER, "commits");
    /** Staging area folder. */
    static final File STAGING_DIR = Utils.join(GITLET_FOLDER, "staging");
    /** Staging blob folder. */
    static final File STAGEBLOB_DIR = Utils.join(STAGING_DIR,
            "stagingfileblobs");
    /** Adding stage. */
    static final File ADD = Utils.join(STAGING_DIR, "addstage");
    /** Removal stage. */
    static final File REMOVE = Utils.join(STAGING_DIR, "removestage");
    /** Branches files contain the branch hashhh map.
     * branch points to the most recent commit and head points
     * to the current branch.
     * */
    static final File BRANCHES = Utils.join(GITLET_FOLDER, "branches");
    /** Head file contains pointer to the current branch. */
    static final File HEAD = Utils.join(GITLET_FOLDER, "head");
    /** Blob folder contains all blobs of files that was once committed. */
    static final File BLOB_DIR = Utils.join(GITLET_FOLDER, "blobs");

    /**hash map of file names in adding stage as
     * the key and the hashid as value.
     * */
    private Hashhh addition = new Hashhh();
    /**hash map of file names in removal stage
     * as key and the hashid as value.
     * */
    private Hashhh removal = new Hashhh();
    /**hash map of branch names as key and most recent
     * commit id of that branch as key.
     * */
    private Hashhh branches = new Hashhh();

    /**Create the persistence by creating directories and files.
     */
    public void setupPersistence() throws IOException {
        if (!GITLET_FOLDER.exists()) {
            GITLET_FOLDER.mkdirs();
        }
        if (!COMMIT_DIR.exists()) {
            COMMIT_DIR.mkdirs();
        }
        if (!STAGING_DIR.exists()) {
            STAGING_DIR.mkdirs();
        }
        if (!ADD.exists()) {
            ADD.createNewFile();
        }
        if (!REMOVE.exists()) {
            REMOVE.createNewFile();
        }
        if (!BRANCHES.exists()) {
            BRANCHES.createNewFile();
        }
        if (!HEAD.exists()) {
            HEAD.createNewFile();
        }
        if (!BLOB_DIR.exists()) {
            BLOB_DIR.mkdirs();
        }
        if (!STAGEBLOB_DIR.exists()) {
            STAGEBLOB_DIR.createNewFile();
        }
    }

    /** Dealing with java.main gitlet init.
     * Creates a new Gitlet version-control system in the current directory.
     * This system will automatically start with one commit: a commit that
     * contains no files and has the commit message initial commit
     * (just like that,with no punctuation). It will have a single
     * branch: master, which initially points to this initial commit,
     * and master will be the current branch. The timestamp for this initial
     * commit will be 00:00:00 UTC, Thursday, 1 January 1970
     * in whatever format you choose for dates (this is called
     * "The (Unix) Epoch", represented internally by the time 0.)
     * Since the initial commit in all repositories created by Gitlet
     * will have exactly the same content, it follows that all repositories
     * will automatically share this commit (they will all have the same UID)
     * and all commits in all repositories will trace back to it.
     * */
    /**use try catch instead.*/
    public void init() throws IOException {

        if (!GITLET_FOLDER.exists()) {
            setupPersistence();
            Commit commit = new Commit("initial commit",
                    null,  null,  new Hashhh());
            Branch masterbranch = new Branch(commit, "master");
            masterbranch.setHead(true);
            getBranches().put(masterbranch.getName(),
                    masterbranch.getCommit().getCommitId());
            Utils.writeObject(BRANCHES, getBranches());
            Utils.writeObject(HEAD, masterbranch);
            Utils.writeObject(ADD, new Hashhh());
            Utils.writeObject(REMOVE, new Hashhh());
            commit.saveCommit();
        } else {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        }
    }
    /**
     * print the error message and exit with code 0.
     * @param  msg *
     */
    public void printmessagenexit(String msg) {
        System.out.println(msg);
        System.exit(0);
    }
    /**Saves a snapshot of tracked files in the current commit and
     * staging area so they can be restored at a later
     * time, creating a new commit. The commit is said to be tracking
     * the saved files. By default, each commit's
     * snapshot of files will be exactly the same as its parent commit's
     * snapshot of files; it will keep versions
     * of files exactly as they are, and not update them. A commit
     * will only update the contents of files it is
     * tracking that have been staged for addition at the time of
     * commit, in which case the commit will now include
     * the version of the file that was staged instead of the version
     * it got from its parent. A commit will save and
     * start tracking any files that were staged for addition but
     * weren't tracked by its parent. Finally, files
     * tracked in the current commit may be untracked in the new
     * commit as a result being staged for removal by the
     * rm command (below).
     *The bottom line: By default a commit is the same as its parent.
     * Files staged for addition and removal are the
     *updates to the commit. Of course, the date (and likely the message)
     * will also different from the parent.
     *Some additional points about commit:
     *The staging area is cleared after a commit.
     *The commit command never adds, changes, or removes files in the
     * working directory (other than those in the .gitlet directory).
     * The rm command will remove such files, as well as staging them
     * for removal, so that they will be untracked after a commit.
     *Any changes made to files after staging for addition or removal
     * are ignored by the commit command, which only modifies the
     * contents of the .gitlet directory.
     * For example, if you remove a tracked file using the Unix rm command
     * (rather than Gitlet's command of the same name), it has no effect
     * on the next commit,
     * which will still contain the deleted version of the file.
     *After the commit command, the new commit is added as a new node in
     * the commit tree.
     *The commit just made becomes the "current commit", and the head
     * pointer now points to it. The previous head commit is this
     * commit's parent commit.
     *Each commit should contain the date and time it was made.
     *Each commit has a log message associated with it that describes
     * the changes to the files in the commit. This is specified by the user.
     * The entire message should take up only one entry in the array args
     * that is passed to main. To include multiword messages, you'll
     * have to surround them in quotes.
     *Each commit is identified by its SHA-1 id, which must include the
     * file (blob) references of its files, parent reference,
     * log message, and commit time.
     * @param args *
     * */

    public void commit(String[] args) throws IOException {
        validateNumArgs("commit", args, 2);
        if (args[1].isBlank()) {
            printmessagenexit("Please enter a commit message.");
        } else {
            removal = Utils.readObject(REMOVE, gitlet.Hashhh.class);
            addition = Utils.readObject(ADD, gitlet.Hashhh.class);
            if ((removal.size() == 0) && (addition.size() == 0)) {
                printmessagenexit("No changes added to the commit.");
            } else {
                Commit curcommit = Utils.readObject(HEAD,
                        Branch.class).getCommit();
                Hashhh h = curcommit.getFilestracking();
                if (removal != null) {
                    for (String key:removal.keySet()) {
                        h.remove(key); }
                }
                if (addition != null) {
                    for (String key:addition.keySet()) {
                        h.put(key, addition.get(key)); }
                }
                String[] splitmessage = args[1].split(" ");
                Commit newcommit;
                if (splitmessage[0].equals("Merged")
                        && splitmessage[2].equals("into")) {
                    String mergedbranch = splitmessage[1];
                    branches = Utils.readObject(BRANCHES, Hashhh.class);
                    String mergedcommitid = branches.get(mergedbranch);
                    newcommit = new Commit(args[1],
                            curcommit.getCommitId(), mergedcommitid, h);
                } else {
                    newcommit = new Commit(args[1],
                            curcommit.getCommitId(), null, h);
                }
                addition.clear(); removal.clear();
                STAGEBLOB_DIR.delete(); STAGEBLOB_DIR.mkdirs();
                for (String key: h.keySet()) {
                    String blobhash = h.get(key);
                    File temp = Utils.join(BLOB_DIR, blobhash);
                    if (!temp.exists()) {
                        temp.createNewFile();
                        File targetfile = Utils.join(CWD, key);
                        Utils.writeContents(temp,
                                Utils.readContentsAsString(targetfile));
                    }
                }
                Branch curbranch = Utils.readObject(HEAD, Branch.class);
                Branch updatedbranch = new Branch(newcommit,
                        curbranch.getName());
                updatedbranch.setHead(true);
                branches = Utils.readObject(BRANCHES, gitlet.Hashhh.class);
                branches.put(updatedbranch.getName(),
                        updatedbranch.getCommit().getCommitId());
                Utils.writeObject(ADD, addition);
                Utils.writeObject(REMOVE, removal);
                Utils.writeObject(BRANCHES, branches);
                Utils.writeObject(HEAD, updatedbranch); newcommit.saveCommit();
            }
        }
    }
    /**
     * Adds a copy of the file as it currently exists to the
     * staging area (see the description
     * of the commit command). For this reason, adding a file
     * is also called staging the file for
     * addition. Staging an already-staged file overwrites
     * the previous entry in the staging area
     * with the new contents. The staging area should be
     * somewhere in .gitlet. If the current
     * working version of the file is identical to the version
     * in the current commit, do not stage
     * it to be added, and remove it from the staging area
     * if it is already there (as can happen
     * when a file is changed, added, and then changed back).
     * The file will no longer be staged for
     * removal (see gitlet rm), if it was at the time of the command.
     * @param args *
     * */
    public void add(String[] args) throws IOException {
        validateNumArgs("add", args, 2);
        File targetfile = Utils.join(CWD, args[1]);
        if (targetfile.exists()) {
            String filecontent = blobid(targetfile);
            Commit curcommitid = Utils.readObject(HEAD,
                    Branch.class).getCommit();
            removal = Utils.readObject(REMOVE, Hashhh.class);
            if (removal.containsKey(args[1])) {
                removal.remove(args[1]);
                Utils.writeObject(REMOVE, removal);
            }
            if (Utils.readContentsAsString(ADD).length() != 0) {
                addition = Utils.readObject(ADD, gitlet.Hashhh.class);
                if (!(curcommitid.getFilestracking() == null)
                        && curcommitid.getFilestracking().containsKey(args[1])
                        && curcommitid.getFilestracking()
                        .get(args[1]).equals(filecontent)) {
                    if (addition.containsKey(args[1])) {
                        addition.remove(args[1]);
                    } else {
                        System.exit(0);
                    }
                } else {
                    if (addition.containsKey(args[1])
                            && addition.get(args[1]).equals(filecontent)) {
                        System.exit(0);
                    } else {
                        addition.put(args[1], filecontent);
                    }
                }
            }

            if (Utils.readContentsAsString(ADD).length() == 0) {
                if (!(curcommitid.getFilestracking() == null)
                        && curcommitid.getFilestracking().containsKey(args[1])
                        && curcommitid.getFilestracking()
                        .get(args[1]).equals(filecontent)) {
                    System.exit(0);
                } else {
                    addition.put(args[1], filecontent);
                }
            }
            Utils.writeObject(ADD, addition);
        } else {
            System.out.println("File doesn't exist");
            System.exit(0);
        }
    }

    /**
     * Starting at the current head commit, display information
     * about each commit backwards along the commit
     * tree until the initial commit, following the first parent
     * commit links, ignoring any second parents
     * found in merge commits. (In regular Git, this is what you
     * get with git log --first-parent). This set
     * of commit nodes is called the commit's history. For every
     * node in this history, the information it should
     * \display is the commit id, the time the commit was made,
     * and the commit message. Here is an example of
     * the exact format it should follow:
     * */
    public void log() {
        Formatter out = new Formatter();
        Commit displaycommit = Utils.readObject(HEAD, Branch.class).getCommit();
        while (displaycommit.getParentoneId() != null) {
            out.format("===%n");
            out.format("%s %s%n", "commit", displaycommit.getCommitId());
            out.format("%s %s%n", "Date:", displaycommit.getDate());
            out.format("%s%n", displaycommit.getMessage());
            out.format("%n");
            displaycommit = displaycommit
                    .fromFile(displaycommit.getParentoneId());
        }
        out.format("===%n");
        out.format("%s %s%n", "commit", displaycommit.getCommitId());
        out.format("%s %s%n", "Date:", "Wed Dec 31 16:00:00 1969 -0800");
        out.format("%s%n", displaycommit.getMessage());

        System.out.println(out.toString());

    }

    /** Main checkout function.
     * Calls to the 3 helper functions.
     * @param args *
     */
    public void checkout(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("missing argument");
            System.exit(0);
        } else if (args.length == 3) {
            Commit targetcommit = Utils.readObject(HEAD,
                    Branch.class).getCommit();
            checkout1(args[2], targetcommit);
        } else if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            checkout2(args[1], args[3]);
        } else if (args.length == 2) {
            checkout3(args[1]);
        } else {
            throw new GitletException();
        }
    }
    /** convert short id back to the 40 digits version.
     * @param id *
     * @return String *
     */
    private String convertidback(String id) {
        if (id.length() == Utils.UID_LENGTH) {
            return id;
        }

        File[] commits = COMMIT_DIR.listFiles();

        for (File file : commits) {
            if (file.getName().contains(id)) {
                return file.getName();
            }
        }
        return null;
    }
    /** check if there is untrackedfiles in the CWD, untracked.
     * files in checkout context means the file
     * is not present in the lastest head commit
     */
    public void untrackedfiles() {
        Commit mostrecent = Utils.readObject(HEAD, Branch.class).getCommit();
        for (File file:CWD.listFiles()) {
            if (mostrecent.getFilestracking() == null
                    && CWD.listFiles().length >= 1) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first");
                System.exit(0);
            } else if (!mostrecent.getFilestracking().keySet()
                    .contains(file.getName())
                    && !Utils.readObject(ADD, Hashhh.class)
                    .containsKey(file.getName())
                    && !file.getName().equals(".gitlet")) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first");
                System.exit(0);
            }
        }

    }

    /**Takes the version of the file as it exists in the head
     * commit, the front of the current branch, and puts it in
     * the working directory, overwriting the version of the
     * file that's already there if there is one. The new version
     * of the file is not staged.
     * @param filename *
     * @param targetc *
     */
    public void checkout1(String filename, Commit targetc) throws IOException {
        if (!targetc.getFilestracking().keySet().contains(filename)) {
            System.out.println("File does not exist in that commit");
            System.exit(0);
        } else {
            File temp = Utils.join(CWD, filename);
            String hash = targetc.getFilestracking().get(filename);
            if (!temp.exists()) {
                temp.createNewFile();
            }
            File contentofhash = Utils.join(BLOB_DIR, hash);
            String contents = Utils.readContentsAsString(contentofhash);
            Utils.writeContents(temp, contents);
        }
    }

    /**Takes the version of the file as it exists in the commit with the
     * given id, and puts it in the working directory, overwriting the
     * version of the file that's already there if there is one.
     * The new version of the file is not staged.
     * @param filename *
     * @param comid *
     */

    public void checkout2(String comid, String filename) throws IOException {
        comid = convertidback(comid);
        File temp = Utils.join(COMMIT_DIR, comid);
        if (!temp.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        } else {
            Commit targetcom = Utils.readObject(temp, Commit.class);
            checkout1(filename, targetcom);
        }

    }
    /**Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory, overwriting the versions
     * of the files that are already there if they exist. Also,
     * at the end of this command, the given branch will now be
     * considered the current branch (HEAD). Any files that are tracked in the
     * current branch but are not present in the checked-out branch
     * are deleted. The staging area is cleared, unless the checked-out
     * branch is the current branch (see Failure cases below).
     * If no branch with that name exists, print No such branch
     * exists. If that branch is the current branch, print No need to checkout
     * the current branch. If a working file is untracked in the
     * current branch and would be overwritten by the checkout, print
     * There is an untracked file in the way; delete it, or add and commit
     * it first. and exit; perform this check before doing
     * anything else.
     * @param branchname *
     */
    public void checkout3(String branchname) throws IOException {
        branches = Utils.readObject(BRANCHES, Hashhh.class);
        Branch currentbranch = Utils.readObject(HEAD, Branch.class);
        if (!branches.containsKey(branchname)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (currentbranch.getName().equals(branchname)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        untrackedfiles();
        String checkoutcommitid = branches.get(branchname);
        File checkoutcommitpath = Utils.join(COMMIT_DIR, checkoutcommitid);
        Commit checkoutcommit;
        if (checkoutcommitpath.exists()) {
            checkoutcommit = Utils.readObject(checkoutcommitpath, Commit.class);
        } else {
            throw new IllegalArgumentException("Commit with name "
                    + "passed in doesn't exit");
        }
        for (File f: CWD.listFiles()) {
            if (!checkoutcommit.getFilestracking()
                    .keySet().contains(f.getName())) {
                f.delete();
            }
        }
        for (String s: checkoutcommit.getFilestracking().keySet()) {
            checkout1(s, checkoutcommit);
        }
        Branch newheadbranch = new Branch(checkoutcommit, branchname);
        Utils.writeObject(HEAD, newheadbranch);
        addition.clear();
        removal.clear();
        Utils.writeObject(ADD, addition);
        Utils.writeObject(REMOVE, removal);
    }
    /** Displays what branches currently exist, and marks the
     * current branch with a *. Also displays
     *  what files have been staged for addition or removal.
     *
     */
    public void status() {
        System.out.println("=== Branches ===");
        String[] branchnames = Utils.readObject(BRANCHES,
                Hashhh.class).keySet().toArray(new String[0]);
        Branch headbranch = Utils.readObject(HEAD, Branch.class);
        Commit currentcommit = headbranch.getCommit();
        Arrays.sort(branchnames);
        for (String s:branchnames) {
            if (headbranch.getName().equals(s)) {
                System.out.println("*" + s);
            } else {
                System.out.println(s);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        String[] addnames = Utils.readObject(ADD,
                Hashhh.class).keySet().toArray(new String[0]);
        Arrays.sort(addnames);
        for (String m: addnames) {
            System.out.println(m);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        String[] removenames = Utils.readObject(REMOVE,
                Hashhh.class).keySet().toArray(new String[0]);
        Arrays.sort(removenames);
        for (String n: removenames) {
            System.out.println(n);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        statushelper(currentcommit);
        System.out.println("=== Untracked Files ===");
        ArrayList<String> untracked = new ArrayList<>();
        for (File x: CWD.listFiles()) {
            if (!currentcommit.getFilestracking().containsKey(x.getName())
                    && !addition.containsKey(x.getName())) {
                untracked.add(x.getName());
            }
        }
        String[] untrackedarray = untracked.toArray(new String[0]);
        Arrays.sort(untrackedarray);
        for (String y : untrackedarray) {
            if (!y.equals(".gitlet")) {
                System.out.println(y);
            }
        }
        System.out.println();
    }

    /** lol.
     *
     * @param currentcommit *
     */
    public void statushelper(Commit currentcommit) {
        Hashhh modi = new Hashhh();
        addition = Utils.readObject(ADD, Hashhh.class);
        removal = Utils.readObject(REMOVE, Hashhh.class);
        for (File e: CWD.listFiles()) {
            if (currentcommit.getFilestracking()
                    .keySet().contains(e.getName())) {
                if (!blobid(e).equals(currentcommit
                        .getFilestracking().get(e.getName()))
                        && !addition.containsKey(e.getName())
                        && !removal.containsKey(e.getName())) {
                    modi.put(e.getName(), "(modified)");
                }
            }
            if (addition.keySet().contains(e.getName())) {
                if (!blobid(e).equals(addition.get(e.getName()))) {
                    modi.put(e.getName(), "(modified)");
                }
            }
            if (removal.keySet().contains(e.getName())) {
                if (!blobid(e).equals(removal.get(e.getName()))) {
                    modi.put(e.getName(), "(modified)");
                }
            }
        }
        for (String b: addition.keySet()) {
            File temp = Utils.join(CWD, b);
            if (!temp.exists()) {
                modi.put(b, "(deleted)");
            }
        }
        for (String c: currentcommit.getFilestracking().keySet()) {
            File delete = Utils.join(CWD, c);
            if (!delete.exists() && !removal.containsKey(c)) {
                modi.put(c, "(deleted)");
            }
        }
        String[] modinames = modi.keySet().toArray(new String[0]);
        Arrays.sort(modinames);
        for (String d: modinames) {
            System.out.println(d + " " + modi.get(d));
        }
        System.out.println();
    }
    /**Unstage the file if it is currently staged for addition.
     * If the file is tracked in the current commit, stage
     * it for removal and remove the file from
     * the working directory if the user has not already
     * done so (do not remove it unless
     * it is tracked in the current commit).
     * @param args *
     */
    public void rm(String[] args) {
        File tbrm = Utils.join(CWD, args[1]);
        Commit recentcommit = Utils.readObject(HEAD, Branch.class).getCommit();
        Boolean inaddstage = Utils.readObject(ADD,
                Hashhh.class).containsKey(args[1]);
        Boolean inrecentcommit = recentcommit.getFilestracking()
                .containsKey(args[1]);
        if (!tbrm.exists() && !inrecentcommit) {
            System.out.println("File doesn't exist");
            System.exit(0);
        }
        if (!inrecentcommit && !inaddstage) {
            System.out.println("No reason to remove the file");
            System.exit(0);
        }
        if (inaddstage) {
            addition = Utils.readObject(ADD, Hashhh.class);
            addition.remove(args[1]);
            Utils.writeObject(ADD, addition);
        }
        if (inrecentcommit) {
            removal = Utils.readObject(REMOVE, gitlet.Hashhh.class);
            removal.put(args[1], recentcommit.getFilestracking().get(args[1]));
            Utils.writeObject(REMOVE, removal);
            if (tbrm.exists()) {
                tbrm.delete();
            }
        }
    }

    /** Like log, except displays information about all commits
     * ever made. The order of the commits does
     * not matter. Hint: there is a useful method in gitlet.
     * Utils that will help you iterate over files
     * within a directory.
     */

    public void globallog() {
        Formatter out = new Formatter();
        List<String> commitidlist = Utils.plainFilenamesIn(COMMIT_DIR);
        for (String a : commitidlist) {
            File afile = Utils.join(COMMIT_DIR, a);
            Commit acommit = Utils.readObject(afile, Commit.class);
            if (acommit.getParentoneId() == null) {
                out.format("===%n");
                out.format("%s %s%n", "commit", acommit.getCommitId());
                out.format("%s %s%n", "Date:",
                        "Wed Dec 31 16:00:00 1969 -0800");
                out.format("%s%n", acommit.getMessage());
                out.format("%n");
            } else {
                out.format("===%n");
                out.format("%s %s%n", "commit", acommit.getCommitId());
                out.format("%s %s%n", "Date:", acommit.getDate());
                out.format("%s%n", acommit.getMessage());
                out.format("%n");
            }
        }
        System.out.println(out.toString());
    }
    /**Prints out the ids of all commits that have the
     * given commit message, one per line.
     * If there are multiple such commits, it prints the
     * ids out on separate lines. The commit
     * message is a single operand; to indicate a multiword
     * message, put the operand in quotation
     * marks, as for the commit command above.
     * @param args *
     */
    public void find(String[] args) {
        List<String> commitidlist = Utils.plainFilenamesIn(COMMIT_DIR);
        int count = 0;
        for (String a : commitidlist) {
            File afile = Utils.join(COMMIT_DIR, a);
            Commit acommit = Utils.readObject(afile, Commit.class);
            if (acommit.getMessage().equals(args[1])) {
                System.out.println(acommit.getCommitId());
                count += 1;
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }
    /**Creates a new branch with the given name, and points
     * it at the current head node. A branch is
     *  nothing more than a name for a reference (a SHA-1
     *  identifier) to a commit node. This command
     *  does NOT immediately switch to the newly created
     *  branch (just as in real Git). Before you ever
     *  call branch, your code should be running with a
     *  default branch called "master".
     * @param args *
     */
    public void branch(String[] args) {
        branches = Utils.readObject(BRANCHES, Hashhh.class);
        if (branches.containsKey(args[1])) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        } else {
            Branch headbranch = Utils.readObject(HEAD, Branch.class);
            Branch newbranch = new Branch(headbranch.getCommit(), args[1]);
            branches.put(newbranch.getName(),
                    newbranch.getCommit().getCommitId());
            Utils.writeObject(BRANCHES, branches);
        }
    }
    /**Deletes the branch with the given name.
     * This only means to delete the pointer associated with
     * the branch; it does not mean to delete all
     * commits that were created under the branch, or
     * anything like that.
     * @param args *
     */
    public void rmbranch(String[] args) {
        branches = Utils.readObject(BRANCHES, Hashhh.class);
        if (!branches.containsKey(args[1])) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        Branch headbranch = Utils.readObject(HEAD, Branch.class);
        if (headbranch.getName().equals(args[1])) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        } else {
            branches.remove(args[1]);
            Utils.writeObject(BRANCHES, branches);
        }
    }
    /**Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present
     *  in that commit. Also moves the current branch's head
     *  to that commit node. See the intro for an
     *  example of what happens to the head pointer after using
     *  reset. The [commit id] may be
     *  abbreviated as for checkout. The staging area is cleared.
     *  The command is essentially checkout
     *  of an arbitrary commit that also changes the current branch head.
     *  If no commit with the given id exists, print No commit
     *  with that id exists. If a working file
     *  is untracked in the current branch and would be
     *  overwritten by the reset, print There is an
     *  untracked file in the way; delete it, or add and
     *  commit it first. and exit; perform this check
     *  before doing anything else.
     * @param args *
     */
    public void reset(String[] args) throws IOException {
        untrackedfiles();
        File wantedcommitpath = Utils.join(COMMIT_DIR, args[1]);
        if (!wantedcommitpath.exists()) {
            System.out.println("No commit with that id exists");
            System.exit(0);
        } else {
            Commit wantedcommit = Utils.readObject(wantedcommitpath,
                    Commit.class);
            for (File f: CWD.listFiles()) {
                if (!wantedcommit.getFilestracking()
                        .keySet().contains(f.getName())) {
                    f.delete();
                }
            }
            for (String s: wantedcommit.getFilestracking().keySet()) {
                checkout1(s, wantedcommit);
            }
            String branchname = Utils.readObject(HEAD, Branch.class).getName();
            Branch newheadbranch = new Branch(wantedcommit, branchname);
            Utils.writeObject(HEAD, newheadbranch);
            branches = Utils.readObject(BRANCHES, Hashhh.class);
            branches.put(branchname, wantedcommit.getCommitId());
            Utils.writeObject(BRANCHES, branches);
            addition.clear();
            removal.clear();
            Utils.writeObject(ADD, addition);
            Utils.writeObject(REMOVE, removal);
        }

    }

    /** merge error handler.
     *
     * @param args *
     * @throws IOException
     */
    public void mergeerrorhandle(String[] args) throws IOException {
        branches = Utils.readObject(BRANCHES, Hashhh.class);
        if (!branches.containsKey(args[1])) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        addition = Utils.readObject(ADD, Hashhh.class);
        removal = Utils.readObject(REMOVE, Hashhh.class);
        if (!addition.isEmpty() || !removal.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        Branch currentbranch = Utils.readObject(HEAD, Branch.class);
        if (currentbranch.getName().equals(args[1])) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        String givenbranchname = args[1];
        String givenid = branches.get(givenbranchname);
        String splitpointcommitid = findsplitpoint(currentbranch
                .getCommit().getCommitId(), givenid);
        if (splitpointcommitid.equals(givenid)) {
            System.out.println("Given branch is an "
                    + "ancestor of the current branch.");
            System.exit(0);
        }
        if (splitpointcommitid.equals(currentbranch
                .getCommit().getCommitId())) {
            checkout3(args[1]);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
    }

    /** merge helper function.
     * helps to get all files in three commits
     * @param splitc *
     * @param givenc *
     * @param currc *
     * @return ArrayList<String> *
     */
    public ArrayList<String> mergehelpgetfiles(Commit splitc,
                                               Commit givenc, Commit currc) {
        ArrayList<String> allfiles = new ArrayList<>();
        for (String s:splitc.getFilestracking().keySet()) {
            allfiles.add(s);
        }
        for (String m: givenc.getFilestracking().keySet()) {
            if (!allfiles.contains(m)) {
                allfiles.add(m);
            }
        }
        for (String n: currc.getFilestracking().keySet()) {
            if (!allfiles.contains(n)) {
                allfiles.add(n);
            }
        }
        return allfiles;
    }
    /**take in branch name.
     * @param args *
     */
    public void merge(String[] args) throws IOException {
        validateNumArgs("merge", args, 2);
        untrackedfiles(); mergeerrorhandle(args);
        branches = Utils.readObject(BRANCHES, Hashhh.class);
        Branch currentbranch = Utils.readObject(HEAD, Branch.class);
        String givenbranchname = args[1];
        String givenid = branches.get(givenbranchname);
        String splitpointcommitid = findsplitpoint(currentbranch
                .getCommit().getCommitId(), givenid);
        Commit sc = Commit.fromFile(splitpointcommitid);
        Commit gc = Commit.fromFile(givenid);
        Commit cc = currentbranch.getCommit();
        ArrayList<String> allfiles = mergehelpgetfiles(sc, gc, cc);
        Boolean conflict = false;
        for (String a: allfiles) {
            String resultcontents = ""; String content1 = "";
            String content2 = ""; String content3 = "";
            if (sc.getFilestracking().containsKey(a)) {
                content1 = sc.getFilestracking().get(a);
            }
            if (cc.getFilestracking().containsKey(a)) {
                content2 = cc.getFilestracking().get(a);
            }
            if (gc.getFilestracking().containsKey(a)) {
                content3 = gc.getFilestracking().get(a);
            }
            if (!content1.equals("")) {
                if (!content1.equals(content3) && content1.equals(content2)) {
                    resultcontents = content3;
                    mergehelper(resultcontents, content2, givenid, a);
                } else if (!content1.equals(content2)
                        && !content1.equals(content3)) {
                    if (!content2.equals(content3)) {
                        conflict = true;
                        conflicthandler(a, cc, gc);
                    }
                } else if (content1.equals(content2) && content3.equals("")) {
                    resultcontents = content3;
                    mergehelper(resultcontents, content2, givenid, a);
                }
            } else {
                if (content2.equals("") & !content3.equals("")) {
                    resultcontents = content3;
                    mergehelper(resultcontents, content2, givenid, a);
                } else if (!content1.equals(content2)
                        && !content1.equals(content3)) {
                    if (!content2.equals(content3)) {
                        conflict = true;
                        conflicthandler(a, cc, gc);
                    }
                }
            }
        }
        commit(new String[]{"commit", "Merged "
                + givenbranchname + " into " + currentbranch.getName() + "."});
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** merge helper.
     *
     * @param rc *
     * @param c2 *
     * @param gid *
     * @param a *
     * @throws IOException
     */
    public void mergehelper(String rc, String c2,
                            String gid, String a) throws IOException {
        if (!rc.equals("")
                && !rc.equals(c2)) {
            checkout(new String[]{"checkout", gid, "--", a});
            add(new String[]{"add", a});
        } else if (rc.equals("")
                && !c2.equals("")) {
            File delete = Utils.join(CWD, a);
            delete.delete();
            rm(new String[]{"rm", a});
        }
    }

    /** merge conflict handler.
     *
     * @param a *
     * @param cc *
     * @param gc *
     * @throws IOException
     */
    public void conflicthandler(String a,
                                Commit cc, Commit gc) throws IOException {

        File temp = Utils.join(CWD, a);
        if (!temp.exists()) {
            temp.createNewFile();
        }
        String contents = "<<<<<<< HEAD\n";

        if (cc.getFilestracking().containsKey(a)) {
            String hash = cc.getFilestracking().get(a);
            File contentofhash = Utils.join(BLOB_DIR, hash);
            contents += Utils.readContentsAsString(
                    contentofhash);
        }
        contents += "=======\n";
        if (gc.getFilestracking().containsKey(a)) {
            String otherhash = gc
                    .getFilestracking().get(a);
            File contentofotherhash = Utils.join(BLOB_DIR,
                    otherhash);
            contents += Utils.readContentsAsString(
                    contentofotherhash);
        }
        contents += ">>>>>>>\n";
        Utils.writeContents(temp, contents);

        add(new String[]{"add", a});

    }

    /** helps to find the split point.
     * which is defind as the lastest common ancestor of the
     * given branch and the current branch.
     * In the case of multiple lastest common ancestors,
     * choose the one that is closer to the current branch's
     * lastest commit, or if they are equal distance
     * choose randomly
     * @param currentbranchid *
     * @param givenbranchid *
     * @return String *
     */
    public String findsplitpoint(String currentbranchid, String givenbranchid) {
        HashMap<String, Integer> currentancestors = new HashMap<>();
        HashMap<String, Integer> givenancestors = new HashMap<>();
        Commit currentcommit = Commit.fromFile(currentbranchid);
        String[] parent = currentcommit.getParentIds();
        Queue<String> parents = new ArrayDeque<>();
        for (int i = 0; i < parent.length; i++) {
            parents.add(parent[i]);
        }
        currentancestors.put(currentcommit.getCommitId(), 0);
        Commit givencommit = Commit.fromFile(givenbranchid);
        String[] parentg = givencommit.getParentIds();
        Queue<String> parentsg = new ArrayDeque<>();
        for (int i = 0; i < parentg.length; i++) {
            parentsg.add(parentg[i]);
        }
        givenancestors.put(givencommit.getCommitId(), 0);
        int distance1 = 1; int distance2 = 1;
        while (parents.size() != 0) {
            ArrayList<String> temp = new ArrayList<>();
            while (parents.size() != 0) {
                String thisparent = parents.poll();
                if (!currentancestors.containsKey(thisparent)) {
                    currentancestors.put(thisparent, distance1);
                }
                for (int i = 0; i < Commit.fromFile(
                        thisparent).getParentIds().length; i++) {
                    temp.add(Commit.fromFile(thisparent).getParentIds()[i]);
                }
            }
            parents.addAll(temp);
            distance1 += 1;
        }
        while (parentsg.size() != 0) {
            ArrayList<String> tempp = new ArrayList<>();
            while (parentsg.size() != 0) {
                String thisparentg = parentsg.poll();
                if (!givenancestors.containsKey(thisparentg)) {
                    givenancestors.put(thisparentg, distance2);
                }
                for (int i = 0; i < Commit.fromFile(
                        thisparentg).getParentIds().length; i++) {
                    tempp.add(Commit.fromFile(thisparentg).getParentIds()[i]);
                }
            }
            parentsg.addAll(tempp);
            distance2 += 1;

        }
        int smallest = Integer.MAX_VALUE;
        String splitcommit = null;
        for (String s:givenancestors.keySet()) {
            if (currentancestors.containsKey(s)
                    && currentancestors.get(s) < smallest) {
                splitcommit = s;
                smallest = currentancestors.get(s);
            }
        }
        return splitcommit;
    }


    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     *
     * @param cmd Name of command you are validating
     * @param args Argument array from command line
     * @param n Number of expected arguments
     */

    public void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            throw new RuntimeException(
                    String.format("Invalid number of arguments for: %s.", cmd));
        }
    }
    /**get the blobid of the File named somefile.
     * @param somefile *
     * @return String *
     */
    public static String blobid(File somefile) {
        String result = Utils.readContentsAsString(somefile);
        String thisblob = Utils.sha1(result);
        return thisblob;
    }
    /** get the addition hashhh map.
     * @return hashhh *
     * */
    public Hashhh getAddition() {
        return addition;
    }
    /** get the removal hashhh map.
     * @return hashhh *
     * */
    public Hashhh getRemoval() {
        return removal;
    }
    /** get the branches hashhh map.
     * @return hashhh *
     * */
    public Hashhh getBranches() {
        return branches;
    }


}
