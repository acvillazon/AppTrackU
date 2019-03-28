package com.example.webservicetracku.networking;

import android.os.AsyncTask;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class WebServiceManager {

    public static void CallWebServiceOperation(final WebServiceManagerInterface  caller,
                                               final String  webServiceURL,
                                               final String resourceName,
                                               final String operation,
                                               final String methodType,
                                               final String payload,
                                               final String userState){

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    URL url=new URL(webServiceURL+"/"+resourceName+"/"+operation);
                    HttpURLConnection httpURLConnection= (HttpURLConnection)url.openConnection();
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setConnectTimeout(3000);
                    httpURLConnection.setRequestMethod(methodType);
                    httpURLConnection.getOutputStream().write(payload.getBytes());
                    int responseCode=httpURLConnection.getResponseCode();
                    if(responseCode==HttpURLConnection.HTTP_OK){
                        InputStream in=httpURLConnection.getInputStream();
                        StringBuffer stringBuffer=new StringBuffer();
                        int charIn=0;
                        while((charIn=in.read())!=-1){
                            stringBuffer.append((char)charIn);
                        }

                        System.out.println("Mensaje "+stringBuffer.toString());
                        caller.WebServiceMessageReceived(userState,new String(stringBuffer));
                    }
                    //httpURLConnection.getOutputStream().flush();
                } catch (SocketTimeoutException e){
                    caller.WebServiceMessageReceived(userState,"Down");
                } catch (Exception error){
                    caller.WebServiceMessageReceived(userState,"Down");
                }
            }
        });
    }


}
