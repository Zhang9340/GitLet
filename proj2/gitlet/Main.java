package gitlet;


/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Zhiyuan
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

                repository.init();

                break;
            case "add":

                repository.add(args[1]);
                break;

            case "commit":
                repository.commit(args[1]);
                break;


            case"rm":
                repository.rm(args[1]);
                break;


            case"log":
                repository.log();
                break;

            case"global-log":
                repository.global_log();
                break;

            case "branch":
               repository.Branch(args[1]);
               break;

            case"check":
                repository.getStagedFile();
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
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    repository.checkout(args[2]);

                }else if (len==4) {
                    //java gitlet.Main checkout [commit id] -- [file name]
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    repository.checkout(args[3], args[1]);

                }
                else if  (len==2){
                    repository.checkout_branch(args[1]);

                    }
                break;

            case"status":
                   repository.status();
                   break;
            default :
                System.out.println("No command with that name exists.");
                break;

        }







        }
    }

