package tech.rectifier.fucknem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Uri data;
        String path;
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction()) && (data = intent.getData()) != null && (path = data.getPath()) != null) {
            //https://music.163.com/#/song?id=34324546&userid=
            //https://music.163.com/m/song?id=32526708&userid=
            //http://music.163.com/song/32526708/?userid=
            try {
                String fragment;
                if ("/".equals(path) && (fragment = data.getFragment()) != null) {
                    String type = fragment.substring(1, fragment.indexOf("?"));
                    Pattern r = Pattern.compile("[?,&]id=(\\d+)");
                    Matcher m = r.matcher(fragment);
                    if (m.find() && !TextUtils.isEmpty(type) && !TextUtils.isEmpty(m.group(1))) {
                        startNEM(type, m.group(1));
                    }
                } else if (path.startsWith("/m/")) {
                    String type = path.substring(3);
                    if (!TextUtils.isEmpty(type) && !TextUtils.isEmpty(data.getQueryParameter("id"))) {
                        startNEM(type, data.getQueryParameter("id"));
                    }
                } else {
                    Pattern r = Pattern.compile("/(.*)/(\\d+)");
                    Matcher m = r.matcher(path);
                    if (m.find() && !TextUtils.isEmpty(m.group(1)) && !TextUtils.isEmpty(m.group(2))) {
                        startNEM(m.group(1), m.group(2));
                    } else {
                        showErrorDialog(data);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog(data);
            }
        } else {
            finish();
        }
    }

    private void showErrorDialog(Uri data) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error_parse)
                .setMessage(data.toString())
                .setCancelable(false)
                .setPositiveButton(android.R.string.copyUrl, (dialog, which) -> {
                    try {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("link", data.toString());
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(clip);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finish();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> finish())
                .show();
    }

    private void startNEM(String type, String id) {
        Intent intent = new Intent();
        intent.setData(Uri.parse(String.format("orpheus://%s/%s", type, id)));
        intent.setAction(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error_open, Toast.LENGTH_LONG).show();
        }
        finish();
    }
}