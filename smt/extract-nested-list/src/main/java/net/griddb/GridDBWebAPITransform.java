package net.griddb;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.components.Versioned;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.transforms.field.FieldSyntaxVersion;
import org.apache.kafka.connect.transforms.field.SingleFieldPath;
import org.apache.kafka.connect.transforms.util.SimpleConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import static org.apache.kafka.connect.transforms.util.Requirements.requireMapOrNull;
import static org.apache.kafka.connect.transforms.util.Requirements.requireStructOrNull;

import org.apache.kafka.connect.transforms.Transformation;

public abstract class GridDBWebAPITransform<R extends ConnectRecord<R>> implements Transformation<R>, Versioned {
     private static final Logger log = LoggerFactory.getLogger(GridDBWebAPITransform.class);

    private static final String FIELD_CONFIG = "fields";

    private List<Field> fieldNames;

    //return new ConfigDef().define("fields", ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Comma-separated list of field names to extract.");

    public static final ConfigDef CONFIG_DEF = FieldSyntaxVersion.appendConfigTo(
            new ConfigDef().define(
                "fields", 
                ConfigDef.Type.STRING, 
                ConfigDef.Importance.HIGH, 
                "Comma-separated list of field names to extract.")
            );

    private static final String PURPOSE = "field extraction";

    private SingleFieldPath fieldPath;
    private String originalPath;

    @Override
    public String version() {
        return AppInfoParser.getVersion();
    }

    @Override
    public void configure(Map<String, ?> props) {
        final SimpleConfig config = new SimpleConfig(CONFIG_DEF, props);
        originalPath = config.getString(FIELD_CONFIG);
    }

    @Override
    public R apply(R record) {
        final Schema schema = operatingSchema(record);
        
        if (schema == null) {
            final Map<String, Object> value = requireMapOrNull(operatingValue(record), PURPOSE);
            return newRecord(record, null, value == null ? null : fieldPath.valueFrom(value));
        } else {
            final Struct value = requireStructOrNull(operatingValue(record), PURPOSE);
            fieldNames = schema.fields(); 

            List<List<Object>> nestedArray = new ArrayList<>();
            List<Object> row = new ArrayList<>();
            for (Field f : fieldNames) {
                String fName = f.name();
                SingleFieldPath fPath = new SingleFieldPath(fName, FieldSyntaxVersion.V2);
                row.add(fPath.valueFrom(value));
            }
            nestedArray.add(row);
    
            return newRecord(record, schema, value == null ? null : nestedArray);
        }
        
    }

    @Override
    public void close() {
    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }

    protected abstract Schema operatingSchema(R record);

    protected abstract Object operatingValue(R record);

    protected abstract R newRecord(R record, Schema updatedSchema, Object updatedValue);

    public static class Value<R extends ConnectRecord<R>> extends GridDBWebAPITransform<R> {
        @Override
        protected Schema operatingSchema(R record) {
            return record.valueSchema();
        }

        @Override
        protected Object operatingValue(R record) {
            return record.value();
        }

        @Override
        protected R newRecord(R record, Schema updatedSchema, Object updatedValue) {
            return record.newRecord(record.topic(), record.kafkaPartition(), record.keySchema(), record.key(), updatedSchema, updatedValue, record.timestamp());
        }
    }

}
