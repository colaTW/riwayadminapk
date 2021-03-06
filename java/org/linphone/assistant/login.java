package org.linphone.assistant;

import static org.linphone.mediastream.MediastreamerAndroidContext.getContext;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.R;
import org.linphone.core.TransportType;

public class login extends AssistantActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        TextView UUID = findViewById(R.id.UUID);
        TextView Num = findViewById(R.id.Num);
        DatePicker dt = findViewById(R.id.dt);
        ImageButton confrim = findViewById(R.id.login);
        Bundle bundle = getIntent().getExtras();
        final String user = bundle.getString("user");
        final String Domain = bundle.getString("Domain");
        final String Password = bundle.getString("Password");
        final String guard = bundle.getString("guard");
        final String type = bundle.getString("type");
        Num.setText(user);
        Integer year = dt.getYear();
        Integer mon = dt.getMonth();
        Integer day = dt.getDayOfMonth();
        final Integer birth = year * 10000 + (mon + 1) * 100 + day;
        dt.setMinDate(19110101);
        // get id?????????
        final String android_id =
                Settings.Secure.getString(
                        getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        UUID.setText(android_id);
        confrim.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view2) {
                        try {
                            URL url =
                                    new URL(
                                            "http://49.159.128.172:8888/api/v1/household/device/register");
                            JSONObject jo = new JSONObject();
                            jo.put("uuid", android_id.toString());
                            jo.put("password", birth.toString());
                            jo.put("household_number", user);
                            HttpClient httpClient = new DefaultHttpClient();
                            AbstractHttpEntity entity =
                                    new ByteArrayEntity(jo.toString().getBytes("UTF8"));
                            entity.setContentType(
                                    new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                            HttpPost httpPost = new HttpPost(url.toURI());
                            httpPost.setEntity(entity);
                            // Prepare JSON to send by setting the entity
                            HttpResponse response = httpClient.execute(httpPost);
                            String json_string = EntityUtils.toString(response.getEntity());
                            JSONObject temp1 = new JSONObject(json_string);
                            JSONObject data = temp1.getJSONObject("data");
                            String newpassword = data.getString("newSipCode");
                            String error = data.getString("errors");
                            if (error.equals("")) {
                                mAccountCreator.setUsername(user);
                                mAccountCreator.setDomain(Domain);
                                mAccountCreator.setPassword(newpassword);
                                mAccountCreator.setTransport(TransportType.Udp);
                                createProxyConfigAndLeaveAssistant();
                                // ????????????
                                FileOutputStream fos =
                                        openFileOutput("info.txt", Context.MODE_PRIVATE);
                                String test = guard + ",";
                                fos.write(test.getBytes());
                                String birthday = birth.toString();
                                fos.write(birthday.getBytes());
                                fos.close();
                                FileInputStream fin = openFileInput("info.txt");
                                byte[] buffer = new byte[100];
                                int byteCount = fin.read(buffer);
                                String show = new String(buffer, 0, byteCount, "utf-8");
                                System.out.println(show);
                                fin.close();
                            } else {
                                Toast.makeText(login.this, error, Toast.LENGTH_SHORT).show();
                            }
                            //  BufferedReader reader = new BufferedReader(new
                            // InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
