package au.com.wallaceit.voicemail.activity.setup;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.fsck.k9.mail.AuthType;
import com.fsck.k9.mail.ConnectionSecurity;
import com.fsck.k9.mail.MessagingException;
import com.fsck.k9.mail.ServerSettings;
import com.fsck.k9.mail.store.RemoteStore;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import au.com.wallaceit.voicemail.Account;
import au.com.wallaceit.voicemail.MobilePhoneNumberValidator;
import au.com.wallaceit.voicemail.Preferences;
import au.com.wallaceit.voicemail.Provider;
import au.com.wallaceit.voicemail.R;
import au.com.wallaceit.voicemail.VisualVoicemail;
import au.com.wallaceit.voicemail.account.AccountCreator;
import au.com.wallaceit.voicemail.activity.Accounts;
import au.com.wallaceit.voicemail.activity.K9Activity;
import au.com.wallaceit.voicemail.helper.Utility;
import au.com.wallaceit.voicemail.service.MissedCallReceiver;
import au.com.wallaceit.voicemail.service.SmsReceiver;


/**
 * Prompts the user for the Mobile Phone Number and Password.
 * Attempts to lookup default settings for the domain the user specified. If the
 * domain is known the settings are handed off to the AccountSetupCheckSettings
 * activity. If no settings are found the settings are handed off to the
 * AccountSetupAccountType activity.
 */

// TODO this calls is really only used to set up a new account
// It should be renamed appropriately, or it should merged with the class that displays and edits an account.


public class AccountSetup extends K9Activity implements OnClickListener, TextWatcher {
    private static final String TAG = AccountSetup.class.getSimpleName();
    
    private EditText mPhoneNumberView;
    private EditText mPasswordView;
    private Button mNextButton;
    private Account mAccount;
    private Spinner mProvider;
    private TextView mSetupInstructions;
    private Button mActivateButton;
    private Button mHelpButton;
    private Provider mCurrentProvider;
    
    private static final int ACTIVITY_REQUEST_NEW_ACCOUNT = 102;

    private MobilePhoneNumberValidator mPhoneNumberValidator = new MobilePhoneNumberValidator();

    public static void actionNewAccount(Activity context)
    {
        Intent intent = new Intent(context, AccountSetup.class);
        context.startActivityForResult(intent, ACTIVITY_REQUEST_NEW_ACCOUNT);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.account_details);

        mPhoneNumberView = (EditText)findViewById(R.id.account_mobile_phone_number);
        mPhoneNumberView.setInputType(Configuration.KEYBOARD_12KEY);
        mPasswordView = (EditText)findViewById(R.id.account_password);
        mNextButton = (Button)findViewById(R.id.next);
        mNextButton.setOnClickListener(this);
        findViewById(R.id.manual_setup).setOnClickListener(this);

        mPhoneNumberView.addTextChangedListener(this);
        mPasswordView.addTextChangedListener(this);
        
        mProvider = (Spinner)findViewById(R.id.account_provider);
        mSetupInstructions = (TextView) findViewById(R.id.setup_instructions);

