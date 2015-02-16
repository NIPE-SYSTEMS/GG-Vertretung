package de.gebatzens.ggvertretungsplan;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class UpdateActivity extends AsyncTask<String, String, String> {
    private ProgressDialog pDialog;
    private Activity mainActivity = null;

    public UpdateActivity(Activity a,Context c) {
        this.mainActivity = a;
        pDialog = new ProgressDialog(c);
    }

    protected void onPreExecute() {
        super.onPreExecute();
        pDialog.setMessage("Lade die Aktualisierung herunter...");
        pDialog.setIndeterminate(false);
        pDialog.setMax(100);
        pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        int count;
        try {
            URL url = new URL("https://gymnasium-glinde.logoip.de/downloads/InfoApp.apk");
            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(sslSocketFactory);
            connection.connect();
            int lenghtOfFile = connection.getContentLength();
            InputStream input = new BufferedInputStream(url.openStream(),8192);
            OutputStream output = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+ "SchulinfoApp.apk");
            byte data[] = new byte[1024];
            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            Log.w("Your_Tag","Download Error", e);
        }
        return null;
    }
    protected void onProgressUpdate(String... progress) {
        this.pDialog.setProgress(Integer.parseInt(progress[0]));
    }
    @SuppressWarnings("deprecation")
    @Override
    protected void onPostExecute(String file_url) {
        pDialog.dismiss();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/SchulinfoAPP.apk")), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mainActivity.startActivity(intent);
    }

    private static TrustManager[] ggTrustMgr = new TrustManager[]{new X509TrustManager() {

        String pub_key = "fa095201ee4f03c32022f11b0c7352eba684d48c09220be0d26fa7c81c26d" +
                "120cbf0fe6c3bdf669de6dd04046c3146641e4131f2113e18b59c01673fe222323" +
                "8dcbd319e58939637affab79367ea3305b5f8ad6b723c6b1cadd5586cc108592d6" +
                "d5fcd7c927909c42c5be56ac54152efaa18557333fc84bfb2d18a182fc66604139" +
                "7873b991e8e6d37efb182c9afa5fcc841025d4d77e76ed9d49de89a0c20fc6eaa8" +
                "09c52c789f15fe6807ab1c61ac5908b427d0ca9012ef86fe18eaf5fef684954c2b" +
                "2e36e68d7b5f2a76500832df8a133e14a4b424bbd818da58f739da7a578e66dfe9" +
                "4ba16506e7c88c66ff25f7f90ac8b2c3f9f347d5b54351dfd971f29";

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {

        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            String recveived_pub_key = chain[0].getPublicKey().toString();
            String obtained_key = recveived_pub_key.split("\\{")[1].split("\\}")[0].split(",")[0].split("=")[1];
            if (!pub_key.equals(obtained_key)) {
                throw new CertificateException();
            }
        }
    }};

    private static SSLSocketFactory sslSocketFactory;
    static {
        try {
            SSLContext sc;
            sc = SSLContext.getInstance("TLS");
            sc.init(null, ggTrustMgr, new java.security.SecureRandom());
            sslSocketFactory = sc.getSocketFactory();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
