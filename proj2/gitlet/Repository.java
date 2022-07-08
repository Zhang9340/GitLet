package gitlet;



import java.io.File;
import java.util.*;
import static gitlet.Utils.*;




/** Represents a gitlet repository.
 *  In this repository, it contains all the implementation of the
 *
 *  @author ZhiYuan
 */
public class Repository {

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The staging directory */
    public  File  STAGE_DIR= join(GITLET_DIR,"staging");
    /**The blobs directory  */
    public  File BLOBS_DIR =join (GITLET_DIR,"blobs");
    /**The commits directory */
    public  File COMMITS_DIR = join (GITLET_DIR, "commits");
    /**The branch directory*/
    public File  Branch = join(GITLET_DIR, "branches");
    /**The Head directory */
    public File Head= join(GITLET_DIR, "heads");
    /**The file save the Stage object*/
    public File STAGE= join(STAGE_DIR,"stage");


    /**
     * Creates a new Gitlet version-control system in the current directory.
     * This system will automatically start with one commit: a commit that contains no files and has the commit message initial commit
     * It will have a single branch: master, which initially points to this initial commit, and master will be the current branch.
     * The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970
     *
     */
    public void init(){
        if (GITLET_DIR.exists() && GITLET_DIR.isDirectory()){
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        /* create  directories*/
        GITLET_DIR.mkdir();
        STAGE_DIR.mkdir();
        writeObject(STAGE,new Stage());
        BLOBS_DIR.mkdir();
        COMMITS_DIR.mkdir();
        Branch.mkdir();

       /*Initial commit*/
        Commit commit= new Commit();
        //write commit to file
        writeCommitToFile(commit);
        String id = commit.getId();
        // write the commit id in to the  branch file that the branch currently pointing to
        File master = join(Branch, "master");
        writeContents(master,id);
        // write the branch detail to  the Head file that the head is currently pointing to
        writeContents(Head,"master");



    }

    /**
     *Adds a copy of the file as it currently exists to the staging area (see the description of the commit command).
     * For this reason, adding a file is also called staging the file for addition.
     * Staging an already-staged file overwrites the previous entry in the staging area with the new contents.
     * The staging area should be somewhere in .gitlet.
     * If the current working version of the file is identical to the version in the current commit,
     * do not stage it to be added, and remove it from the staging area if it is already there (as can happen when a file is changed, added,
     * and then changed back to it’s original version).
     * The file will no longer be staged for removal (see gitlet rm), if it was at the time of the command.
     * @param FileName The filename that intends to add for staged.
     */
    public void add(String FileName){
       // check if the file doesn't exit.
        File file= join(CWD,FileName);
      if (!file.exists()){
          System.out.println("File does not exist.");
          System.exit(0);
      }

      // adding the content and id in the blobs and the stage store the filename and the

      Commit headCommit =getCommitFormTheHead();
      Blobs blobs=new Blobs(FileName,CWD);
      Stage stage =readObject(STAGE, Stage.class);
        if (headCommit.getBlobs().containsValue(blobs.getId())){
            stage.getRemoved().remove(FileName);
        }else {
            stage.add(FileName,blobs.getId());
        }

        writeObject(STAGE, stage);
    }

    /**
     *: Saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time, creating a new commit.
     * the commit is said to be tracking the saved files. By default, each commit’s snapshot of files will be exactly the same as its parent commit’s snapshot of files;
     * it will keep versions of files exactly as they are, and not update them.
     * A commit will only update the contents of files it is tracking that have been staged for addition at the time of commit, in which case the commit will now include the version of the file that was staged instead of the version it got from its parent.
     * A commit will save and start tracking any files that were staged for addition but weren’t tracked by its parent.
     * Finally, files tracked in the current commit may be untracked in the new commit as a result being staged for removal by the rm command (below).
     * @param message  The commit message
     */
    public void commit(String message){
       // read from the stage
        Stage stage= readObject(STAGE,Stage.class);
        if (stage.getAdded().isEmpty()&&stage.getRemoved().isEmpty()){
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        if (message.equals("")){
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        Commit parent=getCommitFormTheHead();
        Commit commit =new Commit(message,List.of(parent), stage);
        writeCommitToFile(commit);
        // save the snapshot of the staged file
        Map<String, String> blobs = stage.getAdded();
        blobs.forEach((key, value) -> writeObject(join(BLOBS_DIR, value), new Blobs(key, CWD)));

        // remove the file in the stage
        stage.getAdded().clear();
        stage.getRemoved().clear();

        writeObject(STAGE,stage);
        //change the HEAD and Branch pointer
        String branchesName= readContentsAsString(Head);
        writeContents(join(Branch,branchesName),commit.getId());
    }

    /**
     *Unstage the file if it is currently staged for addition.
     * If the file is tracked in the current commit, stage it for removal and remove the file from the working directory if the user has not already done so
     * (do not remove it unless it is tracked in the current commit).
     * @param filename the file that intends to remove
     */
    public void rm(String filename){

        Stage stage =readObject(STAGE,Stage.class);
        String stagedId= stage.getAdded().getOrDefault(filename,"");
        Commit commit =getCommitFormTheHead();
        String commitId= commit.getBlobs().getOrDefault(filename,"");

        if (stagedId.equals("")&&commitId.equals("")){
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        if (!stagedId.equals("")){
            //remove form the stage
            stage.getAdded().remove(filename);
        }

        if (!commitId.equals("")&&stagedId.equals("")){
            // If the file is tracked in the current commit,
            // stage it for removal and remove the file from the working directory if the user has not already done so
            stage .getRemoved().add(filename);
            restrictedDelete(filename);
        }
        writeObject(STAGE,stage);
    }

    /**
     * Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit,
     * following the first parent commit links, ignoring any second parents found in merge commits.
     * This set of commit nodes is called the commit’s history.
     * For every node in this history, the information it should display is the commit id, the time the commit was made, and the commit message.
     */
    public void log(){
        Commit commit=getCommitFormTheHead();
        while (commit!=null){
            System.out.println(commit);
            if (commit.getParentsId().equals("null")){
                break;
            }

            commit= readObject(join(COMMITS_DIR,commit.getParentsId()),Commit.class);

        }
    }

    /**
     *  Like log, except displays information about all commits ever made. The order of the commits does not matter
     */
    public void global_log(){
        StringBuffer sb = new StringBuffer();
        List<String> filename =plainFilenamesIn(COMMITS_DIR);
        for (String file: filename){
            File commitFile= join(COMMITS_DIR,file);
            Commit commit =readObject(commitFile,Commit.class);
            sb.append(commit.toString());
        }
        System.out.println(sb);
    }

    /**
     *Prints out the ids of all commits that have the given commit message, one per line.
     * @param commitMsg The commit message
     */
    public void  find(String commitMsg){
        StringBuffer sb =new StringBuffer();
        List<String> commitIds =plainFilenamesIn(COMMITS_DIR);
        for (String file : commitIds) {
            File commitFile= join(COMMITS_DIR,file);
            Commit commit =readObject(commitFile,Commit.class);
            if (commit.getMessage().equals(commitMsg)){
               sb.append(commit.getId()).append("\n");
            }

        }
        if (sb.length()==0){
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }

        System.out.println(sb);

    }

    /**
     *Takes the version of the file as it exists in the head commit and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.
     * @param filename The file that intends to check out
     */
    public void checkout(String filename){
        //Takes the version of the file as it exists in the head commit and puts it in the working directory,
       // overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.
       Commit commit =getCommitFormTheHead();
       String blobId=commit.getBlobs().getOrDefault(filename,"");
       if (blobId.equals("")){
           System.out.println("File does not exist in that commit.");
       }else {
          Blobs blobs= getBlobsFromFile(blobId);
          String content =blobs.getContentAsString();
          File checkedFile =join(CWD,filename);
          writeContents(checkedFile,content);
       }
   }

    /**
     *Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.
     * @param filename  the file intends to check out
     * @param commitId The commit id (the version)
     */
    public void  checkout(String filename, String commitId){
        //Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory,
       // overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.
       String commitIdFull=getFullId(commitId);
       File commitFile= join(COMMITS_DIR,commitIdFull);
       if (!commitFile.exists()){
           System.out.println("No commit with that id exists.");
           System.exit(0);
       }
       Commit commit =readObject(commitFile,Commit.class);
       String blobId=commit.getBlobs().getOrDefault(filename,"");
       if (blobId.equals("")){
           System.out.println("File does not exist in that commit.");
       }else {
           Blobs blobs= getBlobsFromFile(blobId);
           String content =blobs.getContentAsString();
           File checkedFile =join(CWD,filename);
           writeContents(checkedFile,content);
       }
   }

    /**
     * Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory, overwriting the versions of the files that are already there if they exist.
     * Also, at the end of this command, the given branch will now be considered the current branch (HEAD).
     * Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
     * The staging area is cleared, unless the checked-out branch is the current branch
     * @param Branchname The branch name
     */
    public void checkout_branch(String Branchname){
        File Branchfile = join(Branch,Branchname);
        if (!Branchfile.exists()){
            System.out.println("No such branch exists.");
            System.exit(0);
        }
       String BranchFromHead = readContentsAsString(Head);
        if (Branchname.equals(BranchFromHead)){
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        //If a working file is untracked in the current branch and would be overwritten by the checkout,
       // print There is an untracked file in the way; delete it, or add and commit it first. and exit

       validUntrackedFile();
       // remove the files in the current CWD and replace it with the file in the commit that this branch points to

       clearStage();
       removeAllFilesInCWD();
       WriteFilesFromCommit(Branchname);
       writeContents(Head, Branchname);
   }

    /**
     * Creates a new branch with the given name, and points it at the current head commit.
     * A branch is nothing more than a name for a reference (a SHA-1 identifier) to a commit node.
     * This command does NOT immediately switch to the newly created branch
     * @param branchName The new branch name
     */
    public void Branch(String branchName){
        File file = join(Branch,branchName);
        if (file.exists()){
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        String BranchFromHead =readContentsAsString(Head);
        String commitsId= readContentsAsString(join(Branch,BranchFromHead));
        writeContents(file,commitsId);

   }

    /**
     *  Deletes the branch with the given name.
     *  his only means to delete the pointer associated with the branch;
     *  it does not mean to delete all commits that were created under the branch, or anything like that.
     * @param branchName The branch that intends to delete
     */
    public void rm_branch(String branchName){
        File file = join(Branch,branchName);
        String currentBranch= readContentsAsString(Head);
        if (!file.exists()){
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (currentBranch.equals(branchName)){
             System.out.println("Cannot remove the current branch.");
             System.exit(0);
        }

       file.delete();

    }

    /**
     *  Displays what branches currently exist, and marks the current branch with a *.
     *  Also displays what files have been staged for addition or removal
     *  Finally displays files that Modified but not Staged for Commit and untracked file in the system
     */
    public void status() {
       if (!GITLET_DIR.exists() || !GITLET_DIR.isDirectory()) {
           System.out.println("Not in an initialized Gitlet directory.");
           System.exit(0);
       }

       StringBuffer output = new StringBuffer();
       output.append("=== Branches ===").append("\n");
       List<String> branches = plainFilenamesIn(Branch);
       List<String> Blobs = plainFilenamesIn(BLOBS_DIR);
       String HeadBranch = readContentsAsString(Head);
       List<String> FileInCWD = plainFilenamesIn(CWD);
        Stage stage = readObject(STAGE, Stage.class);
        Commit commit = getCommitFormTheHead();


       for (String branch : branches) {
           if (HeadBranch.equals(branch)) {
               output.append("*").append(branch).append("\n");
           } else {
               output.append(branch).append("\n");
           }

       }
        output.append("\n");

       output.append("=== Staged Files ===").append("\n");

       stage.getAdded().keySet().stream().sorted().forEach(s -> output.append(s).append("\n"));
       output.append("\n");


       output.append("=== Removed Files ===").append("\n");
       stage.getRemoved().stream().sorted().forEach(s -> output.append(s).append("\n"));
       output.append("\n");


       output.append("=== Modifications Not Staged For Commit ===").append("\n");
       if (FileInCWD!=null){
       for (String filename : FileInCWD) {

           if (commit.getBlobs().containsKey(filename)
                   && !commit.getBlobs().containsValue(new Blobs(filename, CWD).getId())
                   && !stage.getAdded().containsKey(filename)) {
               // Tracked in the current commit, changed in the working directory, but not staged;
               output.append(filename).append("(modified)").append("\n");
           } else if (stage.getAdded().containsKey(filename)
                   && !stage.getAdded().containsValue(new Blobs(filename, CWD).getId())) {
               // Staged for addition, but with different contents than in the working directory;
               output.append(filename).append("(modified)").append("\n");
           }
       }

       Set<String>  trackedByCommit = commit.getBlobs().keySet();
      for (String filename:trackedByCommit) {
          if (stage.getAdded().containsKey(filename)
                  && !join(CWD, filename).exists()){
                   //Staged for addition, but deleted in the working directory
              output.append(filename).append("(deleted)").append("\n");
          }else if (!stage.getRemoved().contains(filename)
                  && commit.getBlobs().containsKey(filename) && !join(CWD, filename).exists()) {
                //Not staged for removal, but tracked in the current commit and deleted from the working directory.
              output.append(filename).append("(deleted)").append("\n");
           }
               
           }
      

      }
          output.append("\n");

       output.append("=== Untracked Files ===").append("\n");
       if (FileInCWD!=null){
           for (String filename : FileInCWD) {
               if (!stage.getAdded().containsKey(filename)&&!commit.getBlobs().containsKey(filename)){
                   output.append(filename).append("\n");
               }else if(stage.getRemoved().contains(filename)&&join(CWD,filename).exists()){
                   output.append(filename).append("\n");
               }
           }
       }

       output.append("\n");
       System.out.println(output);

   }

    /**
     *Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.
     * Also moves the current branch’s head to that commit node.
     * @param commitId The commit id
     */
    public void reset(String commitId){
        String commitIdFull = getFullId(commitId);
        File commitFile= join(COMMITS_DIR,commitIdFull);
        if (!commitFile.exists()){
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        // check if there is any untracked file in cwd

        validUntrackedFile();

        clearStage();
        removeAllFilesInCWD();
        // move the Head pointer to the given commit ID
        String branchesName = readContentsAsString(Head);
        writeContents(join(Branch,branchesName),commitIdFull);

        WriteFilesFromCommit(branchesName);

    }

    public void merge(String branchName){
        Stage stage =readObject(STAGE,Stage.class);
        if (!stage.getAdded().isEmpty()){
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        File BranchFile = join(Branch,branchName);
        if (!BranchFile.exists()){
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchName.equals(readContentsAsString(Head))){
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);

        }
        validUntrackedFile();

        Commit headCommitFromHead = getCommitFormTheHead();
        Commit headCommitFromBranch =getCommitFormBranch(branchName);
        //Find split point of the current branch and the given branch
        Commit splitPoint=getTheSpitPoint(branchName);
        //If the split point is the same commit as the given branch, then we do nothing; the merge is complete.
        if (splitPoint.getId().equals(headCommitFromBranch.getId())){
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        //If the split point is the current branch, then the effect is to check out the given branch
        if (splitPoint.getId().equals(headCommitFromHead.getId())){
            checkout_branch(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        //Get all files
        Set<String> allFiles= new HashSet<>();
        allFiles.addAll(headCommitFromBranch.getBlobs().keySet());
        allFiles.addAll(headCommitFromHead.getBlobs().keySet());
        allFiles.addAll(splitPoint.getBlobs().keySet());

        List<String> rewriteFile= new ArrayList<>();
        List<String> removeFile= new ArrayList<>();
        List<String> conflictFile= new ArrayList<>();

        for (String file:allFiles ) {
            String id_head= headCommitFromHead.getBlobs().getOrDefault(file,"");
            String id_other = headCommitFromBranch.getBlobs().getOrDefault(file,"");
            String id_sp = splitPoint.getBlobs().getOrDefault(file,"");

            if (id_head.equals(id_other) || id_sp.equals(id_other)){
                continue;
            }

            if (id_head.equals(id_sp)){
                if (!id_other.equals("")){
                    //rewrite the file
                    rewriteFile.add(file);
                }else {
                    // remove the file
                    removeFile.add(file);
                }

            }else {
                //conflict
                conflictFile.add(file);
            }
        }


        if (!removeFile.isEmpty()){
            removeFile.forEach(this::rm);
        }
        if (!rewriteFile.isEmpty()){
            for (String file:rewriteFile
                 ) {
                String id_other = headCommitFromBranch.getBlobs().getOrDefault(file,"");
                Blobs blob = getBlobsFromFile(id_other);
                File writefile =join(CWD,blob.getFileName());
                writeContents(writefile,blob.getContent());
                add(blob.getFileName());


            }
        }

        if (!conflictFile.isEmpty()) {
            for (String file : conflictFile) {
                String id_head = headCommitFromHead.getBlobs().getOrDefault(file, "");
                String id_other = headCommitFromBranch.getBlobs().getOrDefault(file, "");
                String headContent = readContentFromBlob(id_head);
                String otherContent = readContentFromBlob(id_other);

                StringBuffer sb = new StringBuffer();
                sb.append("<<<<<<< HEAD\n");
                sb.append(headContent.equals("") ? headContent : headContent + "\n");
                sb.append("=======\n");
                sb.append(otherContent.equals("") ? otherContent : otherContent + "\n");
                sb.append(">>>>>>>\n");

                File filename = join(CWD, file);
                writeContents(filename, sb.toString());
                System.out.println("Encountered a merge conflict.");
            }
        }

            //Commit the new merge
            String message = "Merged " + branchName + " into " + readContentsAsString(Head) + ".";
            List<Commit> parents = List.of(headCommitFromHead, headCommitFromBranch);
            Stage stage2 =readObject(STAGE,Stage.class);
            Commit commit = new Commit(message,parents,stage2);
            writeCommitToFile(commit);
            // save the snapshot of the staged file
            Map<String, String> blobs = stage2.getAdded();
            blobs.forEach((key, value) -> writeObject(join(BLOBS_DIR, value), new Blobs(key, CWD)));

            // remove the file in the stage
            stage2.getAdded().clear();
            stage2.getRemoved().clear();

            writeObject(STAGE,stage2);
            //change the HEAD and Branch pointer
            String branchesName= readContentsAsString(Head);
            writeContents(join(Branch,branchesName),commit.getId());







    }








    /**
     *  Get the current commit that the Head pointer points to
     * @return The commit that pointed by the Head pointer
     */
    private Commit getCommitFormTheHead(){
        //From the Head file get the branch name
        String BranchName = readContentsAsString(Head);
        //From the Branches get the Commits
        String commitsId = readContentsAsString(join(Branch,BranchName));
        File file= join(COMMITS_DIR,commitsId);
        Commit head = readObject(file,Commit.class);
        if (head == null) {
            System.out.println("error! cannot find HEAD!");
            System.exit(0);
        }
        return head;
    }

    /**
     * Write a commit in to the file
     * @param commit The commit
     */
    private  void writeCommitToFile(Commit commit){
        File path = join(COMMITS_DIR,commit.getId());
        writeObject(path,commit);
    }

    /**
     * Write all the file in the working directory that tracked by the  commit that pointed by the given branch
     * @param BranchName  The branch name
     */
    private  void WriteFilesFromCommit(String BranchName){
        String commitsId = readContentsAsString(join(Branch,BranchName));
        File file = join(COMMITS_DIR,commitsId);
        Commit commit = readObject(file,Commit.class);
        Map<String,String> map = commit.getBlobs();
        map.forEach((k,v)->writeContents(join(CWD,k),getBlobsFromFile(v).getContent()));

    }

    /**
     * Get the Blobs object from the given blob id.
     * @param blobId blob id
     * @return The Blobs object
     */
    private Blobs getBlobsFromFile(String blobId){
        File blobfile = join(BLOBS_DIR,blobId);
        return readObject(blobfile,Blobs.class);
    }

    /**
     * Recover the full commit id.
     * @param id The abbreviated id
     * @return Full commit id
     */
    private  String getFullId(String id ){
        if (id.length() == UID_LENGTH){
            return id;
        }
        for (String filename:COMMITS_DIR.list()){
            if (filename.startsWith(id)){
                return filename;
            }
        }
        return null;
    }

    /**
     * validation for any untracked file in the system
     *
     */
    private void validUntrackedFile() {

        if (!getUntrackedFile()) {
            return;
        }

        System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
        System.exit(0);
    }

    /**
     * Get untracked files in the system
     * @return True if there is any untracked files , False if not.
     */
    private boolean getUntrackedFile(){
        List<String> FileInCWD = plainFilenamesIn(CWD);
        Stage stage = readObject(STAGE, Stage.class);
        Commit commit = getCommitFormTheHead();
        if (FileInCWD!=null){
            for (String filename : FileInCWD) {
                if (!stage.getAdded().containsKey(filename) && !commit.getBlobs().containsKey(filename)){
                     return true;
                }else if(stage.getRemoved().contains(filename) && join(CWD,filename).exists()){
                   return true;
                }
            }
        }

        return false;

    }

    /**
     * Clean the stage (Both staged for addition and removal)
     */
    private void clearStage(){
         Stage stage= readObject(STAGE,Stage.class);
         stage.getAdded().clear();
         stage.getRemoved().clear();
         writeObject(STAGE,stage);
     }

    /**
     * Clear all the file in the working directory
     */
    private void removeAllFilesInCWD(){
         List<String> FileDir = plainFilenamesIn(CWD);
         for (String file : FileDir){
             restrictedDelete(file);
         }
     }

    /**
     * Get the commit that certain branch points to
     * @param branchName Branch's name
     * @return The commit that the branch points to
     */
    private  Commit getCommitFormBranch(String branchName){

        String commitId =readContentsAsString(join(Branch,branchName));
        return readObject(join(COMMITS_DIR,commitId),Commit.class);
    }

    private String  readContentFromBlob(String blobId){
        if (blobId.equals("")){
            return "";
        }
        Blobs blobs = getBlobsFromFile(blobId);
        return blobs.getContentAsString();

    }

    private  Set<String> BFS(Commit head){
        Queue<Commit> fringe = new LinkedList<>();
        Set<String> ancestors =new HashSet<>();
        fringe.add(head);
        while (!fringe.isEmpty()){
          Commit commit =  fringe.poll();
          if (!commit.getParents().isEmpty()&&!ancestors.contains(commit.getId())){
              commit.getParents().forEach(a->fringe.add(readObject(join(COMMITS_DIR,a),Commit.class)));

          }
          ancestors.add(commit.getId());
        }
        return ancestors;
    }

    private Commit getTheSpitPoint(String branchName){
        Set<String> ancestorsFormCurrentBranch= BFS(getCommitFormTheHead());
        Queue<Commit> fringe = new LinkedList<>();
        Commit startCommit =getCommitFormBranch(branchName);
        fringe.add(getCommitFormBranch(branchName));
        while (!fringe.isEmpty()){
            Commit spitPoint =fringe.poll();
            if (ancestorsFormCurrentBranch.contains(spitPoint.getId())){
                return spitPoint;
            }
            if (!spitPoint.getParents().isEmpty()){
                spitPoint.getParents().forEach(a->fringe.add(readObject(join(COMMITS_DIR,a),Commit.class)));
            }
        }

        return new Commit();
    }


}