        mActivateButton = (Button) findViewById(R.id.activate_call_button);
        mActivateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((VisualVoicemail) getApplication()).callPhoneNumber(AccountSetup.this, mCurrentProvider.helperNumbers.get("activate"));
            }
        });

        mHelpButton = (Button) findViewById(R.id.setup_help_button);
        mHelpButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // open help dialog
                int resourceId = AccountSetup.this.getResources().getIdentifier("setup_help_" + (mCurrentProvider.id.equals("0")?"1": String.valueOf(mCurrentProvider.id)), "string", AccountSetup.this.getPackageName());
                if (resourceId != 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AccountSetup.this);
                    builder.setTitle(R.string.setup_help_label).setMessage(resourceId).create().show();
                }
            }
        });

        mProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // show setup instructions
                mCurrentProvider = (Provider) mProvider.getSelectedItem();
                int resourceId = AccountSetup.this.getResources().getIdentifier("setup_instructions_" + (mCurrentProvider.id.equals("0")?"1": String.valueOf(mCurrentProvider.id)), "string", AccountSetup.this.getPackageName());
                if (resourceId != 0) {
                    mSetupInstructions.setText(resourceId);
                }
                mActivateButton.setVisibility(mCurrentProvider.helperNumbers.containsKey("activate")? View.VISIBLE: View.INVISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mActivateButton.setVisibility(View.GONE);
                mHelpButton.setVisibility(View.GONE);
            }
        });

        // Populate the provider list
        ArrayAdapter<Provider> providerAdaptor = new ArrayAdapter<Provider>(this, R.layout.spinner_layout, Provider.getProviderList(AccountSetup.this) );
        providerAdaptor.setDropDownViewResource(R.layout.spinner_layout);
        mProvider.setAdapter(providerAdaptor);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case VisualVoicemail.REQUEST_PHONE_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ((VisualVoicemail) getApplication()).onPhonePermissionSuccess(this);
                }
                return;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)){
            (new AlertDialog.Builder(this))
                    .setTitle("Autosetup")
                    .setMessage("Try auto setup?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendSms();
                        }
                    })
                    .setNegativeButton("No", null).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void sendSms() {
        if (requestSmsPermissions()) return;

        // make sure the receiver is active
        PackageManager packageManager = getApplicationContext().getPackageManager();
        packageManager.setComponentEnabledSetting(
                new ComponentName(this, SmsReceiver.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );

        SmsManager smsManager = SmsManager.getDefault();
        int mApplicationPort = 8901;
        String mDestinationNumber = "121";
        String text = "Activate:pv=12;ct=android;pt="+String.valueOf(mApplicationPort)+";//VVM";
        // If application port is set to 0 then send simple text message, else send data message.
        /*byte[] data;
        try {
            data = text.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Failed to encode: " + text);
        }
        Log.v(TAG, String.format("Sending BINARY sms '%s' to %s:%d", text, mDestinationNumber,
                mApplicationPort));
        smsManager.sendDataMessage(mDestinationNumber, null,
                (short) mApplicationPort, data, null, null);*/
        smsManager.sendTextMessage(mDestinationNumber, null, text, null, null);
    }


    private boolean requestSmsPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS,Manifest.permission.RECEIVE_SMS}, VisualVoicemail.REQUEST_SMSFULL_PERMISSION);
            Toast.makeText(this, "SMS permissions needed for autosetup.", Toast.LENGTH_LONG).show();
            return true;
        }
        return false;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        validateFields();
    }

    public void afterTextChanged(Editable s)
    {
        validateFields();
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
    }

    private void validateFields()
    {
        String phoneNumber = mPhoneNumberView.getText().toString();
        boolean valid = Utility.requiredFieldValid(mPhoneNumberView)
                        && Utility.requiredFieldValid(mPasswordView)
                        && mPhoneNumberValidator.isValid(phoneNumber);

        mNextButton.setEnabled(valid);
    }

	protected void onNext()
    {

		// Get the selected provider
        Provider selectedProvider = (Provider) mProvider.getSelectedItem();
        if (selectedProvider == null)
        {
            // We don't have default settings for this account
        	Log.e(TAG, "No Provider Selected");
            return;
        }

		
        String localPhoneNum = mPhoneNumberView.getText().toString();
        String password      = mPasswordView.getText().toString();

        // The phone number may need to replace the std_prefix with the country code 
        String internationalPhoneNum = localPhoneNum;
        if ( (selectedProvider.int_prefix != null) && (selectedProvider.std_prefix != null))
        {
            if (localPhoneNum.startsWith(selectedProvider.std_prefix))
                internationalPhoneNum = localPhoneNum.replaceFirst(selectedProvider.std_prefix, selectedProvider.int_prefix);
        }

        URI uri = null;
        
        try
        {
            String localPhoneNumEnc         = URLEncoder.encode(localPhoneNum, "UTF-8");
            String internationalPhoneNumEnc = URLEncoder.encode(internationalPhoneNum, "UTF-8");
            String passwordEnc 				= URLEncoder.encode(password, "UTF-8");

            // Perform the substitutions on the loginID.
            String loginID = selectedProvider.login;
            loginID = loginID.replaceAll("\\$loc_num", localPhoneNumEnc);
            loginID = loginID.replaceAll("\\$int_num", internationalPhoneNumEnc);

            // Build the URL adding the Login ID and password
            URI uriTemplate = selectedProvider.uri;
            uri = new URI(	uriTemplate.getScheme(),
            				loginID + ":" + passwordEnc,
            				uriTemplate.getHost(),
            				uriTemplate.getPort(),
            				uriTemplate.getPath(),
                            uriTemplate.getQuery(),
                            uriTemplate.getFragment());

            Log.d(TAG, "onNext: local phone: " + localPhoneNum);
            Log.d(TAG, "onNext: international: " + internationalPhoneNum);
            Log.d(TAG, "onNext: passwd: " + password);
            
            // create a new account
            // NOTE: if we fail the server check, we need to make sure that we delete the account
            mAccount = Preferences.getPreferences(this).newAccount();
            mAccount.setProvider(selectedProvider);
            mAccount.setNetworkOperatorName(selectedProvider.networkOperatorName);
            mAccount.setRequiresCellular(selectedProvider.requiresCellular);
            mAccount.setDescription(selectedProvider.toString());
            mAccount.setStoreUri(uri.toString());

            mAccount = AccountCreator.initialVisualVoicemailSetup(AccountSetup.this, mAccount);

            AccountSetupCheckSettings.actionCheckSettings(AccountSetup.this, mAccount, AccountSetupCheckSettings.CheckDirection.INCOMING);
        }
        catch (UnsupportedEncodingException enc)
        {
            // This really shouldn't happen since the encoding is hardcoded to UTF-8
            Log.e(TAG, "Couldn't urlencode username or password.", enc);
        }
        catch (URISyntaxException use)
        {
            /*
             * If there is some problem with the URI we give up
             */
        	Log.e(TAG, "Couldn't configure visual voice mail.", use);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resCode, Intent data)
    {
        // Display the result to the debug console 
    	String strResCode;
    	
    	switch (resCode)
    	{
    	case RESULT_CANCELED:
    		strResCode = "RESULT_CANCELED";
    		break;
    	case RESULT_OK:
    		strResCode = "RESULT_OK";
    		break;
    	case RESULT_FIRST_USER:
    		strResCode = "RESULT_FIRST_USER";
    		break;
   		default:
   			strResCode = "UNKNOWN";
   			break;
    	}
    	
    	Log.i(TAG, "OnActivityResult: recieved " + strResCode);
   	
    	if ((resCode == RESULT_OK) && (mAccount!=null && mAccount.validated)){
            mAccount.save(Preferences.getPreferences(this));
            try {
                AccountCreator.createArchiveFolderIfNeeded(mAccount, mAccount.getLocalStore());
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            VisualVoicemail.setServicesEnabled(this);
            Intent intent = new Intent(AccountSetup.this, Accounts.class);
            startActivity(intent);
            finish();
       	} else {
        	// we added an added an account so now we need to remove it
        	if (mAccount != null)
        		Preferences.getPreferences(this).deleteAccount(mAccount);
        }
    }

    public void onClick(View v)
    {
        switch (v.getId())
        {
        case R.id.manual_setup:
            Log.d(TAG, "ManualSetup");
            mAccount = Preferences.getPreferences(this).newAccount();
            ServerSettings storeServer = new ServerSettings(ServerSettings.Type.IMAP, "voicemail.example.com", -1,
                    ConnectionSecurity.SSL_TLS_REQUIRED, AuthType.PLAIN, "username", "password", "");
            String storeUri = RemoteStore.createStoreUri(storeServer);
            mAccount.setStoreUri(storeUri);
            AccountSetupIncoming.actionNewAccountSetup(AccountSetup.this, mAccount);
            break;
        case R.id.next:
            Log.d(TAG, "OnNext: next");
            onNext();
            break;
        }
    }

}
