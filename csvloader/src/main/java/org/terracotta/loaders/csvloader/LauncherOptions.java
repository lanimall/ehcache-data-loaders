package org.terracotta.loaders.csvloader;

import com.lexicalscope.jewel.cli.Option;

/**
 * Created by Fabien Sanglier on 7/9/14.
 */
public interface LauncherOptions {
    public enum ParseType {rows, items}

    @Option(helpRequest = true)
    boolean getHelp();

    @Option(
            longName = "file",
            description = "CSV File path")
    String getFilePath();

    @Option(
            longName = "firstLineHeaderLine",
            description = "Is the first line in the CSV a header",
            defaultValue = "false"
    )
    boolean isFirstLineHeaderLine();

    @Option(
            longName = "delimiter",
            description = "CSV data delimiter",
            defaultValue = ",")
    char getCsvDelimiter();

    @Option(
            longName = "skipLines",
            description = "Number of lines to skip during parsing",
            defaultValue = "0")
    int getCsvSkipLines();

    @Option(
            longName = "dataStorageType",
            description = "How do you want to parse and store the data in cache? (rows: 1 cache entry = 1 row / items: 1 cache entry = 1 single csv item)",
            defaultValue = "rows" )
    ParseType getDataStorageType();

    @Option(
            longName = "cacheName",
            description = "Name of cache to load the data in"
    )
    String getCacheName();

    @Option(
            longName = "bulk",
            description = "Is cache bulk load enabled",
            defaultValue = "true"
    )
    boolean isBulkLoadEnabled();

    @Option(
            longName = "replaceAll",
            description = "Remove all entries first before re-importing",
            defaultValue = "false"
    )
    boolean isReplaceAll();
}
