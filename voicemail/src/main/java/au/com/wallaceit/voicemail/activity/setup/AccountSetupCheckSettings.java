
package cc.martin.vv.activity.setup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.security.cert.CertificateException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.List;

import cc.martin.vv.VisualVoicemail;
import cc.martin.vv.activity.VisualVoicemailActivity;
import cc.martin.vv.controller.MessagingController;
import cc.martin.vv.helper.HipriController;
import cc.martin.vv.mail.AuthenticationFailedException;
import cc.martin.vv.mail.CertificateValidationException;
import cc.martin.vv.mail.Store;
import cc.martin.vv.mail.filter.Hex;
import cc.martin.vv.mail.store.TrustManagerFactory;
import cc.martin.vv.mail.store.WebDavStore;
import cc.martin.vv.Account;
import cc.martin.vv.Preferences;
import cc.martin.vv.R;


/**
 * Checks the given settings to make sure that they can be used to receive mail.
 *
 * XXX NOTE: The AndroidManifest for this application has it ignore configuration changes, because
 * it doesn't correctly deal with restarting while its thread is running.
 */
public class AccountSetupCheckSettings extends VisualVoicemailActivity
{
    private static final String TAG =  AccountSetupCheckSettings.class.getSimpleName();
    
    public  static final int            ACTIVITY_REQUEST_CODE   = 1;
    
    private static final int            MAX_NETWORK_RETRYS      = 15;
    private static final String         EXTRA_ACCOUNT           = "account";
    private              Handler        mHandler                = new Handler();
    private              ProgressBar    mProgressBar;
    private              TextView       mMessageView;
    private              Account        mAccount;
    private              boolean        mDestroyed;
    
    
    public static void actionCheckSettings(Activity context, Account account)
    {
        Intent i = new Intent(context, AccountSetupCheckSettings.class);
        i.putExtra(EXTRA_ACCOUNT, account.getUuid());
        context.startActivityForResult(i, ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        
        setContentView(R.layout.account_setup_check_settings);
        mMessageView = (TextView)findViewById(R.id.message);
        mProgressBar = (ProgressBar)findViewById(R.id.progress);

        setMessage(R.string.account_setup_check_settings_retr_info_msg);
        mProgressBar.setIndeterminate(true);

        String accountUuid = getIntent().getStringExtra(EXTRA_ACCOUNT);
        mAccount = Preferences.getPreferences(this).getAccount(accountUuid);
        
        final Context context = this;

        new Thread()
        {
            @Override
            public void run()
            {
                Store store = null;
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                WifiManager         wifiManager	        = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

                boolean             wifiWasDisabled       = false;
                boolean		        requiresCellular	= mAccount.getProvider().requiresCellular;

                mAccount.validated = false;

                try
                {
                    if (mDestroyed)
                    {
                        finish();
                        return;
                    }
                    // check if wifi currently enabled & account requires cellular
                    boolean wifiWasEnabled = null != wifiManager && wifiManager.isWifiEnabled();
                    if (wifiWasEnabled && requiresCellular){
                        // Try to switch to HIPRI
                        boolean hiresult = HipriController.start(VisualVoicemail.getContext(), Uri.parse(mAccount.getStoreUri()).getHost());
                        if (!hiresult){
                            // HIPRI failed: turn off wifi to validate settings
                            Log.d(TAG, "Disabling WiFi");
                            setMessage(R.string.account_setup_check_disable_wifi);
                            wifiManager.setWifiEnabled(false);
                            wifiWasDisabled = true;

                            // now wait a bit to see if the mobile connection gets up and running
                            setMessage(R.string.account_setup_wait_for_mobile_connection);
                            for (int count=0; count<=MAX_NETWORK_RETRYS; count++)
                            {
                                if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected())
                                    break;

                                if (count == MAX_NETWORK_RETRYS)
                                    throw new Throwable("Unable to obtain a mobile connection:\nCheck if Mobile Data is enabled\n and WiFi is disabled");

                                Thread.sleep(1000);
                                if (mDestroyed)
                                {
                                    finish();
                                    return;
                                }
                            }
                        }
                    }

                    store = mAccount.getRemoteStore();
                    
                    Log.d(TAG, "Starting tests");

                    if (store instanceof WebDavStore)
                        setMessage(R.string.account_setup_check_settings_authenticate);
                    else
                        setMessage(R.string.account_setup_check_settings_check_incoming_msg);
                
                    // Start the real check
                    store.checkSettings();
                    if (mDestroyed)
                    {
                        finish();
                        return;
                    }

                    setMessage(R.string.account_setup_check_settings_check_getting_folders_msg);
                    MessagingController.getInstance(getApplication()).listFoldersSynchronous(mAccount, true, null);
                    if (mDestroyed)
                    {
                        finish();
                        return;
                    }
                    
                    setMessage(R.string.account_setup_check_settings_check_syn_msg);
                    MessagingController.getInstance(getApplication()).synchronizeMailbox(mAccount, mAccount.getInboxFolderName(), null, null);
                    //mController = MessagingController.getInstance(getApplication()).synchronizeMailbox(mAccount, mFolderName, mListener, null);

                    mAccount.validated = true;
                    Log.d(TAG, "Tests compleated sucessfully");
                    
                }
                catch (final AuthenticationFailedException afe)
                {
                    String mesg =  afe.getMessage() == null ? getString(R.string.account_setup_failed_dlg_auth_message_2) : afe.getMessage();
                    
                    Log.e(TAG, "Error while testing settings: AuthenticationFailedException: " + mesg);
                    showErrorDialog( R.string.account_setup_failed_dlg_auth_message_fmt,  mesg );
                }
                catch (final CertificateValidationException cve)
                {
                    String mesg = cve.getMessage() == null ? "" : cve.getMessage();
                    
                    Log.e(TAG, "Error while testing settings: CertificateValidationException: " + mesg);

                    // Avoid NullPointerException in acceptKeyDialog()
                    if (TrustManagerFactory.getLastCertChain() != null)
                    {
                        acceptKeyDialog( R.string.account_setup_failed_dlg_certificate_message_fmt, mesg);
                    }
                    else
                    {
                        showErrorDialog( R.string.account_setup_failed_dlg_server_message_fmt, mesg);
                    }
                }
                catch (final Throwable t)
                {
                    String mesg = t.getMessage() == null ? "" : t.getMessage();
                    
                    Log.e(TAG, "Error while testing settings: Throwable: " + mesg);
                    showErrorDialog( R.string.account_setup_failed_dlg_server_message_fmt, mesg);
                }
                finally
                {
                    // Put WiFi back the way we found it
                    if ( wifiWasDisabled )
                    	wifiManager.setWifiEnabled(true);

                    if (mAccount.validated)
                    {
                        setResult(RESULT_OK);
                        finish();
                    }
                }
            }
        }
        .start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        mDestroyed = true;
    }

