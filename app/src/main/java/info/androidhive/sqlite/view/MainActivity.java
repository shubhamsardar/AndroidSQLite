package info.androidhive.sqlite.view;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.List;

import info.androidhive.sqlite.R;
import info.androidhive.sqlite.database.DatabaseHelper;
import info.androidhive.sqlite.database.model.Note;
import info.androidhive.sqlite.utils.MyDividerItemDecoration;
import info.androidhive.sqlite.utils.RecyclerTouchListener;
import info.androidhive.sqlite.utils.ShortingType;

public class MainActivity extends AppCompatActivity {
    private NotesAdapter mAdapter;
    private List<Note> notesList = new ArrayList<>();
    private CoordinatorLayout coordinatorLayout;
    private RecyclerView recyclerView;
    private TextView noNotesView;
    private DatabaseHelper db;

    private TextView mApplyFilters;
    private TextView mClearFilters;
    private TextView mFilterPanelToggle;
    private TextView mSortPanelToggle;
    private TextView mNoOfFilterApply;
    private SlidingUpPanelLayout sliderLayout;
    private LottieAnimationView mBookmarkPanelToggle;

    private boolean isApplyFilterPressed;
    boolean isApplySortPressed;
    private int mSortIndex;
    private RadioGroup mSortRadioGroup;

    private Button mBtnApplySorts;
    private Button mBtnClearSorts;
    private View mFilterView, mSortView, mBookmarkView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        coordinatorLayout = findViewById(R.id.coordinator_layout);
        recyclerView = findViewById(R.id.recycler_view);
        noNotesView = findViewById(R.id.empty_notes_view);
        sliderLayout = findViewById(R.id.sliding_layout);
        mFilterPanelToggle = findViewById(R.id.filter);
        mSortPanelToggle = findViewById(R.id.sort);
        mBookmarkPanelToggle = findViewById(R.id.animation_bookmark);
        mFilterView = findViewById(R.id.include_filters);
        mSortView = findViewById(R.id.include_sort);
        mBookmarkView = findViewById(R.id.include_bookmark);
        mSortRadioGroup = findViewById(R.id.radioGroupSort);
        mBtnApplySorts = findViewById(R.id.buttonApplySort);
        mBtnClearSorts = findViewById(R.id.buttonClearSort);

        db = new DatabaseHelper(this);

