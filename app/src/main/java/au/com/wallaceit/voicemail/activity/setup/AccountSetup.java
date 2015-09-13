package au.com.wallaceit.voicemail.activity.setup;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

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
import au.com.wallaceit.voicemail.activity.K9Activity;
import au.com.wallaceit.voicemail.helper.Utility;


/**
 * Prompts the user for the Mobile Phone Number and Password.
 * Attempts to lookup default settings for the domain the user specified. If the
 * domain is known the settings are handed off to the AccountSetupCheckSettings
 * activity. If no settings are found the settings are handed off to the
 * AccountSetupAccountType activity.
 */

// TODO this calls is really only used to set up a new account
// It should be renamed appropriately, or it should merged with the class that displays and edits an account.


public class AccountSetup extends K9Activity
    implements OnClickListener, TextWatcher
{
    private static final String TAG = AccountSetup.class.getSimpleName();
    
// CSM    private final static String EXTRA_ACCOUNT		= "cc.martin.vv.AccountSetupBasics.account";
// CSM     private final static String STATE_KEY_PROVIDER	= "cc.martin.vv.AccountSetupBasics.provider";
    
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

        mPhoneNumberView.addTextChangedListener(this);
        mPasswordView.addTextChangedListener(this);
        
        mProvider = (Spinner)findViewById(R.id.account_provider);
        mSetupInstructions = (TextView) findViewById(R.id.setup_instructions);

        mActivateButton = (Button) findViewById(R.id.activate_call_button);
        mActivateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mCurrentProvider.helperNumbers.get("activate")));
                startActivity(intent);
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
        ArrayAdapter<Provider> providerAdaptor = new ArrayAdapter<Provider>(this, android.R.layout.simple_spinner_item, Provider.getProviderList(AccountSetup.this) );
        providerAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_item);
        mProvider.setAdapter(providerAdaptor);

    }

    @Override
    public void onResume()
    {
        super.onResume();
        validateFields();
    }

// CSM    
//    @Override
//    public void onSaveInstanceState(Bundle outState)
//    {
//        super.onSaveInstanceState(outState);
//        if (mAccount != null) {
//            outState.putString(EXTRA_ACCOUNT, mAccount.getUuid());
//        }
//        if (mProvider != null) {
//            outState.putSerializable(STATE_KEY_PROVIDER, mProvider);
//        }
//    }

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

    // These are used to to keep track of the WiFi state
    WifiManager wifiManager;
    boolean		wifiWasEnabled;
//    String		requiresCellular;

	protected void onNext()
    {

		// Get the selected provider
        Provider selectedProvider = (Provider) mProvider.getSelectedItem();
        if (selectedProvider == null)
        {
            // We don't have default settings for this account
        	Log.e(TAG, "No Providor Selected");
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
            mAccount.setDescription(selectedProvider.toString());
            mAccount.setNetworkOperatorName(selectedProvider.networkOperatorName);
            
            mAccount.setProvider(selectedProvider);
            mAccount.setStoreUri(uri.toString());
            mAccount.setDraftsFolderName(getString(R.string.special_mailbox_name_drafts));
            mAccount.setTrashFolderName(getString(R.string.special_mailbox_name_trash));
            mAccount.setArchiveFolderName(getString(R.string.special_mailbox_name_archive));
            mAccount.setSpamFolderName(getString(R.string.special_mailbox_name_spam));
            mAccount.setSentFolderName(getString(R.string.special_mailbox_name_sent));

            mAccount.setMaximumAutoDownloadMessageSize(0);
            mAccount.setMaximumPolledMessageAge(-1);
            mAccount.setSubscribedFoldersOnly(false);
            if (uri.toString().startsWith("imap"))
            {
                mAccount.setDeletePolicy(Account.DeletePolicy.ON_DELETE);
            }
            else if (uri.toString().startsWith("pop3"))
            {
                mAccount.setDeletePolicy(Account.DeletePolicy.NEVER);
            }

             
// CSM            AccountSetupCheckSettings.actionCheckSettings(this, mAccount, true);
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
    	
 
    	// Turn back on the WiFi if we turned it off
   	
    	if ( (resCode == RESULT_OK) && mAccount.validated )
        {
            mAccount.save(Preferences.getPreferences(this));
            VisualVoicemail.setServicesEnabled(this);
            // CSM can I get rid of this ??????;
            // CSM AccountSetupNames.actionSetNames(this, mAccount);
            setResult(RESULT_OK);
            finish();
       	}
        else
        {
        	// we added an added an account so now we need to remove it
        	if (mAccount != null)
        		Preferences.getPreferences(this).deleteAccount(mAccount);
        }
    }


    public void onClick(View v)
    {
        switch (v.getId())
        {
        case R.id.next:
            Log.d(TAG, "OnNext: next");
            onNext();
            break;
        }
    }

}
