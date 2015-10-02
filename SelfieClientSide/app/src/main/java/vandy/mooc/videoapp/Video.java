package vandy.mooc.videoapp;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by i on 15.07.15.
 */
public class Video {

    private long id;
    private String name;
    private String url;
    private long duration;
    private String owner;
    private long likes = 0;
    public Set<String> liked = new HashSet<String>();

    public Video(String name,  long duration, String url) {
        this.name = name;
        this.url = url;
        this.duration = duration;
    }
    public Video (){}


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public Set<String> getLiked() {
        return liked;
    }

    public void setLiked(Set<String> liked) {
        this.liked = liked;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Video video = (Video) o;

        if (duration != video.duration) return false;
        if (name != null ? !name.equals(video.name) : video.name != null) return false;
        return !(url != null ? !url.equals(video.url) : video.url != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Video"+ id +
                "(name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", duration=" + duration +
                ", owner='" + owner + '\'' +
                ", likes=" + likes +
                ", liked=" + liked +
                ')';
    }
}
