package io.javabrains.youbetterread.book;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.javabrains.youbetterread.userBooks.UserBookRepository;
import io.javabrains.youbetterread.userBooks.UserBookprimaryKey;
import io.javabrains.youbetterread.userBooks.UserBooks;

@Controller
public class BookController {

    @Autowired
    BookRepository bookRepository;

    @Autowired
    UserBookRepository userBookRepository;

    private final String coverImageUrl = "https://covers.openlibrary.org/b/id/";

    @GetMapping(value = "/books/{bookId}")
    public String getBookbyBookId(@PathVariable String bookId, Model model,
            @AuthenticationPrincipal OAuth2User principal) {
        Optional<Book> optionalbook = bookRepository.findById(bookId);
        if (optionalbook.isPresent()) {
            Book book = optionalbook.get();

            String coverImg = "/img/No_image_available.svg.webp";
            if (book.getCoverIds() != null && book.getCoverIds().size() > 0) {
                coverImg = coverImageUrl + book.getCoverIds().get(0) + "-L.jpg";
            }
            model.addAttribute("coverImg", coverImg);
            model.addAttribute("book", book);

            if (principal != null && principal.getAttribute("login") != null) {
                String loginId = principal.getAttribute("login");
                model.addAttribute("loginId", loginId);

                UserBooks userBooks = new UserBooks();
                UserBookprimaryKey userBookprimaryKey = new UserBookprimaryKey();
                userBookprimaryKey.setBookId(bookId);
                userBookprimaryKey.setUserId(loginId);
                userBooks.setKey(userBookprimaryKey);
                Optional<UserBooks> optionalUserBook = userBookRepository.findById(userBooks.getKey());
                if (optionalUserBook.isPresent()) {
                    model.addAttribute("UserBook", optionalUserBook.get());
                } else {
                    model.addAttribute("UserBook", new UserBooks());
                }
            }

            return "book";
        }
        return "book-not-found";

    }
}
