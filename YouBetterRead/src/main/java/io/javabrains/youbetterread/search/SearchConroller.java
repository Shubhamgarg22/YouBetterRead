package io.javabrains.youbetterread.search;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Controller
public class SearchConroller {

    private final WebClient webClient;
    private final String coverImageUrl = "https://covers.openlibrary.org/b/id/";

    public SearchConroller(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://openlibrary.org/search.json").build();
    }

    @GetMapping(value = "/search")
    public String getSearchResults(@RequestParam String query, Model model) {
        // Reactive JAVA fetch API
        Mono<SearchResults> resultsMono = this.webClient.get()
                .uri("?q={query}", query)
                .retrieve().bodyToMono(SearchResults.class);

        SearchResults result = resultsMono.block();
        List<SearchResultBook> books = result.getDocs().stream().limit(12)
                .map(resultbook -> {
                    resultbook.setKey(resultbook.getKey().replace("/works/", ""));
                    String coverId = resultbook.getCover_i();
                    if (StringUtils.hasText(coverId)) {
                        coverId = coverImageUrl + coverId + "-L.jpg";
                    } else {
                        coverId = "/img/No_image_available.svg.webp";
                    }
                    resultbook.setCover_i(coverId);
                    return resultbook;
                })
                .collect(Collectors.toList());

        model.addAttribute("searchresult", books);

        return "search";
    }
}
