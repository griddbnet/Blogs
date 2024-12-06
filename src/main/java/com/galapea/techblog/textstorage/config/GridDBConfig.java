package com.galapea.techblog.textstorage.config;

import com.galapea.techblog.textstorage.AppConstant;
import com.galapea.techblog.textstorage.entity.Snippet;
import com.galapea.techblog.textstorage.entity.Storage;
import com.galapea.techblog.textstorage.entity.User;
import com.toshiba.mwcloud.gs.*;
import java.util.Properties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GridDBConfig {

    @Value("${GRIDDB_NOTIFICATION_MEMBER}")
    private String notificationMember;

    @Value("${GRIDDB_CLUSTER_NAME}")
    private String clusterName;

    @Value("${GRIDDB_USER}")
    private String user;

    @Value("${GRIDDB_PASSWORD}")
    private String password;

    @Bean
    public GridStore gridStore() throws GSException {
        Properties properties = new Properties();
        properties.setProperty("notificationMember", notificationMember);
        properties.setProperty("clusterName", clusterName);
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        GridStore store = GridStoreFactory.getInstance().getGridStore(properties);

        /**If you try to save an object that is different from the one used to create the collection,
         * the following error will occur: com.toshiba.mwcloud.gs.common.GSStatementException: [60016:DS_DS_CHANGE_SCHEMA_DISABLE]
         * Just delete the collection and redefine it and it should be ok.**/
        // store.dropCollection(AppConstant.SNIPPETS_CONTAINER);
        return store;
    }

    @Bean
    public Collection<String, User> userCollection(GridStore gridStore) throws GSException {
        Collection<String, User> collection = gridStore.putCollection(AppConstant.USERS_CONTAINER, User.class);
        collection.createIndex("email");
        return collection;
    }

    @Bean
    public Collection<String, Snippet> snippetCollection(GridStore gridStore) throws GSException {
        Collection<String, Snippet> snippetCollection =
                gridStore.putCollection(AppConstant.SNIPPETS_CONTAINER, Snippet.class);
        snippetCollection.createIndex("userId");
        snippetCollection.createIndex("title");
        return snippetCollection;
    }

    @Bean
    public Collection<String, Storage> storageCollection(GridStore gridStore) throws GSException {
        Collection<String, Storage> storageCollection =
                gridStore.putCollection(AppConstant.STORAGES_CONTAINER, Storage.class);
        return storageCollection;
    }
}
