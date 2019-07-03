package org.guild.dataprocessor.processor;

import org.codehaus.jettison.json.JSONArray;
import org.guild.dataprocessor.datatypes.Mapper;
import org.guild.dataprocessor.datatypes.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.util.Properties;

public class MetadataProcessor {

    private static final Logger LOG  = LoggerFactory.getLogger(MetadataProcessor.class);

    public static boolean process(Properties config) {
        String metadataPath = config.getProperty("movie.metadata.file");
        String metadataDelimiter = config.getProperty("movie.metadata.delimiter");
        String ratingsPath = config.getProperty("movie.ratings.file");
        String ratingsDelimiter = config.getProperty("movie.ratings.delimiter");
        LOG.info("Processing metadata: " + metadataPath + " \n" +
                "Ratings: " + ratingsPath);
        Mapper mapper = new Mapper();
        try {
            getFilmMetadata(metadataPath, metadataDelimiter, mapper);
            getRatingsMetadata(ratingsPath, ratingsDelimiter, mapper);
            File output = new File(config.getProperty("mapped.output.path"));
            try (BufferedWriter fw = new BufferedWriter(new FileWriter(output))) {
                LOG.info("Writing out metadata JSON");
                mapper.toJson().write(fw);
            }
            return true;
        } catch (Exception e) {
            LOG.error("Unable to parse movie data: " + e);
            return false;
        }
    }

    private static void getFilmMetadata(String path, String delimiter, Mapper mapper) throws Exception {
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] splitLine = line.split(delimiter);
                Metadata metadata = new Metadata(Integer.valueOf(splitLine[5]));
                metadata.setBudget(Long.valueOf(splitLine[2]));
                metadata.setGenre(splitLine[3]);
                metadata.setRevenue(Long.valueOf(splitLine[15]));
                metadata.setPopularity(Float.valueOf(splitLine[22]));
                metadata.setCompany(splitLine[11]);
                metadata.setDate(splitLine[14]);
                mapper.addItem(metadata);
            }
        }
    }

    private static void getRatingsMetadata(String path, String delimiter, Mapper mapper) throws Exception {
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(new File(path)))) {
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] splitLine = line.split(delimiter);
                Metadata metadata = mapper.getItem(Integer.valueOf(splitLine[1]));
                if (metadata != null) {
                    metadata.setRating(Float.valueOf(splitLine[2]));
                }
            }
        }
    }
}