package io.javabrains.youbetterread.home;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import io.javabrains.youbetterread.user.BookByUserRepo;
import io.javabrains.youbetterread.user.BooksbyUser;

@Controller
public class HomeController {

    @Autowired
    BookByUserRepo bookByUserRepo;

    private final String coverImageUrl = "https://covers.openlibrary.org/b/id/";

    @GetMapping("/")
    public String home(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal == null || principal.getAttribute("login") == null)
            return "index";

        String userId = principal.getAttribute("login");

        Slice<BooksbyUser> booksSlice = bookByUserRepo.findAllById(userId, CassandraPageRequest.of(0, 20));
        List<BooksbyUser> booksByUsers = booksSlice.getContent();
        booksByUsers = booksByUsers.stream().distinct().map(book -> {
            String coverImg = "/img/No_image_available.svg.webp";
            if (book.getCoverIds() != null && book.getCoverIds().size() > 0) {
                coverImg = coverImageUrl + book.getCoverIds().get(0) + "-L.jpg";
            }
            book.setCoverUrls(coverImg);
            return book;
        }).collect(Collectors.toList());

        model.addAttribute("booksByUser", booksByUsers);
        return "home";
    }
}
