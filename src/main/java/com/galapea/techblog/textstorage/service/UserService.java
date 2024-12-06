package com.galapea.techblog.textstorage.service;

import com.galapea.techblog.textstorage.entity.User;
import com.galapea.techblog.textstorage.model.CreateUser;
import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.RowSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    Collection<String, User> userCollection;

    public UserService(Collection<String, User> userCollection) {
        this.userCollection = userCollection;
    }

    public List<User> fetchAll() {
        List<User> users = new ArrayList<>(0);
        try {
            Query<User> query = userCollection.query("SELECT *", User.class);
            RowSet<User> rowSet = query.fetch();
            while (rowSet.hasNext()) {
                users.add(rowSet.next());
            }
        } catch (GSException e) {
            log.error("Error fetchAll", e);
        }
        return users;
    }

    public User getRandomUser() {
        List<User> users = fetchAll();
        int idx = ThreadLocalRandom.current().nextInt(0, users.size());
        return users.get(idx);
    }

    public User fetchOneByEmail(String email) throws GSException {
        Query<User> query = userCollection.query("SELECT * WHERE email='" + email + "'", User.class);
        RowSet<User> rowSet = query.fetch();
        if (rowSet.hasNext()) {
            return rowSet.next();
        }
        return null;
    }

    public User fetchOneById(String userId) throws GSException {
        Query<User> query = userCollection.query("SELECT * WHERE id='" + userId + "'", User.class);
        RowSet<User> rowSet = query.fetch();
        if (rowSet.hasNext()) {
            return rowSet.next();
        }
        return null;
    }

    public void create(CreateUser createUser) throws GSException {
        if (fetchOneByEmail(createUser.getEmail()) == null) {
            User user = new User();
            user.setEmail(createUser.getEmail());
            user.setFullName(createUser.getFullName());
            user.setCreatedAt(new Date());
            userCollection.put(KeyGenerator.next("usr"), user);
        }
    }
}
