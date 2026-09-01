package com.naina.ai;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private TextView chatHistory;
    private EditText inputText;
    private Button sendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        chatHistory = findViewById(R.id.chatHistory);
        inputText = findViewById(R.id.inputText);
        sendBtn = findViewById(R.id.sendBtn);

        sendBtn.setOnClickListener(v -> {
            String prompt = inputText.getText().toString().trim();
            if (!prompt.isEmpty()) {
                chatHistory.append("You: " + prompt + "\n\n");
                inputText.setText("");
                sendToModel(prompt);
            }
        });
    }

    private void sendToModel(String prompt) {
        new Thread(() -> {
            try {
                URL url = new URL("http://127.0.0.1:8080/completion");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject json = new JSONObject();
                json.put("prompt", "User: " + prompt + "\nNaina:");
                json.put("n_predict", 128);

                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.flush();
                os.close();

                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNextLine()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                JSONObject resJson = new JSONObject(response.toString());
                String reply = resJson.optString("content", "No reply");

                runOnUiThread(() -> chatHistory.append("Naina: " + reply.trim() + "\n\n"));
            } catch (Exception e) {
                runOnUiThread(() -> chatHistory.append("Error: Model connect nahi hua. Make sure Termux server is running.\n\n"));
            }
        }).start();
    }
}
