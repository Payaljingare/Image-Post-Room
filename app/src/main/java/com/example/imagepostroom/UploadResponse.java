package com.example.imagepostroom;

import java.util.Map;

public class UploadResponse {
    private Map<String, String> form;
    private Map<String, String> files;

    public Map<String, String> getForm() {
        return form;
    }

    public void setForm(Map<String, String> form) {
        this.form = form;
    }

    public Map<String, String> getFiles() {
        return files;
    }

    public void setFiles(Map<String, String> files) {
        this.files = files;
    }
}
