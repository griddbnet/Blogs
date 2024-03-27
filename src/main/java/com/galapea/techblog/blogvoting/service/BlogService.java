package com.galapea.techblog.blogvoting.service;

import com.galapea.techblog.blogvoting.entity.Blog;
import com.galapea.techblog.blogvoting.model.CreateBlogRequest;
import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.RowSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BlogService {
    private final Logger log = LoggerFactory.getLogger(BlogService.class);
    Collection<String, Blog> blogCollection;

    public BlogService(Collection<String, Blog> blogCollection) {
        this.blogCollection = blogCollection;
    }

    public List<Blog> fetchAll() {
        List<Blog> blogs = new ArrayList<>(0);
        try {
            Query<Blog> query = blogCollection.query("SELECT *", Blog.class);
            RowSet<Blog> rowSet = query.fetch();
            while (rowSet.hasNext()) {
                blogs.add(rowSet.next());
            }
        } catch (GSException e) {
            log.error("Error fetchAll", e);
        }
        return blogs;
    }

    public Blog fetchOne(String title) throws GSException {
        Query<Blog> query = blogCollection.query("SELECT * WHERE title='" + title + "'", Blog.class);
        RowSet<Blog> rowSet = query.fetch();
        if (rowSet.hasNext()) {
            return rowSet.next();
        }
        return null;
    }

    public Blog create(CreateBlogRequest createBlogRequest) throws GSException {
        Blog blog = new Blog();
        blog.setId(KeyGenerator.next("bl"));
        blog.setTitle(createBlogRequest.getTitle());
        blog.setVoteDownCount(0);
        blog.setVoteUpCount(0);
        blog.setCreatedAt(new Date());
        blogCollection.put(blog);
        return blog;
    }

    public void updateVoteUp(String blogId) throws GSException {
        blogCollection.setAutoCommit(false);
        Blog blog = blogCollection.get(blogId, true);
        blog.setVoteUpCount(blog.getVoteUpCount() + 1);
        blogCollection.put(blog);
        blogCollection.commit();
    }

    public void updateVoteDown(String blogId) throws GSException {
        blogCollection.setAutoCommit(false);
        Blog blog = blogCollection.get(blogId, true);
        blog.setVoteDownCount(blog.getVoteDownCount() + 1);
        blogCollection.put(blog);
        blogCollection.commit();
    }
}
