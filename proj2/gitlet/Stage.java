package gitlet;


import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



public class Stage implements Serializable {
    // filename blobsID;
    private Map<String, String> added;
    private Set<String> removed;

    public Stage(){
       this.added= new HashMap<>();
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
}
