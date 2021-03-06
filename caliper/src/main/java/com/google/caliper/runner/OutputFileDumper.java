// Copyright 2011 Google Inc. All Rights Reserved.

package com.google.caliper.runner;

import com.google.caliper.model.NewTrial;
import com.google.caliper.model.Run;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * {@link ResultProcessor} that dumps the given {@link com.google.caliper.model.Run} to a file in
 * JSON format. The output file may be specified via {@link CaliperOptions#outputFileOrDir}. If this
 * is unspecified, the output will be dumped to a file called
 * {@code ./caliper-results/[benchmark classname].[timestamp].json}; if it exists and is a file,
 * the file will be overwritten; if it exists and is a directory, the output will be dumped to a
 * file in that directory called {@code [benchmark classname].[timestamp].json}; otherwise, we'll
 * create a file with the given name and dump the output to that file.
 */
final class OutputFileDumper implements ResultProcessor {
  private String outputFileOrDir;
  private String benchmarkName;

  OutputFileDumper(String outputFileOrDir, String benchmarkName) {
    this.outputFileOrDir = outputFileOrDir;
    this.benchmarkName = benchmarkName;
  }

  @Override public void processRun(Run results) {
    try {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      Files.write(gson.toJson(results), getOutputFile(), Charsets.UTF_8);
    } catch (IOException ioe) {
      System.err.println("ERROR: Unable to write json output: " + ioe.getMessage());
    }
  }

  private File getOutputFile() {
    if (outputFileOrDir == null) {
      File dir = new File("caliper-results");
      dir.mkdirs();
      return new File(dir, createFileName(benchmarkName));
    } else {
      File fileOrDir = new File(outputFileOrDir);
      if (fileOrDir.exists() && fileOrDir.isDirectory()) {
        return new File(fileOrDir, createFileName(benchmarkName));
      } else {
        // assume this is a file
        File parent = fileOrDir.getParentFile();
        if (parent != null) {
          parent.mkdirs();
        }
        return fileOrDir;
      }
    }
  }

  private String createFileName(String benchmarkName) {
    String timestamp = createTimestamp();
    return String.format("%s.%s.json", benchmarkName, timestamp);
  }

  private static final String FILE_NAME_DATE_FORMAT = "yyyy-MM-dd'T'HH-mm-ssZ";

  private String createTimestamp() {
    SimpleDateFormat dateFormat = new SimpleDateFormat(FILE_NAME_DATE_FORMAT, Locale.US);
    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    dateFormat.setLenient(true);
    return dateFormat.format(new Date());
  }

  @Override public void processTrial(NewTrial trial) {
    // TODO(gak): implement this when we migrate to NewRun
  }

  @Override public void close() {}
}
