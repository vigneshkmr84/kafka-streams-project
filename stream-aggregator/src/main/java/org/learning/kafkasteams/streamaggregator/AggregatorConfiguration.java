package org.learning.kafkasteams.streamaggregator;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;



@Component
@Slf4j
public class AggregatorConfiguration {

    JsonParser parser = new JsonParser();

    @Autowired
    Environment environment;

    @Autowired
    KafkaProducer kafkaProducer;

    @Autowired
    public void process(StreamsBuilder builder) {

        final String inputTopic = environment.getProperty("kafka.stream.topic");
        final Serde<String> stringSerde = Serdes.String();
        final Serde<Long> longSerde = Serdes.Long();
        final Serde<Integer> integerSerde = Serdes.Integer();
        //final Serde<Movies> movieSerde = Serdes.serdeFrom(Movies.class);

        // KSTREAM will stream the input topic, from where we will read the messages

        // reading messages as stream from topic movies-dump
        KStream<String, String> kStream = builder.stream(inputTopic, Consumed.with(stringSerde, stringSerde));
        //KStream<String, Movies> kStream = builder.stream(inputTopic, Consumed.with(stringSerde, movieSerde));

        KGroupedStream<String, String> kStream1 = kStream.groupBy( (k, v) -> {
            Movies m = parser.parse(v);
            return m.getDirector();
            },
                // this Serdes Config is for the kStream1 grouping (post grouping)
                Grouped.with(stringSerde, stringSerde));

        // post grouping, perform the count & move it to a kTable
        KTable<String, Long> kTable = kStream1.count();

        // Once
        kTable.toStream().print(Printed.<String, Long>toSysOut().withLabel("director-count"));

    /*
KTable<String, Long> kTable = kStream1.count();

        kTable.toStream().print(Printed.<String, Long>toSysOut().withLabel("director-count"));*/

        /*KTable<String, Long> directorCount  = kStream
                .flatMapValues( value -> Arrays.asList(value) )
                .groupBy( (key, value) -> value).count();


        kStream.groupBy( (key, value) -> {
            Movies m = parser.parse(value);
            m.
        })*/



        /*kStream.filter((key, jsonMovie) -> {
            Movies movie = parser.parse(jsonMovie);

            if (movie.getImdbRating() >= 7) {
                log.info("Movie [" + movie.getTitle() + "] is a good movie");

                kafkaProducer.produce("good-movies", String.valueOf(movie.getImdbID()), jsonMovie);
            } else {
                log.info("Movie [" + movie.getTitle() + "] is not a good movie");
                kafkaProducer.produce("bad-movies", String.valueOf(movie.getImdbID()), jsonMovie);
            }
            return movie.getImdbRating() >= 7;
        });*/
    }

}
