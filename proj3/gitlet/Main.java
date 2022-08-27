package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Xuanyi Zhang
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        CommandClass instance = new CommandClass();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        switch (args[0]) {
        case "init":
            instance.init();
            break;
        case "add":
            instance.add(args);
            break;
        case "commit":
            instance.commit(args);
            break;
        case "log":
            instance.log();
            break;
        case "checkout":
            instance.checkout(args);
            break;
        case "rm":
            instance.rm(args);
            break;
        case "global-log":
            instance.globallog();
            break;
        case "find":
            instance.find(args);
            break;
        case "status":
            if (CommandClass.GITLET_FOLDER.exists()) {
                instance.status();
                break;
            } else {
                System.out.println("Not in an initialized Gitlet directory.");
                System.exit(0);
                break;
            }
        case "branch":
            instance.branch(args);
            break;
        case "rm-branch":
            instance.rmbranch(args);
            break;
        case "reset":
            instance.reset(args);
            break;
        case "merge":
            instance.merge(args);
            break;
        default:
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
        return;
    }

}
