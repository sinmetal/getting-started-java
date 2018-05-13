/*
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.appengine.java8;

// [START example]

import com.google.appengine.api.utils.SystemProperty;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.annotations.Nullable;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

// With @WebServlet annotation the webapp/WEB-INF/web.xml is no longer required.
@WebServlet(name = "Firestore", value = "/firestore")
public class FirestoreSample extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // Use the application default credentials
    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
    FirebaseOptions options = new FirebaseOptions.Builder()
            .setCredentials(credentials)
            .setProjectId("sinmetal-firestore")
            .build();
    FirebaseApp.initializeApp(options);

    Firestore db = FirestoreClient.getFirestore();

    DocumentReference docRef = db.collection("cities").document("SF");
    docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
      @Override
      public void onEvent(@Nullable DocumentSnapshot snapshot,
                          @Nullable FirestoreException e) {
        if (e != null) {
          System.err.println("Listen failed: " + e);
          return;
        }

        if (snapshot != null && snapshot.exists()) {
          System.out.println("Current data: " + snapshot.getData());
          Set<Map.Entry<String, Object>> entries = snapshot.getData().entrySet();
          send(entries.toArray()[0].toString());
        } else {
          System.out.print("Current data: null");
        }
      }
    });
    
  }

  public static void send(String text) {
    HttpURLConnection con = null;
    try {
      URL url = new URL("https://sinmetal-firestore.appspot.com/hello?text=" + text);

      con = (HttpURLConnection) url.openConnection();
      con.setDoOutput(true);
      con.setRequestMethod("GET");
      con.connect();

      final int status = con.getResponseCode();
      if (status == HttpURLConnection.HTTP_OK) {
        // 通信に成功した
        // テキストを取得する
        final InputStream in = con.getInputStream();
        String encoding = con.getContentEncoding();
        if (null == encoding) {
          encoding = "UTF-8";
        }
        final InputStreamReader inReader = new InputStreamReader(in, encoding);
        final BufferedReader bufReader = new BufferedReader(inReader);
        String line = null;
        // 1行ずつテキストを読み込む
        StringBuffer result = new StringBuffer();
        while ((line = bufReader.readLine()) != null) {
          result.append(line);
        }
        bufReader.close();
        inReader.close();
        in.close();
      } else {
        // 通信が失敗した場合のレスポンスコードを表示
        System.out.println(status);
      }
    } catch(Throwable t) {
      System.err.print(t.getMessage());
    } finally {

    }


  }
}
