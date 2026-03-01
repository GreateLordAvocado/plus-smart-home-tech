package ru.yandex.practicum.kafka.telemetry.analyzer.kafka;

import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.DatumReader;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;

public class AvroBytesDeserializer {

    public <T extends SpecificRecordBase> T deserialize(byte[] bytes, Class<T> clazz) {
        try {
            T empty = clazz.getDeclaredConstructor().newInstance();
            SpecificData specificData = empty.getSpecificData();

            DatumReader<T> reader = new SpecificDatumReader<>(
                    empty.getSchema(),
                    empty.getSchema(),
                    specificData
            );

            Decoder decoder = DecoderFactory.get().binaryDecoder(bytes, null);
            return reader.read(null, decoder);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to deserialize Avro for " + clazz.getSimpleName(), e);
        }
    }
}