    /**
     * Displays the progress messages as each test is started
     * 
     * @param resId
     * This is the resource ID of the message to be displayed
     */
    private void setMessage(final int resId) {
        mHandler.post(new Runnable() {
            public void run() {
                if (mDestroyed) {
                    return;
                }
                mMessageView.setText(getString(resId));
            }
        });
    }

    private void showErrorDialog(final int msgResId, final Object... args)
    {
        mHandler.post(new Runnable()
        {
            public void run()
            {
                if (mDestroyed)
                    return;

                mProgressBar.setIndeterminate(false);
                AlertDialog.Builder win = new AlertDialog.Builder(AccountSetupCheckSettings.this);
                win.setTitle(getString(R.string.account_setup_failed_dlg_title));
                win.setMessage(getString(msgResId, args));
                win.setCancelable(false);

                win.setPositiveButton(
                		getString(R.string.account_setup_failed_dlg_retry_action),
                		new DialogInterface.OnClickListener()
                		{
                			public void onClick(DialogInterface dialog, int which)
                			{
                				dialog.cancel();
                				finish();
                			}
                		});
                win.create().show();
            }
        });
    }
    private void acceptKeyDialog(final int msgResId, final Object... args) {
        mHandler.post(new Runnable() {
            public void run() {
                if (mDestroyed) {
                    return;
                }
                final X509Certificate[] chain = TrustManagerFactory.getLastCertChain();
                String exMessage = "Unknown Error";
                Exception ex = null;
                if (args[0] instanceof Exception)
                    ex = ((Exception)args[0]);
                if (ex != null) {
                    if (ex.getCause() != null) {
                        if (ex.getCause().getCause() != null) {
                            exMessage = ex.getCause().getCause().getMessage();

                        } else {
                            exMessage = ex.getCause().getMessage();
                        }
                    } else {
                        exMessage = ex.getMessage();
                    }
                }

                mProgressBar.setIndeterminate(false);
                StringBuilder chainInfo = new StringBuilder(100);
                MessageDigest sha1 = null;
                try {
                    sha1 = MessageDigest.getInstance("SHA-1");
                } catch (NoSuchAlgorithmException e) {
                    Log.e(TAG, "Error while initializing MessageDigest", e);
                }
                for (int i = 0; i < chain.length; i++) {
                    // display certificate chain information
                    //TODO: localize this strings
                    chainInfo.append("Certificate chain[").append(i).append("]:\n");
                    chainInfo.append("Subject: ").append(chain[i].getSubjectDN().toString()).append("\n");

                    // display SubjectAltNames too
                    // (the user may be mislead into mistrusting a certificate
                    //  by a subjectDN not matching the server even though a
                    //  SubjectAltName matches)
                    try {
                        final Collection < List<? >> subjectAlternativeNames = chain[i].getSubjectAlternativeNames();
                        if (subjectAlternativeNames != null) {
                            // The list of SubjectAltNames may be very long
                            //TODO: localize this string
                            StringBuilder altNamesText = new StringBuilder();
                            altNamesText.append("Subject has ").append(subjectAlternativeNames.size()).append(" alternative names\n");

                            // we need these for matching
                            String storeURIHost = (Uri.parse(mAccount.getStoreUri())).getHost();
                            // String transportURIHost = (Uri.parse(mAccount.getTransportUri())).getHost();

                            for (List<?> subjectAlternativeName : subjectAlternativeNames) {
                                Integer type = (Integer)subjectAlternativeName.get(0);
                                Object value = subjectAlternativeName.get(1);
                                String name = "";
                                switch (type.intValue()) {
                                case 0:
                                    Log.w(TAG, "SubjectAltName of type OtherName not supported.");
                                    continue;
                                case 1: // RFC822Name
                                    name = (String)value;
                                    break;
                                case 2:  // DNSName
                                    name = (String)value;
                                    break;
                                case 3:
                                    Log.w(TAG, "unsupported SubjectAltName of type x400Address");
                                    continue;
                                case 4:
                                    Log.w(TAG, "unsupported SubjectAltName of type directoryName");
                                    continue;
                                case 5:
                                    Log.w(TAG, "unsupported SubjectAltName of type ediPartyName");
                                    continue;
                                case 6:  // Uri
                                    name = (String)value;
                                    break;
                                case 7: // ip-address
                                    name = (String)value;
                                    break;
                                default:
                                    Log.w(TAG, "unsupported SubjectAltName of unknown type");
                                    continue;
                                }

                                // if some of the SubjectAltNames match the store or transport -host,
                                // display them
                                if (name.equalsIgnoreCase(storeURIHost) ) {
                                    //TODO: localise this string
                                    altNamesText.append("Subject(alt): ").append(name).append(",...\n");
                                }
                                else if ( name.startsWith("*.") && (storeURIHost.endsWith(name.substring(2))) ) 
                                {
                                    //TODO: localise this string
                                    altNamesText.append("Subject(alt): ").append(name).append(",...\n");
                                }
                            }
                            chainInfo.append(altNamesText);
                        }
                    } catch (Exception e1) {
                        // don't fail just because of subjectAltNames
                        Log.w(TAG, "cannot display SubjectAltNames in dialog", e1);
                    }

                    chainInfo.append("Issuer: ").append(chain[i].getIssuerDN().toString()).append("\n");
                    if (sha1 != null) {
                        sha1.reset();
                        try {
                            char[] sha1sum = Hex.encodeHex(sha1.digest(chain[i].getEncoded()));
                            chainInfo.append("Fingerprint (SHA-1): ").append(new String(sha1sum)).append("\n");
                        } catch (CertificateEncodingException e) {
                            Log.e(TAG, "Error while encoding certificate", e);
                        }
                    }
                }

                new AlertDialog.Builder(AccountSetupCheckSettings.this)
                .setTitle(getString(R.string.account_setup_failed_dlg_invalid_certificate_title))
                //.setMessage(getString(R.string.account_setup_failed_dlg_invalid_certificate)
                .setMessage(getString(msgResId, exMessage)
                            + " " + chainInfo.toString()
                           )
                .setCancelable(true)
                .setPositiveButton(
                    getString(R.string.account_setup_failed_dlg_invalid_certificate_accept),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String alias = mAccount.getUuid();
                            alias = alias + ".incoming";
                            TrustManagerFactory.addCertificateChain(alias, chain);
                        } catch (CertificateException e) {
                            showErrorDialog(
                                R.string.account_setup_failed_dlg_certificate_message_fmt,
                                e.getMessage() == null ? "" : e.getMessage());
                        }
                        AccountSetupCheckSettings.actionCheckSettings(AccountSetupCheckSettings.this, mAccount);
                    }
                })
                .setNegativeButton(
                    getString(R.string.account_setup_failed_dlg_invalid_certificate_reject),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        return;
                    }
                })
                .show();
            }
        });
    }
}
