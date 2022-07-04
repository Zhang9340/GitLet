package gitlet;


import java.io.File;
import java.sql.Blob;
import java.util.*;
import java.util.stream.Stream;


import static gitlet.Utils.*;


// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author ZHiYUAN
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

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
    /** The file that saves the commits object */
    public File commits;
    /**The file that saves  blobs object  */
    public File blobs;
    /**The skeleton of the gitlet Repository
     * .gitlet
     *    ->STAGE_DIR
     *    ->BLOBS_DIR
     *       ->blobs [filename]
     *    ->COMMITS_DIR
     *       ->commits [commits id]
     *    -> Head->branch(the file save the content that shows which branch the Head is pointing to)
     *    ->Branch_DIR
     *      ->master branches(the file save the content that the current branch the commit pointing to)
     *      ->other branches
     *
     *      */
    /* TODO: fill in the rest of this class. */
  /* init operation */
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
    // add the file to staging area and copy the content in to the blobs
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
      Map<String,String> stageAddedFile=stage.getAdded();
      // from the commit that the head pointer points to get the blobId for the given filename
      String headCommitBlobId=headCommit.getBlobs().getOrDefault(FileName, "");
      // find dulplicate files
//      if (Objects.equals(blobs.getId(), headCommitBlobId)
//              || checkDuplicateFile(stageAddedFile,file) ){
//          System.exit(0);
//      }else if (stage.getRemoved().contains(FileName)){
//          // Status with a removal followed by an add that restores former contents.
//          // Should simply "unremove" the file without staging.
//               stage.getRemoved().remove(FileName);
//
//      }else{
//          //write stage and blobs object
//          stage.add(FileName, blobs.getId());
//      }
//        writeObject(STAGE, stage);


        if (headCommit.getBlobs().containsValue(blobs.getId())){
            stage.getRemoved().remove(FileName);
        }else {
            stage.add(FileName,blobs.getId());
        }

        writeObject(STAGE, stage);
    }

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
        Commit commit =new Commit(message,parent, stage);
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

    public void rm(String filename){
        File file= join(CWD,filename);
        Stage stage =readObject(STAGE,Stage.class);
        String stageid= stage.getAdded().getOrDefault(filename,"");
        Commit commit =getCommitFormTheHead();
        String commitid= commit.getBlobs().getOrDefault(filename,"");

        if (stageid.equals("")&&commitid.equals("")){
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        if (!stageid.equals("")){
            //remove form the stage
            stage.getAdded().remove(filename);
        }

        if (!commitid.equals("")&&stageid.equals("")){
            // If the file is tracked in the current commit,
            // stage it for removal and remove the file from the working directory if the user has not already done so
            stage .getRemoved().add(filename);
            restrictedDelete(filename);
        }
        writeObject(STAGE,stage);
    }

    public void log(){
        Commit commit=getCommitFormTheHead();
        while (commit!=null){
            System.out.println(commit);
            if (commit.getParentsId()==null){
                break;
            }
            commit= readObject(join(COMMITS_DIR,commit.getParentsId()),Commit.class);
        }
    }

    public void global_log(){
        StringBuffer sb = new StringBuffer();
        List<String> filename =plainFilenamesIn(COMMITS_DIR);
        for (String file: filename){
            File commitfile= join(COMMITS_DIR,file);
            Commit commit =readObject(commitfile,Commit.class);
            sb.append(commit.toString());
        }
        System.out.println(sb);
    }
    // java gitlet.Main checkout -- [file name]
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
    // java gitlet.Main checkout [commit id] -- [file name]
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

    public void checkout_branch(String Branchname){
        File Branchfile =join(Branch,Branchname);
        if (!Branchfile.exists()){
            System.out.println("No such branch exists.");
            System.exit(0);
        }
       String BranchFromHead =readContentsAsString(Head);
        if (Branchname.equals(BranchFromHead)){
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        //If a working file is untracked in the current branch and would be overwritten by the checkout,
       // print There is an untracked file in the way; delete it, or add and commit it first. and exit
       Commit commit =getCommitFormTheHead();
       validUntrackedFile(commit.getBlobs());
       // remove the files in the current CWD and replace it with the file in the commit that this branch points to

       clearStage();
       removeAllFilesInCWD();
       WriteFilesFromCommit(Branchname);
       writeContents(Head, Branchname);
   }

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



       for (String branch : branches) {
           if (HeadBranch.equals(branch)) {
               output.append("*").append(branch).append("\n");
           } else {
               output.append(branch).append("\n");
           }
           output.append("\n");
       }
       Stage stage = readObject(STAGE, Stage.class);
       Commit commit = getCommitFormTheHead();
       output.append("=== Staged Files ===").append("\n");

       stage.getAdded().keySet().stream().sorted().forEach(s -> output.append(s).append("\n"));
       output.append("\n");


       output.append("=== Removed Files ===").append("\n");
       stage.getRemoved().stream().sorted().forEach(s -> output.append(s).append("\n"));
       output.append("\n");


       output.append("=== Modifications Not Staged For Commit ===").append("\n");
//       if (FileInCWD!=null){
//       for (String filename : FileInCWD) {
//
//           if (commit.getBlobs().containsKey(filename)
//                   && commit.getBlobs().containsValue(new Blobs(filename, CWD).getId())
//                   && !stage.getAdded().containsKey(filename)) {
//               // Tracked in the current commit, changed in the working directory, but not staged;
//               output.append(filename).append("(modified)").append("\n");
//           } else if (stage.getAdded().containsKey(filename)
//                   && !stage.getAdded().containsValue(new Blobs(filename, CWD).getId())) {
//               // Staged for addition, but with different contents than in the working directory;
//               output.append(filename).append("(modified)").append("\n");
//           } else if (stage.getAdded().containsKey(filename)
//                   && !join(CWD, filename).exists()) {
//               //Staged for addition, but deleted in the working directory
//               output.append(filename).append("(deleted)").append("\n");
//
//           } else if (!stage.getRemoved().contains(filename)
//                   && commit.getBlobs().containsKey(filename) && !join(CWD, filename).exists()) {
//               //Not staged for removal, but tracked in the current commit and deleted from the working directory.
//               output.append(filename).append("(deleted)").append("\n");
//           }
//
//
//       }
//      }
          output.append("\n");

       output.append("=== Untracked Files ===").append("\n");
//       if (FileInCWD!=null){
//           for (String filename : FileInCWD) {
//               if (!stage.getAdded().containsKey(filename)&&commit.getBlobs().containsKey(filename)){
//                   output.append(filename).append("\n");
//               }else if(stage.getRemoved().contains(filename)&&join(CWD,filename).exists()){
//                   output.append(filename).append("\n");
//               }
//           }
//       }


       output.append("\n");
       System.out.println(output);

   }




    private Commit getCommitFormTheHead(){
        //From the Head file get the branch name
        String BranchName =readContentsAsString(Head);
        //From the Branches get the Commits
        String commitsId = readContentsAsString(join(Branch,BranchName));
        File file= join(COMMITS_DIR,commitsId);
        Commit head =readObject(file,Commit.class);
        if (head==null) {
            System.out.println("error! cannot find HEAD!");
            System.exit(0);
        }
        return head;
    }

    private  void writeCommitToFile(Commit commit){
        File path = join(COMMITS_DIR,commit.getId());
        writeObject(path,commit);
    }

    private  void WriteFilesFromCommit(String BranchName){
        String commitsId =readContentsAsString(join(Branch,BranchName));
        File file =join(COMMITS_DIR,commitsId);
        Commit commit =readObject(file,Commit.class);
        Map<String,String> map=commit.getBlobs();
        map.forEach((k,v)->writeContents(join(CWD,k),getBlobsFromFile(v).getContent()));

    }

    private Blobs getBlobsFromFile(String blobId){
        File blobfile =join(BLOBS_DIR,blobId);
        return readObject(blobfile,Blobs.class);
    }

    private  String getFullId(String id ){
        if (id.length()==UID_LENGTH){
            return id;
        }
        for (String filename:COMMITS_DIR.list()){
            if (filename.startsWith(id)){
                return filename;
            }
        }
        return null;
    }

    private void validUntrackedFile(Map<String, String> blobs) {
        List<String> untrackedFiles = getUntrackedFile();
        if (untrackedFiles.isEmpty()) {
            return;
        }

        for (String filename : untrackedFiles) {
            String blobId = new Blobs(filename, CWD).getId();
            String otherId = blobs.getOrDefault(filename, "");
            if (!otherId.equals(blobId)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    private List<String> getUntrackedFile(){
        List<String> res = new ArrayList<>();
        Stage stage =readObject(STAGE,Stage.class);
        ArrayList<String>  stageFiles= stage.getAllStagedFile();
        Set<String> HeadFiles= getCommitFormTheHead().getBlobs().keySet();
        for (String filename : plainFilenamesIn(CWD)) {
            // file that not track by stage and the current commit
            if (!stageFiles.contains(filename) && !HeadFiles.contains(filename)) {
                res.add(filename);
            }
        }
        Collections.sort(res);
        return res;

    }


     private void clearStage(){
         Stage stage= readObject(STAGE,Stage.class);
         stage.getAdded().clear();
         stage.getRemoved().clear();
     }
     private void removeAllFilesInCWD(){
         List<String> FileDir=plainFilenamesIn(CWD);
         for (String file : FileDir){
             restrictedDelete(file);
         }
     }


    private boolean checkDuplicateFile(Map<String,String> map,File filename){
       return map.keySet().stream().anyMatch(a->join(CWD,a).equals(filename));

    }
    public  void  getStagedFile(){
        Stage stage =readObject(STAGE,Stage.class);
        System.out.println(stage.getAdded());
        System.out.println(stage.getRemoved());
    }
    public void getTrackedFileFromCurrentcommit(){
        String branch =readContentsAsString(Head);
        String CommitId= readContentsAsString(join(Branch,branch));
        Commit commit= readObject(join(COMMITS_DIR,CommitId),Commit.class);
        System.out.println(commit.getBlobs());
        System.out.println(commit.getMessage());
    }





}
