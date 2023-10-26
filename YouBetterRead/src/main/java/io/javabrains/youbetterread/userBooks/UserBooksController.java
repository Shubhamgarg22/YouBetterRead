package io.javabrains.youbetterread.userBooks;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

import io.javabrains.youbetterread.book.Book;
import io.javabrains.youbetterread.book.BookRepository;
import io.javabrains.youbetterread.user.BookByUserRepo;
import io.javabrains.youbetterread.user.BooksbyUser;

@Controller
public class UserBooksController {

    @Autowired
    UserBookRepository userBookRepository;

    @Autowired
    BookByUserRepo bookByUserRepo;

    @Autowired
    BookRepository bookRepository;

    @PostMapping("/addUserBook")
    private ModelAndView addBookforUser(@RequestBody MultiValueMap<String, String> formData,
            @AuthenticationPrincipal OAuth2User principal) {

        if (principal == null || principal.getAttribute("login") == null)
            return null;

        String userId = principal.getAttribute("login");
        UserBookprimaryKey userBookprimaryKey = new UserBookprimaryKey();
        userBookprimaryKey.setUserId(userId);
        String bookId = formData.getFirst("bookId");
        int rating = Integer.parseInt(formData.getFirst("rating"));

        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (!optionalBook.isPresent()) {
            return new ModelAndView("redirect:/");
        }
        Book book = optionalBook.get();

        userBookprimaryKey.setBookId(bookId);

        UserBooks userBooks = new UserBooks();

        userBooks.setKey(userBookprimaryKey);
        userBooks.setStartDate(LocalDate.parse(formData.getFirst("startDate")));
        userBooks.setEndDate(LocalDate.parse(formData.getFirst("endDate")));
        userBooks.setReadingStatus(formData.getFirst("readingStatus"));
        userBooks.setRating(rating);

        userBookRepository.save(userBooks);

        BooksbyUser booksbyUser = new BooksbyUser();

        booksbyUser.setBookId(bookId);
        booksbyUser.setId(userId);
        booksbyUser.setRating(rating);
        booksbyUser.setReadingStatus(formData.getFirst("readingStatus"));
        booksbyUser.setAuthorNames(book.getAuthorNames());
        booksbyUser.setBookName(book.getName());
        booksbyUser.setCoverIds(book.getCoverIds());

        bookByUserRepo.save(booksbyUser);

        return new ModelAndView("redirect:/books/" + bookId);
    }

}
