package gitlet;


import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class Stage implements Serializable {
    // filename blobsID;
    private Map<String, String> added;
    private Set<String> removed;

    public Stage(){
       this.added= new ConcurrentHashMap<>();
       this.removed=new HashSet<>();
    }


    public void add(String fileName, String BlobsId){
          added.put(fileName,BlobsId);
    }
    public void remove(String filename){
        removed.add(filename);
    }
    public Map<String, String> getAdded() {
        return added;
    }

    public Set<String> getRemoved() {
        return removed;
    }

    public ArrayList<String> getAllStagedFile(){
        ArrayList<String> res = new ArrayList<>();
        res.addAll(added.keySet());
        res.addAll(removed);
        return res;
    }
}
