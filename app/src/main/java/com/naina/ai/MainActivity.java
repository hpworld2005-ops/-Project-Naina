package com.naina.ai;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private TextView chatHistory;
    private EditText inputText;
    private Button sendBtn;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatHistory = findViewById(R.id.chatHistory);
        inputText = findViewById(R.id.inputText);
        sendBtn = findViewById(R.id.sendBtn);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userMessage = inputText.getText().toString().trim();
                if (!userMessage.isEmpty()) {
                    chatHistory.append("You: " + userMessage + "\n");
                    inputText.setText("");
                    sendMessageToLocalLLM(userMessage);
                }
            }
        });
    }

    private void sendMessageToLocalLLM(final String prompt) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://127.0.0.1:8080/completion");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; utf-8");
                    conn.setDoOutput(true);

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("prompt", prompt);
                    jsonParam.put("n_predict", 128);

                    OutputStream os = conn.getOutputStream();
                    os.write(jsonParam.toString().getBytes("utf-8"));
                    os.flush();
                    os.close();

                    Scanner scanner = new Scanner(conn.getInputStream());
                    StringBuilder response = new StringBuilder();
                    while (scanner.hasNextLine()) {
                        response.append(scanner.nextLine());
                    }
                    scanner.close();

                    JSONObject resObj = new JSONObject(response.toString());
                    final String aiReply = resObj.optString("content", "No response");

                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            chatHistory.append("Naina: " + aiReply + "\n\n");
                        }
                    });

                } catch (final Exception e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            chatHistory.append("Naina (Error): " + e.getMessage() + "\n\n");
                        }
                    });
                }
            }
        }).start();
    }
}
