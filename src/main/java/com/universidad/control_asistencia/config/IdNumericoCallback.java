package com.universidad.control_asistencia.config;

import com.universidad.control_asistencia.model.ConIdNumerico;
import org.bson.Document;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Antes de guardar un documento nuevo (id vacío), le asigna el siguiente número
 * de su colección. Los contadores viven en la colección "contadores":
 * { _id: "asistencias", seq: 5342 }  →  la próxima asistencia será la 5343.
 * Es el equivalente al AUTO_INCREMENT de MySQL.
 */
@Component
public class IdNumericoCallback implements BeforeConvertCallback<Object> {

    private final MongoOperations mongo;

    public IdNumericoCallback(@Lazy MongoOperations mongo) {
        this.mongo = mongo;
    }

    @Override
    public Object onBeforeConvert(Object entidad, String coleccion) {
        if (entidad instanceof ConIdNumerico doc && doc.getId() == null) {
            Document contador = mongo.findAndModify(
                    Query.query(Criteria.where("_id").is(coleccion)),
                    new Update().inc("seq", 1L),
                    FindAndModifyOptions.options().returnNew(true).upsert(true),
                    Document.class,
                    "contadores");
            doc.setId(((Number) contador.get("seq")).longValue());
        }
        return entidad;
    }
}
