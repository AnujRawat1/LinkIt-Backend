package com.Backend.LinkitBackend.Services;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import java.io.InputStream;

@Service
public class GridFsService {
    @Autowired
    private MongoTemplate mongoTemplate;

    public String storeFile(InputStream inputStream, String fileName, String contentType) {
        GridFSBucket gridFSBucket = GridFSBuckets.create(mongoTemplate.getDb());
        ObjectId fileId = gridFSBucket.uploadFromStream(fileName, inputStream, new GridFSUploadOptions().metadata(new Document("contentType", contentType)));
        return fileId.toString();
    }

    public InputStream downloadFile(String gridFsId) {
        GridFSBucket gridFSBucket = GridFSBuckets.create(mongoTemplate.getDb());
        return gridFSBucket.openDownloadStream(new ObjectId(gridFsId));
    }

    public GridFSFile getFileMetadata(String gridFsId) {
        GridFSBucket gridFSBucket = GridFSBuckets.create(mongoTemplate.getDb());
        return gridFSBucket.find(Filters.eq("_id", new ObjectId(gridFsId))).first();
    }

    public void deleteFile(String gridFsId) {
        GridFSBucket gridFSBucket = GridFSBuckets.create(mongoTemplate.getDb());
        gridFSBucket.delete(new ObjectId(gridFsId));
    }
}