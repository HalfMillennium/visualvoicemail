package au.com.wallaceit.voicemail;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import au.com.wallaceit.voicemail.R;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

public class Provider
{
    private static final String TAG = Provider.class.getSimpleName();
    private Context mContext;

    public String   id;             // used to track the configuration - allows the label to change. - should not be shown to the user
    public String   displayName;    // This is shown to the user
    public String   networkOperatorName;
    public URI      uri;            // Subscriber should never see this
    public String   int_prefix;     // Subscriber should never see this
    public String   std_prefix;     // Subscriber should never see this
    public String   login;          // Subscriber should never see this
    public boolean requiresCellular;
    public String notifySmsNumber;
    public HashMap<String, String> helperNumbers = new HashMap<String, String>();

    public Provider(Context context, XmlResourceParser xml) throws URISyntaxException
    {
        mContext = context;
        id                     = getXmlAttribute(context, xml, "id");
        displayName            = getXmlAttribute(context, xml, "displayName");
        networkOperatorName    = getXmlAttribute(context, xml, "networkOperatorName");
        uri                    = new URI(getXmlAttribute(context, xml, "uri"));
        int_prefix             = getXmlAttribute(context, xml, "int_prefix");
        std_prefix             = getXmlAttribute(context, xml, "std_prefix");
        login                  = getXmlAttribute(context, xml, "login");
        requiresCellular = getXmlAttribute(context, xml, "requires_cellular").equals("true");
        notifySmsNumber = getXmlAttribute(context, xml, "notify_sms_number");

        String activate = getXmlAttribute(context, xml, "phone_activate");
        String notifysms = getXmlAttribute(context, xml, "phone_notify_sms");
        if (activate!=null)
            helperNumbers.put("activate", activate);
        if (notifysms!=null)
            helperNumbers.put("notify_sms", notifysms);
    }

    public String toString()
    {
        if (displayName != null)
            return displayName;
        
        if (networkOperatorName != null)
            return networkOperatorName;
        
        return "Unknown";
    }

    private static String getXmlAttribute(Context context, XmlResourceParser xml, String name)
    {
        int resId = xml.getAttributeResourceValue(null, name, 0);
        
        if (resId == 0)
            return xml.getAttributeValue (null, name);
        else
            return context.getString(resId);
    }

    public static Provider findProviderById(Context context, String id)
    {
        try
        {
            int xmlEventType;
            Provider provider = null;
            XmlResourceParser xml = context.getResources().getXml(R.xml.providers);

            while ((xmlEventType = xml.next()) != XmlResourceParser.END_DOCUMENT)
            {
                if (xmlEventType == XmlResourceParser.START_TAG
                        && "provider".equals(xml.getName())
                        && id.equalsIgnoreCase(getXmlAttribute(context, xml, "id")))
                {
                    provider = new Provider(context, xml);
                    return provider;
                }
            }
        }
        catch (Exception e)
        {
          Log.e(TAG, "Error while trying to load provider settings.", e);
        }

        return null;
    }

    public static ArrayList<Provider> getProviderList(Context context)
    {
        ArrayList<Provider>     providerList    = new ArrayList<Provider>();

        try
        {
            int                 xmlEventType;
            XmlResourceParser   xml             = context.getResources().getXml(R.xml.providers);
            
            while ((xmlEventType = xml.next()) != XmlResourceParser.END_DOCUMENT)
            {
                if ( xmlEventType == XmlResourceParser.START_TAG && "provider".equals(xml.getName()) )
                {
                    if (getXmlAttribute(context, xml, "networkOperatorName") != null)
                    {
                        if ( getXmlAttribute(context, xml, "visable").equalsIgnoreCase("yes") || (BuildConfig.DEBUG) )
                        {
                            Provider provider               = new Provider(context, xml);
                            providerList.add(provider);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.e(TAG, "Error while trying to load provider settings.", e);
        }

        return providerList;
        
    }
}
