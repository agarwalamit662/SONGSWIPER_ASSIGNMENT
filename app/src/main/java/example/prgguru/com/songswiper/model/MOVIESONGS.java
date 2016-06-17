package example.prgguru.com.songswiper.model;

/**
 * Created by amitagarwal3 on 6/16/2016.
 */
public class MOVIESONGS {

    public int mid;
    public int sid;
    public String mname;
    public String sname;
    public String slike;
    public String sdelete;

    public MOVIESONGS(int mid, int sid, String mname, String sname, String slike, String sdelete) {
        this.mid = mid;
        this.sid = sid;
        this.mname = mname;
        this.sname = sname;
        this.slike = slike;
        this.sdelete = sdelete;
    }

    public MOVIESONGS() {
        super();
    }

    public int getMid() {
        return mid;
    }

    public void setMid(int mid) {
        this.mid = mid;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public String getMname() {
        return mname;
    }

    public void setMname(String mname) {
        this.mname = mname;
    }

    public String getSname() {
        return sname;
    }

    public void setSname(String sname) {
        this.sname = sname;
    }

    public String getSlike() {
        return slike;
    }

    public void setSlike(String slike) {
        this.slike = slike;
    }

    public String getSdelete() {
        return sdelete;
    }

    public void setSdelete(String sdelete) {
        this.sdelete = sdelete;
    }
}
