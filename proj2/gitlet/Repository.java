package gitlet;









import java.io.File;
import java.util.*;

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
     *    ->HEAD
     *      -> Head->branch(the file save the content that shows which branch the Head is pointing to)
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
      String stageBlobId=stage.getAdded().getOrDefault(FileName,"0");
      // from the commit that the head pointer points to get the blobId for the given filename
      String headCommitBlobId=headCommit.getBlobs().getOrDefault(FileName, "");
      if (Objects.equals(blobs.getId(), headCommitBlobId)){
          System.exit(0);
      }else {

          //write stage and blobs object
          stage.add(FileName, blobs.getId());
          File BlobsFile = join(BLOBS_DIR, blobs.getId());
          writeObject(BlobsFile, blobs);
          writeObject(STAGE, stage);
      }




    }
    public void commit(String message){
       // read from the stage
        Stage stage= readObject(STAGE,Stage.class);
        if (stage.getAdded().isEmpty()){
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
        //change the HEAD and Branch pointer
        String branchesName= readContentsAsString(Head);
        writeContents(join(Branch,branchesName),commit.getId());
        //TODO: remove the file in the stage

        Iterator<String> iterator = stage.getAdded().keySet().iterator();
        while (iterator.hasNext()) {

            rm(iterator.next());
        }


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
            stage.getAdded().remove(filename);


        }else {
            stage .getRemoved().add(filename);
        }
        if (!commitid.equals("")){
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
       File commitFile= join(COMMITS_DIR,commitId);
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
           byte[] content =blobs.getContent();
           File checkedFile =join(CWD,filename);
           writeContents(checkedFile,content);
       }


   }

    private Commit getCommitFormTheHead(){
        //From the Head file get the branch name
        String BranchName =readContentsAsString(Head);
        //From the Branches get the Commits
        String commitsId = readContentsAsString(join(Branch,BranchName));
        File file= join(COMMITS_DIR,commitsId);
        Commit head =readObject(file,Commit.class);
        if (head==null){
            System.out.println("error! cannot find HEAD!");
            System.exit(0);
        }
        return head;
    }
    private  void writeCommitToFile(Commit commit){
        File path = join(COMMITS_DIR,commit.getId());
        writeObject(path,commit);
    }

    private Blobs getBlobsFromFile(String blobId){
        File blobfile =join(BLOBS_DIR,blobId);
        Blobs blobs= readObject(blobfile,Blobs.class);
        return blobs;
    }




    public void readfromStage(){
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
