package com.threadtrace.trace.controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        String stackTrace = "  at com.smarsh.fetch.service.processor.queue.FetchJobWorker.newFlow(FetchJobWorker.java:81)\n" +
                "  at com.smarsh.fetch.service.processor.queue.FetchJobWorker.onMessage(FetchJobWorker.java:77)\n" +
                "  at com.smarsh.fetch.service.processor.queue.FetchJobWorker.onMessage(FetchJobWorker.java:37)";

        Pattern pattern = Pattern.compile("at\\s+([\\w.]+)\\.([\\w]+)\\(");

        String[] lines = stackTrace.split("\\n");
        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String fullClassName = matcher.group(1);
                String[] parts = fullClassName.split("\\.");
                String className = parts[parts.length - 1];
                String methodName = matcher.group(2);
                System.out.println("Method: " + methodName + ", Class: " + className);
            }
        }
    }
}


