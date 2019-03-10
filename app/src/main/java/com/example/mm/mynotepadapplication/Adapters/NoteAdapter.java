package com.example.mm.mynotepadapplication.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.mm.mynotepadapplication.Callbacks.NoteEventListener;
import com.example.mm.mynotepadapplication.Model.Note;
import com.example.mm.mynotepadapplication.R;
import com.example.mm.mynotepadapplication.Utils.NoteUtils;

import java.util.ArrayList;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter <NoteAdapter.NoteHolder>{
    private Context mContext;
    private ArrayList <Note> notes;
    private NoteEventListener listener;
    private boolean multiCheckMode = false;

    public NoteAdapter(Context mContext, ArrayList<Note> notes) {
        this.mContext = mContext;
        this.notes = notes;
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteHolder(LayoutInflater.from(mContext).inflate(R.layout.note_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {

        final Note note = notes.get(position);
        if (note != null){
        holder.noteText.setText(note.getNoteText());
        holder.noteDate.setText(NoteUtils.dateFromeString(note.getNoteDate()));

         // init note click event
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onNoteClick(note);
            }
        });

         // init note long click
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onNoteLongClick(note);
                return false;
            }
        });

            // check checkBox if note selected
            if (multiCheckMode) {
                holder.checkBox.setVisibility(View.VISIBLE); // show checkBox if multiMode on
                holder.checkBox.setChecked(note.isChecked());
            } else holder.checkBox.setVisibility(View.GONE); // hide checkBox if multiMode off
        }

    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public Note getNote (int position){
        return notes.get(position);
    }

    public List<Note> getCheckedNotes() {
        List<Note> checkedNotes = new ArrayList<>();
        for (Note n : this.notes) {
            if (n.isChecked())
                checkedNotes.add(n);
        }

        return checkedNotes;
    }

    class NoteHolder extends RecyclerView.ViewHolder{
        TextView noteText, noteDate;
        CheckBox checkBox;

        public NoteHolder(View itemView) {
            super(itemView);
            noteText =  itemView.findViewById(R.id.note_text);
            noteDate =  itemView.findViewById(R.id.note_data);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }

    public void setListener (NoteEventListener listener){
        this.listener = listener;
    }

    public void setMultiCheckMode(boolean multiCheckMode) {
        this.multiCheckMode = multiCheckMode;
        if (!multiCheckMode)
            for (Note note : this.notes) {
                note.setChecked(false);
            }
        notifyDataSetChanged();
    }
}
