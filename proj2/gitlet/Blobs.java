package gitlet;


import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static gitlet.Utils.*;

public class Blobs implements Serializable {
    private
    String fileName;
    private byte[] content;
    private String id;
    //filename blobsId

    public Blobs(String fileName, File CWD){
        this.fileName=fileName;
        File file = join(CWD,fileName);
        if (file.exists()){
            content=readContents(file);
            id=sha1(fileName,content);
        }else{
            content=null;
            id= sha1(fileName, null);
        }

    }



    public String getId() {
        return id;
    }

    public byte[] getContent() {
        return content;
    }
    public String getContentAsString(){
        return new String(content, StandardCharsets.UTF_8);
    }

    public String getFileName() {
        return fileName;
    }
}
