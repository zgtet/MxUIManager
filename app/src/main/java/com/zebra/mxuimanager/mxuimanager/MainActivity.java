package com.zebra.mxuimanager.mxuimanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.symbol.emdk.*;
import com.symbol.emdk.EMDKManager.EMDKListener;

import android.text.TextUtils;
import android.util.Xml;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.StringReader;

public class MainActivity extends Activity implements EMDKListener {


    // Assign the profile name used in EMDKConfig.xml
    private String profileName = "UI Manager";

    // Declare a variable to store ProfileManager object
    private ProfileManager profileManager = null;

    // Declare a variable to store EMDKManager object
    private EMDKManager emdkManager = null;

    // Contains the parm-error name (sub-feature that has error)
    private String errorName = "";

    // Contains the characteristic-error type (Root feature that has error)
    private String errorType = "";

    // contains the error description for parm or characteristic error.
    private String errorDescription = "";

    // contains status of the profile operation
    private String status = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //The EMDKManager object will be created and returned in the callback.
        EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), this);

        //Check the return status of getEMDKManager
        if (results.statusCode == EMDKResults.STATUS_CODE.SUCCESS) {

        // EMDKManager object creation success


        } else {

        // EMDKManager object creation failed


        }
    }

    @Override
    public void onClosed() {
        // TODO Auto-generated method stub
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        //Clean up the objects created by EMDK manager
        emdkManager.release();
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        // This callback will be issued when the EMDK is ready to use.
        this.emdkManager = emdkManager;

        // Get the ProfileManager object to process the profiles
        profileManager = (ProfileManager) emdkManager
                .getInstance(EMDKManager.FEATURE_TYPE.PROFILE);

        if (profileManager != null) {
            String[] modifyData = new String[1];

// Call processPrfoile with profile name and SET flag to create the profile. The modifyData can be null.
            EMDKResults results = profileManager.processProfile(profileName,
                    ProfileManager.PROFILE_FLAG.SET, modifyData);

            if (results.statusCode == EMDKResults.STATUS_CODE.CHECK_XML) {
                // Get XML response as a String
                String statusXMLResponse = results.getStatusString();

                try {
                    // Create instance of XML Pull Parser to parse the response
                    XmlPullParser parser = Xml.newPullParser();
                    // Provide the string response to the String Reader that reads
                    // for the parser
                    parser.setInput(new StringReader(statusXMLResponse));
                    // Call method to parse the response
                    parseXML(parser);
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }

                // Method call to display results in a dialog
                displayResults();
            }


            } else {
                // Show dialog of Failure
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Failure");
                builder.setMessage("Failed to apply profile...")
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick                                                 (DialogInterface dialog,
                                                                                                         int id) {

                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
    }

    // Method to parse the XML response using XML Pull Parser
    public void parseXML(XmlPullParser myParser) {
        int event;
        try {
            event = myParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = myParser.getName();
                switch (event) {
                    case XmlPullParser.START_TAG:
                        // Get Status, error name and description in case of
                        // parm-error
                        if (name.equals("parm-error")) {
                            status = "Failure";
                            errorName = myParser.getAttributeValue(null, "name");
                            errorDescription = myParser.getAttributeValue(null,
                                    "desc");

                            // Get Status, error type and description in case of
                            // parm-error
                        } else if (name.equals("characteristic-error")) {
                            status = "Failure";
                            errorType = myParser.getAttributeValue(null, "type");
                            errorDescription = myParser.getAttributeValue(null,
                                    "desc");
                        }
                        break;
                    case XmlPullParser.END_TAG:

                        break;
                }
                event = myParser.next();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    // Method to build failure message that contains name, type and
        // description of respective error (parm, characteristic or both)
    public String buildFailureMessage() {
        String failureMessage = "";
        if (!TextUtils.isEmpty(errorName) && !TextUtils.isEmpty(errorType))
            failureMessage = errorName + " :" + "\n" + errorType + " :" + "\n"
                    + errorDescription;
        else if (!TextUtils.isEmpty(errorName))
            failureMessage = errorName + " :" + "\n" + errorDescription;
        else
            failureMessage = errorType + " :" + "\n" + errorDescription;
        return failureMessage;

    }

    // Method to display results (Status, Error Name, Error Type, Error
    // Description) in a
    // dialog
    public void displayResults() {
        // Alert Dialog to display the status of the Profile creation
        // operation of MX features
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                MainActivity.this);

        if (TextUtils.isEmpty(errorDescription)) {
            alertDialogBuilder.setTitle("Success");
            alertDialogBuilder.setMessage("Profile Successfully Applied...");
        } else {
            // set title
            alertDialogBuilder.setTitle(status);
            // call buildFailureMessage() method to set failure message in
            // dialog
            alertDialogBuilder.setMessage(buildFailureMessage());
        }

        alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();

    }
}
