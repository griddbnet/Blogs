package com.galapea.techblog.tetextstorage.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.galapea.techblog.textstorage.entity.Snippet;
import com.galapea.techblog.textstorage.service.KeyGenerator;
import com.galapea.techblog.textstorage.service.SnippetService;
import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.RowSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class SnippetServiceTest {

    @InjectMocks
    private SnippetService snippetService;

    @Mock
    private Collection<String, Snippet> snippetCollection;

    @Mock
    private Query<Snippet> query;

    @Mock
    private RowSet<Snippet> rowSet;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testfetchAll() throws GSException {
        when(snippetCollection.query(anyString())).thenReturn(query);
        when(query.fetch()).thenReturn(rowSet);
        when(rowSet.hasNext()).thenReturn(true, true, false);

        when(rowSet.next()).thenReturn(new Snippet(), new Snippet());

        List<Snippet> snippets = snippetService.fetchAll();

        assertEquals(2, snippets.size());
    }

    public void testfetchOne() throws GSException {
        String id = KeyGenerator.next("xyz");
        when(snippetCollection.query(anyString())).thenReturn(query);
        when(query.fetch()).thenReturn(rowSet);
        when(rowSet.hasNext()).thenReturn(true, false);

        Snippet snippet = new Snippet();
        snippet.setId(id);
        when(rowSet.next()).thenReturn(snippet);

        Snippet found = snippetService.fetchOne(id);

        assertEquals(snippet.getId(), found.getId());
    }

    public void testfetchOneByTitle() throws GSException {
        String id = KeyGenerator.next("xyz");
        when(snippetCollection.query(anyString())).thenReturn(query);
        when(query.fetch()).thenReturn(rowSet);
        when(rowSet.hasNext()).thenReturn(true, false);

        Snippet snippet = new Snippet();
        snippet.setId(id);
        String title = "the one technology";
        snippet.setTitle(title);
        when(rowSet.next()).thenReturn(snippet);

        Snippet found = snippetService.fetchOneByTitle(title);

        assertEquals(snippet.getTitle(), found.getTitle());
        assertEquals(snippet.getId(), found.getId());
    }
}
