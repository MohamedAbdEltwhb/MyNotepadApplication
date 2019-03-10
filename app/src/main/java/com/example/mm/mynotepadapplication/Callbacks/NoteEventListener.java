package com.example.mm.mynotepadapplication.Callbacks;

import com.example.mm.mynotepadapplication.Model.Note;

public interface NoteEventListener {

    // call wen note clicked.
    void onNoteClick(Note note);


    // call wen long Click to note.
    void onNoteLongClick(Note note);
}
