/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.demo.notepad3;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NoteEdit extends Activity {

    private EditText mTitleText;
    private EditText mBodyText;
    private Long mRowId;
    private NotesDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	//INSTRUMENTED
    	Supervisor.logCall("onCreate",this);
    	Supervisor.logActivityCreated(this);

        super.onCreate(savedInstanceState);
        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.note_edit);
        setTitle(R.string.edit_note);

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        //ORIGINAL 001
        //mRowId = (savedInstanceState == null) ? null :
        //   (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        
        //INSTRUMENTED 001
        if (savedInstanceState == null){
        	Supervisor.logTrue("onCreate",this);
        	mRowId = null;
        }
        else{
        	Supervisor.logFalse("onCreat",this);
        	mRowId = (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        }
        
        
		if (mRowId == null) {
			//INSTRUMENTED
			Supervisor.logTrue("onCreate",this);
			
			Bundle extras = getIntent().getExtras();
			mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
									: null;
		}

		populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
            	//INSTRUMENTED
            	Supervisor.logCall("onClick",this);
            	
                setResult(RESULT_OK);
                
                //INSTRUMENTED
                Supervisor.logReturn("onClick",this);
                finish();
            }

        });
        
        //INSTRUMENTED
    	Supervisor.logReturn("onCreate",this);
    }

    private void populateFields() {
    	//INSTRUMENTED
    	Supervisor.logCall("onPopulateFields",this);

        if (mRowId != null) {
        	//INSTRUMENTED
        	Supervisor.logTrue("onPopulateFields",this);

            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            mTitleText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
            mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
        }
        
        //INSTRUMENTED
    	Supervisor.logReturn("onPopulateFields",this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	//INSTRUMENTED
    	Supervisor.logCall("onSaveInstanceState",this);

        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(NotesDbAdapter.KEY_ROWID, mRowId);
        
    	//INSTRUMENTED
    	Supervisor.logReturn("onSaveInstanceState",this);
    }

    @Override
    protected void onPause() {
    	//INSTRUMENTED
    	Supervisor.logCall("onPause",this);
    	
        super.onPause();
        saveState();
        
    	//INSTRUMENTED
    	Supervisor.logReturn("onPause",this);
    }

    @Override
    protected void onResume() {
    	//INSTRUMENTED
    	Supervisor.logCall("onResume",this);

        super.onResume();
        populateFields();
        
    	//INSTRUMENTED
    	Supervisor.logReturn("onResume",this);
    }

    private void saveState() {
    	//INSTRUMENTED
    	Supervisor.logCall("saveState",this);

        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();

        if (mRowId == null) {
        	//INSTRUMENTED
        	Supervisor.logTrue("saveState",this);

            long id = mDbHelper.createNote(title, body);
            if (id > 0) {
                mRowId = id;
            }
        } else {
        	//INSTRUMENTED
        	Supervisor.logFalse("saveState",this);

            mDbHelper.updateNote(mRowId, title, body);
        }
        
    	//INSTRUMENTED
    	Supervisor.logReturn("saveState",this);
    }

    //INSTRUMENTED
    @Override
    protected void onStart(){
    	Supervisor.logStart(this);
    	super.onStart();
    }
    
    @Override
    protected void onStop(){
    	Log.d("wtchoi","NoteEdit.onStop");
    	Supervisor.logStop(this);
    	super.onStop();
    }
}
