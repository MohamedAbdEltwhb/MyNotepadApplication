package com.example.mm.mynotepadapplication;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mm.mynotepadapplication.Adapters.NoteAdapter;
import com.example.mm.mynotepadapplication.Callbacks.MainActionModeCallback;
import com.example.mm.mynotepadapplication.Callbacks.NoteEventListener;
import com.example.mm.mynotepadapplication.Model.Note;
import com.example.mm.mynotepadapplication.Utils.NoteUtils;
import com.example.mm.mynotepadapplication.db.NotesDB;
import com.example.mm.mynotepadapplication.db.NotesDao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.mm.mynotepadapplication.EditeNoteActivity.NOTE_EXTRA_Key;

public class MainActivity extends AppCompatActivity implements NoteEventListener {

    private static final String TAG = "MainActivity";
    private RecyclerView mRecyclerView;
    private ArrayList<Note> notes;
    private NoteAdapter adapter;
    private NotesDao dao;
    private MainActionModeCallback actionModeCallback;
    private FloatingActionButton fab;
    private int chackedCount = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // init recyclerView
        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // init fab Button
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddNewNote();
            }
        });

        dao = NotesDB.getInstance(this).notesDao();
    }

    private void loadNotes() {
        this.notes = new ArrayList<>();
        List<Note> list = dao.getNotes();// get All notes from DataBase
        this.notes.addAll(list);
        this.adapter = new NoteAdapter(this, this.notes);
        // set listener to adapter
        this.adapter.setListener(this);
        this.mRecyclerView.setAdapter(adapter);
        showEmptyView();
        // add swipe helper to recyclerView

        swipeToDeleteHelper.attachToRecyclerView(mRecyclerView);
    }


    public void onAddNewNote() {
        startActivity(new Intent(this, EditeNoteActivity.class));
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        loadNotes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNoteClick(Note note) {
        Log.d(TAG, "onNoteClick" + note.toString());

        Intent edit = new Intent(MainActivity.this, EditeNoteActivity.class);
        edit.putExtra(NOTE_EXTRA_Key, note.getId());
        startActivity(edit);
    }

    @Override
    public void onNoteLongClick(Note note) {

        note.setChecked(true);
        chackedCount = 1;
        adapter.setMultiCheckMode(true);

        // set new listener to adapter intend off MainActivity listener that we have implement
        adapter.setListener(new NoteEventListener() {
            @Override
            public void onNoteClick(Note note) {
                note.setChecked(!note.isChecked()); // inverse selected
                if (note.isChecked())
                    chackedCount++;
                else chackedCount--;

                if (chackedCount > 1) {
                    actionModeCallback.changeShareItemVisible(false);
                } else actionModeCallback.changeShareItemVisible(true);

                if (chackedCount == 0) {
                    //  finish multi select mode wen checked count =0
                    actionModeCallback.getAction().finish();
                }

                actionModeCallback.setCount(chackedCount + "/" + notes.size());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onNoteLongClick(Note note) {

            }
        });

        actionModeCallback = new MainActionModeCallback() {
            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_delete_notes)
                    onDeleteMultiNotes();
                else if (menuItem.getItemId() == R.id.action_share_note)
                    onShareNote();

                actionMode.finish();
                return false;
            }

        };

        // start action mode
        startActionMode(actionModeCallback);
        // hide fab button
        fab.setVisibility(View.GONE);
        actionModeCallback.setCount(chackedCount + "/" + notes.size());
    }

    // swipe to right or to left te delete
    private ItemTouchHelper swipeToDeleteHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            if (notes != null) {
                // get swiped note
                Note swipedNote = notes.get(viewHolder.getAdapterPosition());
                if (swipedNote != null) {
                    swipeToDelete(swipedNote, viewHolder);
                }
            }
        }
    });

    private void swipeToDelete(final Note swipedNote, final RecyclerView.ViewHolder viewHolder) {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.delete_dialoge);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final Button deleteButton = (Button) dialog.findViewById(R.id.delete_btn);
        final Button cancelButton = (Button) dialog.findViewById(R.id.cancel_btn);


        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                dao.deleteNote(swipedNote);
                notes.remove(swipedNote);
                adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                showEmptyView();
                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);

//        new AlertDialog.Builder(MainActivity.this)
//                .setMessage("Delete Note?")
//                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dao.deleteNote(swipedNote);
//                        notes.remove(swipedNote);
//                        adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
//                        showEmptyView();
//                    }
//                })
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        mRecyclerView.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());
//                    }
//                })
//                .setCancelable(false)
//                .create()
//                .show();

    }

    /**
     * when no notes show msg in main_layout
     */
    private void showEmptyView() {
        if (notes.size() == 0) {
            this.mRecyclerView.setVisibility(View.GONE);
            findViewById(R.id.empty_notes_view).setVisibility(View.VISIBLE);

        } else {
            this.mRecyclerView.setVisibility(View.VISIBLE);
            findViewById(R.id.empty_notes_view).setVisibility(View.GONE);
        }
    }

    private void onDeleteMultiNotes() {
        // TODO: 22/07/2018 delete multi notes

        List<Note> chackedNotes = adapter.getCheckedNotes();
        if (chackedNotes.size() != 0) {
            for (Note note : chackedNotes) {
                dao.deleteNote(note);
            }
            // refresh Notes
            loadNotes();
            Toast.makeText(this, chackedNotes.size() + " Note(s) Delete successfully !", Toast.LENGTH_SHORT).show();
        } else Toast.makeText(this, "No Note(s) selected", Toast.LENGTH_SHORT).show();

        //adapter.setMultiCheckMode(false);
    }

    private void onShareNote() {
        Note note = adapter.getCheckedNotes().get(0);
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        String notetext = note.getNoteText() + "\n\n Create on : " +
                NoteUtils.dateFromeString(note.getNoteDate()) + "\n  By :" +
                getString(R.string.app_name);
        share.putExtra(Intent.EXTRA_TEXT, notetext);
        startActivity(share);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);

        adapter.setMultiCheckMode(false); // uncheck the notes
        adapter.setListener(this); // set back the old listener
        fab.setVisibility(View.VISIBLE);
    }
}
