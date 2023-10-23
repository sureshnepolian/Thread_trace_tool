package com.threadtrace.trace.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
public class ThreadDumpController {

    public static class ParsedThreadInfo {
        public String threadName;
        public String threadState = "";
        public int atCount = 0;
        public Map<String, String> methodClassMap = new HashMap<>();
        public List<String> unmatchedLines = new ArrayList<>();
    }

    @PostMapping("/threaddump")
    public ResponseEntity<List<ParsedThreadInfo>> parseThreadDump(@RequestBody String threadDump,
                                                                  @RequestParam(name = "threadNamePattern", required = false) String threadNamePattern) {
        List<ParsedThreadInfo> parsedStackTraces = new ArrayList<>();
        try {
            Pattern threadPattern = Pattern.compile("\"([^\"]+)\".*?(?=\\n\"|\\Z)", Pattern.DOTALL | Pattern.MULTILINE);
            Matcher threadMatcher = threadPattern.matcher(threadDump);

            Pattern classMethodPattern = Pattern.compile("^at\\s+((?:[a-zA-Z_$][a-zA-Z\\d_$]*\\.)*[a-zA-Z_$][a-zA-Z\\d_$]*)(?:\\$\\$Lambda\\$\\d+/\\d+)?\\.([a-zA-Z_$][a-zA-Z\\d_$]*)\\(", Pattern.MULTILINE);
            Pattern threadStatePattern = Pattern.compile("Thread State: (.+)");
            Pattern waitingPattern = Pattern.compile("- waiting on <(.+)> \\(a (.+)\\)");

            List<Map.Entry<String, String>> methodClassEntries = new ArrayList<>();

            while (threadMatcher.find()) {
                ParsedThreadInfo info = new ParsedThreadInfo();
                info.threadName = threadMatcher.group(1);

                // Use the optional threadNamePattern to filter threads
                if (threadNamePattern != null && !info.threadName.startsWith(threadNamePattern)) {
                    continue;
                }

                String allLines = threadMatcher.group().split("\n", 2)[1];

                Matcher stateMatcher = threadStatePattern.matcher(allLines);
                if (stateMatcher.find()) {
                    info.threadState = stateMatcher.group(1);
                }

                String[] lines = allLines.split("\\n");

                for (String line : lines) {
                    String trimmedLine = line.trim();
                    Matcher classMethodMatcher = classMethodPattern.matcher(trimmedLine);
                    Matcher waitingMatcher = waitingPattern.matcher(trimmedLine);
                    if (classMethodMatcher.find()) {
                        info.atCount++;
                        String fullClassName = classMethodMatcher.group(1);
                        String methodName = classMethodMatcher.group(2);

                        int lastDotIndex = fullClassName.lastIndexOf('.');
                        String simpleClassName = fullClassName;
                        if (lastDotIndex != -1) {
                            simpleClassName = fullClassName.substring(lastDotIndex + 1);
                        }

                        Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<>(simpleClassName, methodName);
                        methodClassEntries.add(entry);
                    } else if (waitingMatcher.find()) {
                        info.threadState += " (Waiting on " + waitingMatcher.group(2) + ")";
                    } else {
                        info.unmatchedLines.add(trimmedLine);
                    }
                }

                // Reverse the list to maintain the bottom-to-top order
                Collections.reverse(methodClassEntries);

                // Convert the list back to a map
                Map<String, String> methodClassMap = new LinkedHashMap<>();
                for (Map.Entry<String, String> entry : methodClassEntries) {
                    methodClassMap.put(entry.getKey(), entry.getValue());
                }

                info.methodClassMap = methodClassMap;

                // Clear methodClassEntries for the next iteration
                methodClassEntries.clear();

                parsedStackTraces.add(info);
            }

            return new ResponseEntity<>(parsedStackTraces, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}