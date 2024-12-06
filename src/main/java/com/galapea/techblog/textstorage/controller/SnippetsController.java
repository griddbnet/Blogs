package com.galapea.techblog.textstorage.controller;

import com.galapea.techblog.textstorage.config.NotFoundException;
import com.galapea.techblog.textstorage.entity.Snippet;
import com.galapea.techblog.textstorage.entity.Storage;
import com.galapea.techblog.textstorage.entity.User;
import com.galapea.techblog.textstorage.model.CreateSnippet;
import com.galapea.techblog.textstorage.model.EditSnippet;
import com.galapea.techblog.textstorage.model.SnippetView;
import com.galapea.techblog.textstorage.service.SnippetService;
import com.galapea.techblog.textstorage.service.UserService;
import com.toshiba.mwcloud.gs.GSException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/snippets")
@RequiredArgsConstructor
public class SnippetsController {
    private static final Logger log = LoggerFactory.getLogger(SnippetsController.class);
    private final SnippetService snippetService;
    private final UserService userService;

    @GetMapping
    String snippets(Model model) {
        List<Snippet> snippets = snippetService.fetchAll();
        List<SnippetView> snippetViews = snippets.stream()
                .map(sn -> SnippetView.builder()
                        .id(sn.getId())
                        .title(sn.getTitle())
                        .createdAt(sn.getCreatedAt())
                        .timeAgo(calculateTimeAgoByTimeGranularity(sn.getCreatedAt(), TimeGranularity.MINUTES))
                        .contentSizeHumanReadable(sn.getContentSizeHumanReadable())
                        .build())
                .collect(Collectors.toList());

        List<User> users = userService.fetchAll();
        model.addAttribute("snippets", snippetViews);
        model.addAttribute("users", users);
        return "snippets";
    }

    @GetMapping("/new")
    String newSnippet(Model model) {
        List<User> users = userService.fetchAll();
        model.addAttribute("snippet", new CreateSnippet());
        model.addAttribute("users", users);
        return "new_snippet";
    }

    @PostMapping("/save")
    String saveSnippet(@ModelAttribute("snippet") CreateSnippet createSnippet) {
        snippetService.create(createSnippet);
        return "redirect:/snippets";
    }

    @GetMapping("/{id}")
    String view(Model model, @PathVariable("id") String id) throws GSException, SQLException {
        Snippet snippet = snippetService.fetchOne(id);
        if (snippet == null) {
            throw new NotFoundException();
        }

        log.info("Fetched snippet: {}", snippet);
        Storage storage = snippetService.fetchStorage(snippet.getStorageId());
        if (storage == null) {
            log.error("Storage not found for snippet: {}", snippet);
            return "redirect:/snippets";
        }

        String content = new String(
                storage.getContent().getBytes(1l, (int) storage.getContent().length()));
        EditSnippet editSnippet = new EditSnippet();
        editSnippet.setTitle(snippet.getTitle());
        editSnippet.setContent(content);
        editSnippet.setId(snippet.getId());
        model.addAttribute("snippet", editSnippet);
        model.addAttribute("user", userService.fetchOneById(snippet.getUserId()));
        return "view_snippet";
    }

    @PostMapping("/update")
    String update(@ModelAttribute("snippet") EditSnippet editSnippet) throws GSException {
        snippetService.update(editSnippet);

        return "redirect:/snippets";
    }

    static String calculateTimeAgoByTimeGranularity(Date pastTime, TimeGranularity defaultGranularity) {
        long timeDifferenceInMillis = Instant.now().toEpochMilli() - pastTime.getTime();

        if (timeDifferenceInMillis > TimeGranularity.HOURS.toMillis() * 24) {
            return timeDifferenceInMillis / TimeGranularity.DAYS.toMillis() + " "
                    + TimeGranularity.DAYS.name().toLowerCase() + " ago";
        }
        if (timeDifferenceInMillis > TimeGranularity.HOURS.toMillis()) {
            return timeDifferenceInMillis / TimeGranularity.HOURS.toMillis() + " "
                    + TimeGranularity.HOURS.name().toLowerCase() + " ago";
        }
        return timeDifferenceInMillis / defaultGranularity.toMillis() + " "
                + defaultGranularity.name().toLowerCase() + " ago";
    }

    public enum TimeGranularity {
        SECONDS {
            public long toMillis() {
                return TimeUnit.SECONDS.toMillis(1);
            }
        },
        MINUTES {
            public long toMillis() {
                return TimeUnit.MINUTES.toMillis(1);
            }
        },
        HOURS {
            public long toMillis() {
                return TimeUnit.HOURS.toMillis(1);
            }
        },
        DAYS {
            public long toMillis() {
                return TimeUnit.DAYS.toMillis(1);
            }
        },
        WEEKS {
            public long toMillis() {
                return TimeUnit.DAYS.toMillis(7);
            }
        },
        MONTHS {
            public long toMillis() {
                return TimeUnit.DAYS.toMillis(30);
            }
        },
        YEARS {
            public long toMillis() {
                return TimeUnit.DAYS.toMillis(365);
            }
        },
        DECADES {
            public long toMillis() {
                return TimeUnit.DAYS.toMillis(365 * 10);
            }
        };

        public abstract long toMillis();
    }
}
