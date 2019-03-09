package cc.martin.vv.activity.setup;

// CSMimport android.content.Context;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
// CSM import android.sax.StartElementListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import cc.martin.vv.activity.VisualVoicemailActivity;

// CSM import cc.martin.vv.Preferences;
import cc.martin.vv.R;

public class InstallMessage extends VisualVoicemailActivity implements OnClickListener
{
    private static final String TAG = InstallMessage.class.getSimpleName();
    
    public static final int ACTIVITY_REQUEST_CODE_WELCOME_SCREEN   = 101;
	
	public static void showInstalationMessage(Activity activity)
	{
		Intent intent = new Intent(activity, InstallMessage.class);
		activity.startActivityForResult(intent, ACTIVITY_REQUEST_CODE_WELCOME_SCREEN);
		//context.startActivity(intent);
	}

	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		Log.d(TAG, "onCreate()");
		setContentView(R.layout.install_message);
		((Button) findViewById(R.id.next)).setOnClickListener(this);
	}

	public void onClick(View view)
	{
		if (view.getId() == R.id.next)
		{
		    Log.d(TAG, "onClick: Next");
			AccountSetup.actionNewAccount(this);
		}
	}
	
	public void onActivityResult(int requestCode, int resCode, Intent data)
	{
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
    	
    	Log.d(TAG, "OnActivityResult: received " + strResCode);
		
		if (resCode == RESULT_OK)
		{
	        setResult(RESULT_OK);
            finish();
		}
	}
}
