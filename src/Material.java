
import java.net.URI;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

/*
 * Copyright (C) 2015 Victor Anuebunwa
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 *
 * @author Victor Anuebunwa
 */
public class Material implements Comparable<Material> {

    private MaterialType type;
    private boolean excluded;
    private String preview;
    private String title;
    private String ISBN;
    private URI path;
    private String author;
    private String[] keywords;
    private Date dateAdded;
    private Date lastModificationTime;
    private boolean modified;
    private boolean synced;

    /**
     * Convenient for creating a material from local library
     *
     * @param path
     */
    public Material(URI path) {
        this.path = path;
        //Set Defaults
        this.ISBN = "";
        this.type = MaterialType.EBOOK;
    }

    /**
     *
     * @return true if material has been synchronised with copy in database,
     * false otherwise
     */
    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public MaterialType getType() {
        return type;
    }

    public void setExcluded(boolean excluded) {
        this.excluded = excluded;
        noteChanges();
    }

    public boolean isExcluded() {
        return excluded;
    }

    public String getPreview() {
        return preview;
    }

    public String getTitle() {
        return title;
    }

    public URI getPath() {
        return path;
    }

    public String getAuthor() {
        return author;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public Date getLastModificationTime() {
        return lastModificationTime;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
        noteChanges();
    }

    public void setLastModificationTime(Date lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
        modified = true;
    }

    public void setType(MaterialType type) {
        this.type = type;
        noteChanges();
    }

    public void setPreview(String preview) {
        this.preview = preview;
        noteChanges();
    }

    public void setTitle(String title) {
        this.title = title;
        noteChanges();
    }

    public void setPath(URI path) {
        this.path = path;
        noteChanges();
    }

    public void setAuthor(String author) {
        this.author = author;
        noteChanges();
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
        noteChanges();
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
        noteChanges();
    }

    private void noteChanges() {
        lastModificationTime = Date.from(Instant.now());
        modified = true;
    }

    /**
     * This causes this material to ignore noting previous changes.<br/>
     * This can be used to prevent this material from being re-indexed
     */
    public void suppressNotedChanges() {
        this.modified = false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Material) {
            Material otherMaterial = (Material) obj;
            return this.path.equals(otherMaterial.getPath());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.path);
        return hash;
    }

    @Override
    public String toString() {
        if (title == null) {
            title = "";
        }
        return title + ((author == null || author.isEmpty()) ? "" : " by " + author);
    }

    @Override
    public int compareTo(Material otherMaterial) {
        if (title == null) {
            title = "";
        }
        return title.compareTo(otherMaterial.title);
    }

}
