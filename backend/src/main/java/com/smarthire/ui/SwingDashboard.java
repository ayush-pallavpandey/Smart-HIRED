package com.smarthire.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class SwingDashboard extends JFrame {
    private DefaultListModel<JsonNode> listModel = new DefaultListModel<>();
    private JList<JsonNode> resumeList = new JList<>(listModel);
    private JTextArea textArea = new JTextArea();
    private JLabel statusLabel = new JLabel("Ready");
    private ObjectMapper mapper = new ObjectMapper();

    private static final String BACKEND = "http://localhost:8080";

    public SwingDashboard() {
        super("SmartHire - Desktop Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        buildUI();
        loadResumes();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(10,10));
        root.setBorder(new EmptyBorder(10,10,10,10));
        JPanel left = new JPanel(new BorderLayout(8,8));

        // upload area
        JPanel uploadPanel = new JPanel();
        JButton chooseBtn = new JButton("Choose File...");
        JTextField filePath = new JTextField(20);
        JButton uploadBtn = new JButton("Upload");
        uploadPanel.add(chooseBtn);
        uploadPanel.add(filePath);
        uploadPanel.add(uploadBtn);
        left.add(uploadPanel, BorderLayout.NORTH);

        chooseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int ret = chooser.showOpenDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                filePath.setText(f.getAbsolutePath());
            }
        });

        uploadBtn.addActionListener(e -> {
            String p = filePath.getText();
            if (p == null || p.isBlank()) {
                statusLabel.setText("Choose a file first");
                return;
            }
            File f = new File(p);
            if (!f.exists()) {
                statusLabel.setText("File not found");
                return;
            }
            new Thread(() -> {
                try {
                    statusLabel.setText("Uploading...");
                    String resp = uploadFile(f);
                    statusLabel.setText("Upload response: " + resp);
                    loadResumes();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    statusLabel.setText("Upload failed: " + ex.getMessage());
                }
            }).start();
        });

        // list area
        resumeList.setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
            String fname = value.path("filename").asText("");
            String status = value.path("status").asText("");
            JLabel lbl = new JLabel(fname + "  (" + status + ")");
            lbl.setOpaque(true);
            lbl.setBackground(isSelected ? Color.LIGHT_GRAY : Color.WHITE);
            return lbl;
        });
        JScrollPane listScroll = new JScrollPane(resumeList);
        left.add(listScroll, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh");
        left.add(refreshBtn, BorderLayout.SOUTH);
        refreshBtn.addActionListener(e -> loadResumes());

        root.add(left, BorderLayout.WEST);

        // right side: details
        JPanel right = new JPanel(new BorderLayout(8,8));
        textArea.setEditable(false);
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        right.add(new JScrollPane(textArea), BorderLayout.CENTER);
        root.add(right, BorderLayout.CENTER);

        // bottom status
        root.add(statusLabel, BorderLayout.SOUTH);

        resumeList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                JsonNode selected = resumeList.getSelectedValue();
                if (selected != null) {
                    String text = "";
                    if (selected.has("textExtracted")) text = selected.path("textExtracted").asText("");
                    if (text.isBlank() && selected.has("text_extracted")) text = selected.path("text_extracted").asText("");
                    textArea.setText(text);
                }
            }
        });

        setContentPane(root);
    }

    private void loadResumes() {
        new Thread(() -> {
            try {
                statusLabel.setText("Loading resumes...");
                URL u = new URL(BACKEND + "/api/resumes");
                HttpURLConnection conn = (HttpURLConnection) u.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(10000);
                conn.setRequestMethod("GET");
                int code = conn.getResponseCode();
                InputStream in = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                JsonNode arr = mapper.readTree(in);
                SwingUtilities.invokeLater(() -> {
                    listModel.clear();
                    if (arr.isArray()) {
                        for (JsonNode n : arr) listModel.addElement(n);
                    }
                    statusLabel.setText("Loaded " + listModel.size() + " resumes");
                });
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> statusLabel.setText("Failed to load: " + e.getMessage()));
            }
        }).start();
    }

    private String uploadFile(File f) throws Exception {
        String boundary = "----SmartHireBoundary" + System.currentTimeMillis();
        URL url = new URL(BACKEND + "/api/resumes");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream out = conn.getOutputStream();
             BufferedOutputStream bos = new BufferedOutputStream(out)) {

            String fileHeader = "--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"file\"; filename=\"" + f.getName() + "\"\r\n"
                    + "Content-Type: application/octet-stream\r\n\r\n";
            bos.write(fileHeader.getBytes());
            Files.copy(f.toPath(), bos);
            bos.write("\r\n".getBytes());

            String end = "--" + boundary + "--\r\n";
            bos.write(end.getBytes());
            bos.flush();
        }

        int code = conn.getResponseCode();
        InputStream in = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        String resp = new String(in.readAllBytes());
        conn.disconnect();
        return resp;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SwingDashboard d = new SwingDashboard();
            d.setVisible(true);
        });
    }
}