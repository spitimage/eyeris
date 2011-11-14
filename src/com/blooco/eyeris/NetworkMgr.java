package com.blooco.eyeris;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.util.Log;

public class NetworkMgr
{
    private static final String TAG = "NetworkMgr";

    /**
     * perform an Http POST to the supplied urlString with the supplied
     * requestHeaders and formParameters
     * 
     * @return String the response contents
     * @param urlString
     *            the URL to post to
     * @param requestHeaders
     *            a Map of the request headernames and values to be placed into
     *            the request
     * @param formParameters
     *            a Map of form parameters and values to be placed into the
     *            request
     * @param contents
     *            the contents of the HTTP request
     * @throws MalformedURLException
     *             reports problems with the urlString
     * @throws IOException
     *             reports I/O sending and/or retrieving data over Http
     */
    public static int post(String urlString, Map<String, String> requestHeaders,
            Map<String, String> formParameters, String requestContents) throws EyerisException
    {
        int ret = 0;
        try
        {
            // open url connection
            Log.d(TAG, "Opening connection to " + urlString);
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // set up url connection to post information and
            // retrieve information back
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);

            // add all the request headers
            if (requestHeaders != null)
            {
                Set<String> headers = requestHeaders.keySet();
                for (Iterator<String> it = headers.iterator(); it.hasNext();)
                {
                    String headerName = it.next();
                    String headerValue = requestHeaders.get(headerName);
                    con.setRequestProperty(headerName, headerValue);
                }
            }

            // add url form parameters
            DataOutputStream ostream = null;
            try
            {
                ostream = new DataOutputStream(con.getOutputStream());
                if (formParameters != null)
                {
                    Set<String> parameters = formParameters.keySet();
                    Iterator<String> it = parameters.iterator();
                    StringBuffer buf = new StringBuffer();

                    for (int i = 0; it.hasNext(); i++)
                    {
                        String parameterName = it.next();
                        String parameterValue = formParameters.get(parameterName);

                        if (parameterValue != null)
                        {
                            Log.d(TAG, "Adding param: " + parameterValue);
                            parameterValue = URLEncoder.encode(parameterValue);
                            if (i > 0)
                            {
                                buf.append("&");
                            }
                            buf.append(parameterName);
                            buf.append("=");
                            buf.append(parameterValue);
                        }
                    }
                    ostream.writeBytes(buf.toString());
                }

                if (requestContents != null)
                {
                    ostream.writeBytes(requestContents);
                }

            }
            finally
            {
                if (ostream != null)
                {
                    ostream.flush();
                    ostream.close();
                }
            }

            ret = con.getResponseCode();
        }
        catch (MalformedURLException e)
        {
            Log.e(TAG, "Malformed URL: " + urlString);
            throw new EyerisException("Unable to make network connection.");
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
            throw new EyerisException("Unable to make network connection.");
        }
        
        return ret;
    }
}
