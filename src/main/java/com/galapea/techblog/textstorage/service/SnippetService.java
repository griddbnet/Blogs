package com.galapea.techblog.textstorage.service;

import com.galapea.techblog.textstorage.AppConstant;
import com.galapea.techblog.textstorage.entity.Snippet;
import com.galapea.techblog.textstorage.entity.Storage;
import com.galapea.techblog.textstorage.model.CreateSnippet;
import com.galapea.techblog.textstorage.model.EditSnippet;
import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.RowSet;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SnippetService {
    private final Logger log = LoggerFactory.getLogger(SnippetService.class);
    private final Collection<String, Snippet> snippetCollection;
    private final Collection<String, Storage> storagCollection;
    // private final Collection<String, User> userCollection;

    private static DecimalFormat DEC_FORMAT = new DecimalFormat("#.##");

    private static String formatSize(long size, long divider, String unitName) {
        return DEC_FORMAT.format((double) size / divider) + " " + unitName;
    }

    public static String toHumanReadableByNumOfLeadingZeros(long size) {
        if (size < 0) {
            throw new IllegalArgumentException("Invalid file size: " + size);
        }
        if (size < 1024) return size + " Bytes";
        int unitIdx = (63 - Long.numberOfLeadingZeros(size)) / 10;
        return formatSize(size, 1L << (unitIdx * 10), " KMGTPE".charAt(unitIdx) + "B");
    }

    public List<Snippet> fetchAll() {
        List<Snippet> snippets = new ArrayList<>(0);
        try {
            String tql = "SELECT * FROM " + AppConstant.SNIPPETS_CONTAINER + " ORDER BY createdAt DESC";
            Query<Snippet> query = snippetCollection.query(tql);
            RowSet<Snippet> rowSet = query.fetch();
            while (rowSet.hasNext()) {
                snippets.add(rowSet.next());
            }
        } catch (GSException e) {
            log.error("Error fetch all snippet", e);
        }
        return snippets;
    }

    public Snippet fetchOneByTitle(String title) throws GSException {
        Query<Snippet> query = snippetCollection.query("SELECT * WHERE title='" + title + "'", Snippet.class);
        RowSet<Snippet> rowSet = query.fetch();
        if (rowSet.hasNext()) {
            return rowSet.next();
        }
        return null;
    }

    public void create(CreateSnippet createSnippet) {
        try {
            Snippet found = fetchOneByTitle(createSnippet.getTitle());
            if (found != null) {
                return;
            }
            Blob content = snippetCollection.createBlob();
            content.setBytes(1, createSnippet.getContent().getBytes());
            Storage storage = new Storage();
            storage.setId(KeyGenerator.next("obj"));
            storage.setContent(content);
            storagCollection.put(storage.getId(), storage);
            Snippet snippet = new Snippet();
            snippet.setTitle(createSnippet.getTitle());
            snippet.setStorageId(storage.getId());
            snippet.setUserId(createSnippet.getUserId());
            snippet.setCreatedAt(new Date());
            snippet.setContentSizeHumanReadable(toHumanReadableByNumOfLeadingZeros(
                    createSnippet.getContent().getBytes().length));
            snippetCollection.put(KeyGenerator.next("sn"), snippet);
        } catch (GSException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Snippet fetchOne(String id) {
        Query<Snippet> query;
        try {
            query = snippetCollection.query("SELECT * WHERE id='" + id + "'", Snippet.class);
            RowSet<Snippet> rowSet = query.fetch();
            if (rowSet.hasNext()) {
                return rowSet.next();
            }
        } catch (GSException e) {
            log.error("Error fetch a Snippet", e);
        }
        return null;
    }

    public Storage fetchStorage(String id) {
        Query<Storage> query;
        try {
            query = storagCollection.query("SELECT * WHERE id='" + id + "'", Storage.class);
            RowSet<Storage> rowSet = query.fetch();
            if (rowSet.hasNext()) {
                return rowSet.next();
            }
        } catch (GSException e) {
            log.error("Error fetch a Snippet", e);
        }
        return null;
    }

    public void update(EditSnippet editSnippet) {
        Snippet snippet = fetchOne(editSnippet.getId());
        if (snippet != null) {
            snippet.setTitle(editSnippet.getTitle());
            snippet.setContentSizeHumanReadable(
                    toHumanReadableByNumOfLeadingZeros(editSnippet.getContent().getBytes().length));
            try {
                Storage storage = storagCollection.get(snippet.getStorageId());
                storage.getContent().setBytes(1, editSnippet.getContent().getBytes());
                storagCollection.put(storage.getId(), storage);
                snippetCollection.put(snippet);
            } catch (GSException e) {
                log.error("Error update a Snippet: {}", editSnippet.getId());
                e.printStackTrace();
            } catch (SQLException e) {
                log.error("Error write bytes of Snippet: {}", editSnippet.getId());
                e.printStackTrace();
            }
        }
    }
}
