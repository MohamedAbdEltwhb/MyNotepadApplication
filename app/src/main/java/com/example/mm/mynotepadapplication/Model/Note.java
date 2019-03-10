package com.example.mm.mynotepadapplication.Model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity (tableName = "notes")
public class Note {

    @PrimaryKey (autoGenerate = true)
    private int id;

    @ColumnInfo (name = "text")
    private String noteText;

    @ColumnInfo (name = "date")
    private Long noteDate;

    @Ignore // we don't want to store this value on database so ignore it
    private boolean checked = false;

    public Note() {
    }

    public Note(String noteText, Long noteDate) {

        this.noteText = noteText;
        this.noteDate = noteDate;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public Long getNoteDate() {
        return noteDate;
    }

    public void setNoteDate(Long noteDate) {
        this.noteDate = noteDate;
    }

    public int getId() {
        return id;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isChecked() {
        return checked;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", noteDate=" + noteDate +
                '}';
    }
}
