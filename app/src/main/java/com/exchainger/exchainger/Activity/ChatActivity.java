package com.exchainger.exchainger.Activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.exchainger.exchainger.Model.ChatMessage;
import com.exchainger.exchainger.Model.Constants;
import com.exchainger.exchainger.R;
import com.exchainger.exchainger.databinding.ActivityChatBinding;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int SIGN_IN_REQUEST_CODE = 1;
    private static final int PHOTO_PICKER_REQUEST_CODE = 2;
    private static final String TRANSACTION_KEY_KEY = "transaction.key_key";
    private static final String TRANSACTION_USER_ID_KEY = "transaction.user.id.key";

    private static final String TAG = "ChatActivity";
    private static final int DEFAULT_MSG_LENGTH_LIMIT = 250;

    private EditText input;
    private FirebaseRecyclerAdapter<ChatMessage, ChatHolder> mRecyclerAdapter;
    private DatabaseReference mMessageDatabaseReference;
    private FirebaseUser mFirebaseUser;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FloatingActionButton mFab;
    private RecyclerView mListOfMessages;
    private ImageButton photoPicker;
    private ProgressBar mProgressBar;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ActivityChatBinding mChatBinding;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mChatPhotosStorageReference;
    private ChildEventListener mChildEventListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference referenceToTransChat;



    public static Intent newIntent(Context context, String transactionId, String transactionUserID) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(TRANSACTION_KEY_KEY, transactionId);
        intent.putExtra(TRANSACTION_USER_ID_KEY, transactionUserID);
        return intent;
    }

    private static CharSequence formatDate(long date) {
        return DateFormat.format("h:mm a d/MMM/yy", date);
    }

    private void detachChildListener(){
        if (mChildEventListener != null){
            referenceToTransChat.child(Constants.FIREBASE_CHILD_TRANSACTIONS).removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    private void attachChildListener(){
        if (mChildEventListener == null){
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot snapshot, String s) {
                }

                @Override
                public void onChildChanged(DataSnapshot snapshot, String s) {
                    Boolean bool = true;

                    try {
                        bool = snapshot.getValue(Boolean.class);
                    } catch (Exception e) {
                        Log.e(TAG, "onChildChanged: " + e.getMessage());
                    }
                    if (bool) {
                        input.setVisibility(View.VISIBLE);
                        mFab.setVisibility(View.VISIBLE);
                        photoPicker.setVisibility(View.VISIBLE);
                    } else {
                        input.setVisibility(View.GONE);
                        mFab.setVisibility(View.GONE);
                        photoPicker.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot snapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            };
            referenceToTransChat.child(Constants.FIREBASE_CHILD_TRANSACTIONS).addChildEventListener(mChildEventListener);
            referenceToTransChat.child(Constants.FIREBASE_CHILD_TRANSACTIONS).addChildEventListener(mChildEventListener);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChatBinding = DataBindingUtil.setContentView(this, R.layout.activity_chat);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        final Intent intent = getIntent();
        final String transactionKey = intent.getStringExtra(TRANSACTION_KEY_KEY);
        String buyer_or_seller = intent.getStringExtra(TRANSACTION_USER_ID_KEY);

        mChatPhotosStorageReference = mFirebaseStorage.getReference(transactionKey);

        mListOfMessages = mChatBinding.listOfMessages;
        mFab = mChatBinding.fab;
        mFab.setOnClickListener(this);
        input = mChatBinding.input;
        photoPicker = mChatBinding.photoPickerButton;
        photoPicker.setOnClickListener(this);
        mProgressBar = mChatBinding.progressBar;

        mProgressBar.setVisibility(View.INVISIBLE);

        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        referenceToTransChat = mFirebaseDatabase.getReference(Constants.FIREBASE_CHILD_TRANSACTION_CHATS).child(transactionKey);
        mMessageDatabaseReference = referenceToTransChat.child(Constants.FIREBASE_CHILD_CHATS);
        referenceToTransChat.child(Constants.FIREBASE_CHILD_TRANSACTION_INFO).child(buyer_or_seller).setValue(mFirebaseUser.getUid());
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    //displayChatMessages();
                } else {
                    List<AuthUI.IdpConfig> providers = new ArrayList<>();
                    providers.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
                    providers.add(new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .build()
                            , SIGN_IN_REQUEST_CODE);
                }
            }
        };
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    mFab.setEnabled(true);
                } else {
                    mFab.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});
    }

    private void displayChatMessages() {
        /*final LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mListOfMessages.setLayoutManager(mLinearLayoutManager);
        mRecyclerAdapter = new FirebaseRecyclerAdapter<ChatMessage, ChatHolder>(ChatMessage.class, R.layout.chat_message, ChatHolder.class, mMessageDatabaseReference) {
            @Override
            protected void populateViewHolder(ChatHolder viewHolder, ChatMessage model, int position) {
                viewHolder.bind(model);
            }
        };
        mRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mRecyclerAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 || (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mListOfMessages.scrollToPosition(positionStart);
                }
            }
        });

        mListOfMessages.setAdapter(mRecyclerAdapter);
*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this,
                        getResources().getString(R.string.sucess_login),
                        Toast.LENGTH_LONG)
                        .show();
                //displayChatMessages();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this,
                        "We couldn't sign you in. Please try again later.",
                        Toast.LENGTH_LONG)
                        .show();
                finish();
            }
        } else if (requestCode == PHOTO_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {


            Uri selectedImageUri = data.getData();
            File imageFile = new File(getRealPathFromURI_(selectedImageUri));
            long length = imageFile.length();
            length = length / 1024;
            Log.d(TAG, "onActivityResult: ImageSize " + length);
            if (length > 50) {
                Snackbar.make(mChatBinding.getRoot(), "Image Size Must Be Less Than 50kb", Snackbar.LENGTH_LONG).show();
            } else {
//                StorageReference photoRef = mChatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());
//                UploadTask uploadTask = photoRef.putFile(selectedImageUri);
//                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(UploadTask.TaskSnapshot snapshot) {
//                        Uri downloadUrl = snapshot.getDownloadUrl();
//                        mMessageDatabaseReference.push().setValue(new ChatMessage(null, "Ope Leke", "xyzAbcD00mMxWeOhgT", downloadUrl.toString()));
//                    }
//                });
            }


        }
    }

    private String getRealPathFromURI(Uri uri) {
        String result;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = uri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public String getRealPathFromURI_ (Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, "", null, "");
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }


    @Override
    public void onClick(View view) {
        if (view == mFab) {
            String text = input.getText().toString();
            if (TextUtils.isEmpty(text) || text.isEmpty()) return;
            String displayName = mFirebaseUser.getDisplayName();
            ChatMessage message = new ChatMessage(text, displayName, null);
            mMessageDatabaseReference.push().setValue(message);
            input.setText("");
        } else if (view == photoPicker) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent, "Complete action using"), PHOTO_PICKER_REQUEST_CODE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        attachChildListener();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        detachChildListener();
    }


    public static class ChatHolder extends RecyclerView.ViewHolder {

        private TextView messageTime;
        private TextView messageUser;
        private TextView messageText;
        private ImageView photoImageView;

        public ChatHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            messageUser = itemView.findViewById(R.id.message_user);
            messageTime = itemView.findViewById(R.id.message_time);
            photoImageView = itemView.findViewById(R.id.photoImageView);
        }

        public void bind(ChatMessage model) {
            messageTime.setText(formatDate(model.getTime()));
            messageUser.setText(model.getUserName());

            boolean isPhoto = (model.getPhotoUrl() != null);
            if (isPhoto) {
                messageText.setVisibility(View.GONE);
                photoImageView.setVisibility(View.VISIBLE);
                Glide.with(photoImageView.getContext())
                        .load(model.getPhotoUrl())
                        .into(photoImageView);
            } else {
                messageText.setText(model.getText());
                messageText.setVisibility(View.VISIBLE);
                photoImageView.setVisibility(View.GONE);
            }

        }
    }
}