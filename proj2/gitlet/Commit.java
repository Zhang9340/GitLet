package gitlet;

// TODO: any imports you need here

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author ZhiYuan Zhang
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private Date  timeStamp;
    private String parents;
    private String  id;
    /* The files that this commit track
    * filename
    * blobs id*/
    private HashMap<String, String> blobs;

    /* TODO: fill in the rest of this class. */
    public Commit(){
        this.message= "initial commit";
        this.timeStamp= new Date(0);
        this.blobs=new HashMap<>();
        this.id=Utils.sha1(message,timeStamp.toString());
    }

    public Commit(String message, Commit parents,Stage stage){
        this.message=message;
        this.parents=parents.getId();
        this.timeStamp= new Date();


        this.blobs=parents.getBlobs();

        blobs.putAll(stage.getAdded());

        this.id=Utils.sha1(message,timeStamp.toString(),parents.toString(),blobs.toString());

    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }
    public String getTimeStamp() {
        DateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        return df.format(timeStamp);
    }
    public String getId() {return id;}
    public HashMap<String, String> getBlobs() {return blobs;}

    public String getParentsId() {
        return parents;
    }
    @Override
    public String toString(){
        return "==="+"\n"
                +"commit "+id +"\n"
                +"Date: " +getTimeStamp()+"\n"
                +getMessage()+"\n";
    }
}
