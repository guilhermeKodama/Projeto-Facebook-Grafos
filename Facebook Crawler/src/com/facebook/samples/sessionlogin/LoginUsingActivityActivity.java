/**
 * Copyright 2010-present Facebook.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.samples.sessionlogin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.HttpMethod;
import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;

public class LoginUsingActivityActivity extends Activity {
    private static final String URL_PREFIX_FRIENDS = "https://graph.facebook.com/me/friends?access_token=";
    
    private TextView textInstructionsOrLink;
    private Button buttonLoginLogout;
    private Button queryButton;
    private Button multiQueryButton;
    private Session.StatusCallback statusCallback = new SessionStatusCallback();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity);
        
        
        buttonLoginLogout = (Button)findViewById(R.id.buttonLoginLogout);
        textInstructionsOrLink = (TextView)findViewById(R.id.instructionsOrLink);
        queryButton = (Button) findViewById(R.id.queryButton);
        multiQueryButton = (Button) findViewById(R.id.multiQueryButton);
        /*Botao da query*/
        queryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fqlQuery = "SELECT uid, name, pic_square FROM user WHERE uid IN " +
                      "(SELECT uid2 FROM friend WHERE uid1 = me() LIMIT 25)";
                Bundle params = new Bundle();
                params.putString("q", fqlQuery);
                Session session = Session.getActiveSession();
                Request request = new Request(session,
                    "/fql",                         
                    params,                         
                    HttpMethod.GET,                 
                    new Request.Callback(){         
                        public void onCompleted(Response response) {
                            Log.i("Resultado", "Result: " + response.toString());
                        }                  
                }); 
                Request.executeBatchAsync(request);                 
            }
        });
        
        /*Botão multi query*/
        multiQueryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fqlQuery = "{" +
                      "'friends':'SELECT uid2 FROM friend WHERE uid1 = me() LIMIT 25'," +
                      "'friendinfo':'SELECT uid, name, pic_square FROM user WHERE uid IN " +
                      "(SELECT uid2 FROM #friends)'," +
                      "}";
                Bundle params = new Bundle();
                params.putString("q", fqlQuery);
                Session session = Session.getActiveSession();
                Request request = new Request(session,
                    "/fql",                         
                    params,                         
                    HttpMethod.GET,                 
                    new Request.Callback(){         
                        public void onCompleted(Response response) {
                            Log.i("RESULTADO MULTIQUERY", "Result: " + response.toString());
                        }                  
                }); 
                Request.executeBatchAsync(request);                 
            }
        });

        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

        Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
            }
            if (session == null) {
                session = new Session(this);
            }
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
            }
        }

        updateView();
    }

    @Override
    public void onStart() {
        super.onStart();
        Session.getActiveSession().addCallback(statusCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        Session.getActiveSession().removeCallback(statusCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }

    private void updateView() {
        Session session = Session.getActiveSession();
        if (session.isOpened()) {
            textInstructionsOrLink.setText(URL_PREFIX_FRIENDS + session.getAccessToken());
            buttonLoginLogout.setText(R.string.logout);
            buttonLoginLogout.setOnClickListener(new OnClickListener() {
                public void onClick(View view) { onClickLogout(); }
            });
        } else {
            textInstructionsOrLink.setText(R.string.instructions);
            buttonLoginLogout.setText(R.string.login);
            buttonLoginLogout.setOnClickListener(new OnClickListener() {
                public void onClick(View view) { onClickLogin(); }
            });
        }
    }

    private void onClickLogin() {
        Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
           session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
           queryButton.setVisibility(View.VISIBLE);
           multiQueryButton.setVisibility(View.VISIBLE);
        } else {
            Session.openActiveSession(this, true, statusCallback);
            queryButton.setVisibility(View.INVISIBLE);
            multiQueryButton.setVisibility(View.INVISIBLE);
        }
    }

    private void onClickLogout() {
        Session session = Session.getActiveSession();
        if (!session.isClosed()) {
            session.closeAndClearTokenInformation();
        }
    }

    private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            updateView();
        }
    }
}
