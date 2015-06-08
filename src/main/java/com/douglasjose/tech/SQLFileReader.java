package com.douglasjose.tech;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Douglas José (douglasjose@gmail.com)
 */
public class SQLFileReader {

    private static final Logger LOG = LoggerFactory.getLogger(SQLFileReader.class);

    private final Properties queries = new Properties();

    public SQLFileReader(InputStream is) {
        parseFile(is);
        if (LOG.isInfoEnabled()) {
            logConfiguration();
        }
    }

    private void parseFile(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            String queryName = null;
            List<String> queryLines = null;
            while ((line = nextLine(reader)) != null) {
                if (isQueryName(line)) {
                    registerQuery(queryName, queryLines);
                    queryName = extractQueryName(line);
                    queryLines = new ArrayList<>();
                } else {
                    // Query line
                    if (queryLines != null) {
                        queryLines.add(line);
                    }
                }
            }
            registerQuery(queryName, queryLines);
        } catch (IOException e) {
            throw new IllegalArgumentException("SQL file could not be read", e);
        }
    }

    private void registerQuery(String queryName, List<String> queryLines) {
        if (queryName != null && !queryName.trim().isEmpty() && queryLines != null && !queryLines.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String queryLine: queryLines) {
                sb.append(queryLine).append('\n');
            }
            queries.setProperty(queryName, sb.substring(0, sb.length() - 1));
        }
    }

    private String extractQueryName(String line) {
        Matcher matcher = Pattern.compile("#(\\w+)").matcher(line);
        return matcher.find() ? matcher.group(1) : null;
    }

    private boolean isQueryLine(String line) {
        return !line.trim().isEmpty() && !line.matches("^--.*");
    }

    private boolean isQueryName(String line) {
        return line.matches("^--\\s*#\\w+");
    }

    /**
     * Reads next valid line from file
     * @param reader File reader
     * @return Next valid line, or <code>null</code> if EOF.
     * @throws IOException If the file could not be read
     */
    private String nextLine(BufferedReader reader) throws IOException {
        String line;
        do {
            line = reader.readLine();
        } while (line != null && !isValidLine(line) );
        return line;
    }

    private boolean isValidLine(String line) {
        return isQueryLine(line) || isQueryName(line);
    }

    public Set<String> queryNames() {
        return queries.stringPropertyNames();
    }

    public String query(String name) {
        return queries.getProperty(name);
    }

    private void logConfiguration() {
        SortedSet<String> queryNames = new TreeSet<>(queryNames());
        LOG.info("{} {} initialized: {}", queryNames.size(), queryNames.size() > 1 ? "queries" : "query" , queryNames);
    }

}