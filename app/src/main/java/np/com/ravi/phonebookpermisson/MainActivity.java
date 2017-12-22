package np.com.ravi.phonebookpermisson;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ConstraintLayout mLayout;
    AutoCompleteTextView userInput;
    Button addButton;
    ImageButton addContactButton;
    private ProgressBar progressBar;

    //for read contacts permission
    private static final int PERMISSION_REQUEST_READ_CONTACTS = 0;

    private static final int PICK_CONTACT = 1;

    //uri for contact data
    Uri contactDataUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayout = (ConstraintLayout) findViewById(R.id.main_layout);
        progressBar = (ProgressBar) findViewById(R.id.indeterminate_bar);

        userInput = (AutoCompleteTextView) findViewById(R.id.user_input);
        addButton = (Button) findViewById(R.id.add_button);
        addContactButton = (ImageButton) findViewById(R.id.add_contact_button);

        addButton.setOnClickListener(this);
        addContactButton.setOnClickListener(this);

        if (checkForReadContactsPermission()){
            storeContactsToArrayList();
        }
    }

    private boolean checkForReadContactsPermission() {
        // Check if the Read Contacts permission has been granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available
            return true;
        } else {
            // Permission is missing and must be requested.
            return false;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.add_button:
                // do your code
                //validate first
                showToast("Add Button clicked");
                break;

            case R.id.add_contact_button:
                //check contact read permission
                if (checkForReadContactsPermission()){
                    startContactPicker();
                } else {
                    askForReadContactsPermission();
                }
                break;

            default:
                break;
        }
    }

    private void askForReadContactsPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.READ_CONTACTS)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Snackbar.make(mLayout, "Contacts access is required by the app to display your contacts",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.READ_CONTACTS},
                            PERMISSION_REQUEST_READ_CONTACTS);
                }
            }).show();
        } else {
            Snackbar.make(mLayout,
                    "Permission is not available. Requesting read contacts permission.",
                    Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSION_REQUEST_READ_CONTACTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("IN", " onRequestPermissionResult");
        switch (requestCode) {
            case PERMISSION_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    storeContactsToArrayList();
                    startContactPicker();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Snackbar.make(mLayout,
                            "Permission is not available.",
                            Snackbar.LENGTH_SHORT).show();
                }
                //return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void storeContactsToArrayList() {
        Log.d("In ", "storeContactsToArrayList() called");

        List<Contact> contactList = new ArrayList<>();

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
//                        Log.i("GOT", "Name: " + name);
//                        Log.i("GOT", "Phone Number: " + phoneNo); //working
                        //To our POJO
                        Contact contact = new Contact();
                        contact.setName(name);
                        contact.setPhoneNumber(phoneNo);

                        contactList.add(contact);

                    }
                    pCur.close();
                }
                ArrayAdapter<Contact> contactsArrayAdapter =
                        new ArrayAdapter<Contact>(this, android.R.layout.simple_list_item_1, contactList);

                //setting this adapter to our autocompleteTextView userInput
                userInput.setAdapter(contactsArrayAdapter);
            }
        }
        if(cur!=null){
            cur.close();
        }
    }

    private void startContactPicker() {
        Log.d("IN ", "startContactPicker() called");
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("In result ", String.valueOf(resultCode));
        if (requestCode == PICK_CONTACT &&
                resultCode == RESULT_OK &&
                data != null && data.getData() != null) {
            contactDataUri = data.getData();
            //showToast(String.valueOf(contactDataUri)); //working
            Log.d("Got URI", String.valueOf(contactDataUri));
            getContact(contactDataUri); //also sets the contact number to our autocomplete textview

        } else {
            userInput.setText("");
            Snackbar.make(mLayout, "No contact selected.", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void getContact(Uri contactDataUri) {
        Cursor c = managedQuery(contactDataUri, null, null, null, null);
        if (c.moveToFirst()) {
            String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
            String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

            if (hasPhone.equalsIgnoreCase("1") || hasPhone.equalsIgnoreCase("2")) {
                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                phones.moveToFirst();

                String pickedContactNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String pickedContactName = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)); //name is here
                //TODO: email aauchha ki nai check
                Log.d("Got name", " from Contact " + pickedContactName);
                //Log.d("Got email", " from Contact "+pickedContactEmail);
                showToast(pickedContactName);

                //setting picked contact's number to our edittext
                userInput.setText(pickedContactNumber);

            }
        }
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