        notesList.addAll(db.getAllNotes(0));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNoteDialog(false, null, -1);
            }
        });

        mFilterPanelToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFilterView.setVisibility(View.VISIBLE);
                sliderLayout.setScrollableView(findViewById(R.id.scroll_filters));
                sliderLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });

        mSortPanelToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSortView.setVisibility(View.VISIBLE);
                sliderLayout.setScrollableView(findViewById(R.id.scroll_sort));
                sliderLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });

        mBookmarkPanelToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBookmarkView.setVisibility(View.VISIBLE);
                sliderLayout.setScrollableView(findViewById(R.id.rv_bookmarks));
                sliderLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
            }
        });

        mBtnClearSorts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mSortIndex != 0) {
                    mSortRadioGroup.clearCheck();
                    mSortIndex = 0;
                }
                sliderLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });

        mBtnApplySorts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (mSortRadioGroup.getCheckedRadioButtonId()) {
                    case R.id.radioButton1: {
                        mSortIndex = ShortingType.ALPHA_ASSENDING;
                        break;
                    }
                    case R.id.radioButton2: {
                        mSortIndex = ShortingType.ALPHA_DECENDING;
                        break;
                    }
                    case R.id.radioButton4: {
                        mSortIndex = ShortingType.ACCOUNT_TYPE;
                        break;
                    }
                    case R.id.shortby_lastactive: {
                        mSortIndex = ShortingType.LAST_ACTIVE;
                        break;
                    }
                    default: {
                        mSortIndex = ShortingType.DEFAULT;
                        break;
                    }
                }

                if (mSortIndex != 0) {
                    isApplySortPressed = true;
                    sliderLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

                    mSortPanelToggle.setCompoundDrawablesWithIntrinsicBounds(null,
                            null,
                            ContextCompat
                                    .getDrawable(getApplicationContext(),
                                            R.drawable.ic_arrow_drop_up_black_24dp),
                            null);
                }
            }
        });

        sliderLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    if (isApplyFilterPressed) {
                        isApplyFilterPressed = false;

                        //update list
                    }
                    if (isApplySortPressed) {
                        isApplySortPressed = false;
                        //update list
                        notesList.clear();
                        notesList.addAll(db.getAllNotes(mSortIndex));
                        mAdapter.notifyDataSetChanged();

                    }
                    mFilterView.setVisibility(View.GONE);
                    mSortView.setVisibility(View.GONE);
                    mBookmarkView.setVisibility(View.GONE);
                }
            }
        });

        mAdapter = new NotesAdapter(this, notesList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 16));
        recyclerView.setAdapter(mAdapter);

        toggleEmptyNotes();

        /**
         * On long press on RecyclerView item, open alert dialog
         * with options to choose
         * Edit and Delete
         * */
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
            }

            @Override
            public void onLongClick(View view, int position) {
                showActionsDialog(position);
            }
        }));

        FirebaseFirestore.getInstance().collection("notes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                db.clearDatabase();
                notesList.clear();
                mAdapter.notifyDataSetChanged();

                Log.v("OnEvent", "triggered");
                if (queryDocumentSnapshots != null) {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            if (documentSnapshot.exists()) {
                                Note note = documentSnapshot.toObject(Note.class);

                                // inserting note in db and getting
                                // newly inserted note id
                                long id = db.insertNote(note);

                                // get the newly inserted note from db
                                Note n = db.getNote(id);

                                if (n != null) {
                                    // adding new note to array list at 0 position
                                    notesList.add(0, n);

                                    // refreshing the list
                                    mAdapter.notifyDataSetChanged();

                                    toggleEmptyNotes();
                                }

                            }
                        }
                    }
                }


            }
        });
    }

    /**
     * Inserting new note in db
     * and refreshing the list
     */
    private void createNote(String note) {

        //uploading to firestore

        FirebaseFirestore.getInstance().collection("notes").add(new Note(0, note, "timestamp")).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getApplicationContext(), "Uploaded to firestore DB", Toast.LENGTH_LONG).show();
            }
        });


    }

    /**
     * Updating note in db and updating
     * item in the list by its position
     */
    private void updateNote(String note, int position) {
        Note n = notesList.get(position);
        // updating note text
        n.setNote(note);

        // updating note in db
        db.updateNote(n);

        // refreshing the list
        notesList.set(position, n);
        mAdapter.notifyItemChanged(position);

        toggleEmptyNotes();
    }

    /**
     * Deleting note from SQLite and removing the
     * item from the list by its position
     */
    private void deleteNote(int position) {
        // deleting the note from db
        db.deleteNote(notesList.get(position));

        // removing the note from the list
        notesList.remove(position);
        mAdapter.notifyItemRemoved(position);

        toggleEmptyNotes();
    }

    /**
     * Opens dialog with Edit - Delete options
     * Edit - 0
     * Delete - 0
     */
    private void showActionsDialog(final int position) {
        CharSequence colors[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    showNoteDialog(true, notesList.get(position), position);
                } else {
                    deleteNote(position);
                }
            }
        });
        builder.show();
    }


    /**
     * Shows alert dialog with EditText options to enter / edit
     * a note.
     * when shouldUpdate=true, it automatically displays old note and changes the
     * button text to UPDATE
     */
    private void showNoteDialog(final boolean shouldUpdate, final Note note, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.note_dialog, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        final EditText inputNote = view.findViewById(R.id.note);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.lbl_new_note_title) : getString(R.string.lbl_edit_note_title));

        if (shouldUpdate && note != null) {
            inputNote.setText(note.getNote());
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(shouldUpdate ? "update" : "save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show toast message when no text is entered
                if (TextUtils.isEmpty(inputNote.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Enter note!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }

                // check if user updating note
                if (shouldUpdate && note != null) {
                    // update note by it's id
                    updateNote(inputNote.getText().toString(), position);
                } else {
                    // create new note
                    createNote(inputNote.getText().toString());
                }
            }
        });
    }

    /**
     * Toggling list and empty notes view
     */
    private void toggleEmptyNotes() {
        // you can check notesList.size() > 0

        if (db.getNotesCount() > 0) {
            noNotesView.setVisibility(View.GONE);
        } else {
            noNotesView.setVisibility(View.VISIBLE);
        }
    }


}
