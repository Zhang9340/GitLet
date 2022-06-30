package gitlet;


/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        String firstArg = args[0];
        Repository repository = new Repository();
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                repository.init();

                break;
            case "add":
                // TODO: handle the `add [filename]` command
                repository.add(args[1]);
                break;
            // TODO: FILL THE REST IN
            //  handle the commit [message]
            case "commit":
                repository.commit(args[1]);
                break;

            case "check":
                repository.readfromStage();
                repository.getTrackedFileFromCurrentcommit();
                break;


            case"rm":
                repository.rm(args[1]);
                break;


            case"log":
                repository.log();
                break;

            case"checkout":
                int len = args.length;  // 2 3 4
                if (len < 2 || len > 4) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }

                if (len == 3) {
                    // java gitlet.Main checkout -- [file name]
                    if (!args[1].equals("--")){
                        System.out.println("invalid operand");
                    }
                    repository.checkout(args[2]);
                    break;
                }else if (len==4){
                    //java gitlet.Main checkout [commit id] -- [file name]
                    if (!args[2].equals("--")){
                        System.out.println("invalid operand");
                    }
                    repository.checkout(args[3],args[1]);
                    break;

                }



        }
    }
}
