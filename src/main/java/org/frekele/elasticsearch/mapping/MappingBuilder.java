package org.frekele.elasticsearch.mapping;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.frekele.elasticsearch.mapping.annotations.ElasticDocument;
import org.frekele.elasticsearch.mapping.annotations.ElasticTextField;
import org.frekele.elasticsearch.mapping.exceptions.InvalidDocumentClassException;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MappingBuilder implements Serializable {

    private List<Class> docsClass;

    private XContentBuilder mapping;

    public MappingBuilder(Class... documentClass) {
        if (documentClass != null && documentClass.length > 0) {
            this.docsClass = Arrays.asList(documentClass);
        } else {
            this.docsClass = new ArrayList<>(0);
        }
        this.validateElasticDocument();
    }

    static boolean isElasticDocument(Class documentClass) {
        return (documentClass.isAnnotationPresent(ElasticDocument.class));
    }

    void validateElasticDocument() {
        for (Class clazz : this.docsClass) {
            if (!isElasticDocument(clazz)) {
                throw new InvalidDocumentClassException("Document Class[" + clazz.getCanonicalName() + "] Invalid. @ElasticDocument must be present.");
            }
        }
    }

    public XContentBuilder source() throws IOException {
        if (this.mapping == null) {
            return build();
        } else {
            return this.mapping;
        }
    }

    public String sourceAsString() throws IOException {
        return this.source().string();
    }

    XContentBuilder build() throws IOException {
        if (this.mapping == null) {
            this.mapping = XContentFactory.jsonBuilder();
            this.mapping.prettyPrint();
            //BEGIN
            this.mapping.startObject();
            this.mapping.startObject("mappings");

            for (Class clazz : this.docsClass) {
                ElasticDocument elasticDocument = (ElasticDocument) clazz.getAnnotation(ElasticDocument.class);
                this.mapping.startObject(elasticDocument.value());
                Field[] fields = clazz.getDeclaredFields();

                if (fields != null && fields.length > 0) {
                    this.mapping.startObject("properties");

                    for (Field field : fields) {
                        if (field.isAnnotationPresent(ElasticTextField.class)) {
                            ElasticTextField elasticField = field.getAnnotation(ElasticTextField.class);
                            this.mapping.startObject(field.getName());
                            this.mapping.field("type", ElasticTextField.type.getName());
                            if (!elasticField.analyzer().isEmpty()) {
                                this.mapping.field("analyzer", elasticField.analyzer());
                            }
                            //Default "fielddata" : false
                            if (elasticField.fielddata()) {
                                this.mapping.field("fielddata", elasticField.fielddata());
                            }
                            if (elasticField.fielddataFrequencyFilter().enabled()) {
                                this.mapping.startObject("fielddata_frequency_filter");
                                this.mapping.field("min", elasticField.fielddataFrequencyFilter().min());
                                this.mapping.field("max", elasticField.fielddataFrequencyFilter().max());
                                this.mapping.field("min_segment_size", elasticField.fielddataFrequencyFilter().minSegmentSize());
                                //fielddata_frequency_filter
                                this.mapping.endObject();
                            }
                            this.mapping.field("index", elasticField.index());
                            if (!elasticField.indexOptions().isEmpty()) {
                                this.mapping.field("index_options", elasticField.indexOptions());
                            }
                            this.mapping.field("norms", elasticField.norms());
                            this.mapping.field("positionIncrementGap", elasticField.positionIncrementGap());
                            this.mapping.field("store", elasticField.store());

                            if (!elasticField.searchAnalyzer().isEmpty()) {
                                this.mapping.field("search_analyzer", elasticField.searchAnalyzer());
                            }
                            if (!elasticField.searchQuoteAnalyzer().isEmpty()) {
                                this.mapping.field("search_quote_analyzer", elasticField.searchQuoteAnalyzer());
                            }
                            if (!elasticField.similarity().isEmpty()) {
                                this.mapping.field("similarity", elasticField.similarity());
                            }
                            if (!elasticField.termVector().isEmpty()) {
                                this.mapping.field("term_vector", elasticField.termVector());
                            }

                            //class type
                            this.mapping.endObject();
                        }
                    }

                    //properties
                    this.mapping.endObject();
                }

                //ElasticDocument
                this.mapping.endObject();
            }

            //mappings
            this.mapping.endObject();
            //END
            this.mapping.endObject();
        }
        return this.mapping;
    }

    public XContentBuilder old() throws IOException {
        XContentBuilder mapping = XContentFactory.jsonBuilder().startObject().startObject("type1")
            .startObject("properties")
            .startObject("text").field("type", "text").field("analyzer", "keyword").endObject()
            .endObject()
            .endObject().endObject();
        return mapping;
    }
}